/*
 * Copyright 2023 OPPO.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.oppo.cloud.meta.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.TypeFactory;
import com.oppo.cloud.common.constant.Constant;
import com.oppo.cloud.common.domain.cluster.spark.SparkApp;
import com.oppo.cloud.common.domain.cluster.spark.SparkApplication;
import com.oppo.cloud.common.domain.cluster.yarn.Attempt;
import com.oppo.cloud.common.service.RedisService;
import com.oppo.cloud.common.util.DateUtil;
import com.oppo.cloud.common.util.elastic.BulkApi;
import com.oppo.cloud.meta.config.HadoopConfig;
import com.oppo.cloud.meta.service.IClusterConfigService;
import com.oppo.cloud.meta.service.ITaskSyncerMetaService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.elasticsearch.action.bulk.BulkItemResponse;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.client.RestHighLevelClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 同步spark app元数据
 */
@Slf4j
@Service("SparkMetaServiceImpl")
public class SparkMetaServiceImpl implements ITaskSyncerMetaService {

    @Value("${scheduler.sparkMeta.limitCount}")
    private long limitCount;

    @Value("${spring.elasticsearch.spark-app-prefix}")
    private String sparkAppPrefix;
    @Resource
    private HadoopConfig config;

    @Resource
    private IClusterConfigService iClusterConfigService;

    @Resource(name = "restTemplate")
    private RestTemplate restTemplate;

    @Resource
    private RedisService redisService;

    @Resource
    private Executor sparkMetaExecutor;

    @Resource
    private ObjectMapper objectMapper;

    @Resource
    private RestHighLevelClient client;

    private final Pattern hdfsPattern = Pattern.compile(".*?(?<hdfs>hdfs://.*)</li>.*", Pattern.DOTALL);

    private static final String SPARK_HOME_URL = "http://%s/";

    private static final String SPARK_APPS_URL = "http://%s/api/v1/applications?limit=%d&minDate=%s";

    @Override
    public void syncer() {
        List<String> clusters = iClusterConfigService.getSparkHistoryServers();
        log.info("sparkClusters:{}", clusters);
        if (clusters == null) {
            return;
        }
        CompletableFuture[] array = new CompletableFuture[clusters.size()];
        for (int i = 0; i < clusters.size(); i++) {
            int finalI = i;
            array[i] = CompletableFuture.supplyAsync(() -> {
                try {
                    // TODO
                    pull(clusters.get(finalI));
                } catch (Exception e) {
                    log.error(e.getMessage());
                }
                return null;
            }, sparkMetaExecutor);
        }
        try {
            CompletableFuture.allOf(array).get();
        } catch (Exception e) {
            log.error(e.getMessage());
        }
    }

    /**
     * 任务数据同步
     */
    public void pull(String shs) {
        log.info("start to pull spark tasks:{}", shs);
        String eventLogDirectory;
        try {
            // TODO 获取SparkHistoryServer Event log directory: hdfs://ip:port/spark/, 缓存到redis
            eventLogDirectory = getEventLogDirectory(shs);
        } catch (Exception e) {
            log.error("sparkMetaErr:eventLogDirectory:{},{}", shs, e.getMessage());
            return;
        }
        if (StringUtils.isBlank(eventLogDirectory)) {
            log.info("sparkMetaErr:eventLogDirectory:{}", shs);
            return;
        }

        // TODO spark任务获取
        List<SparkApplication> apps = sparkRequest(shs);
        if (apps == null || apps.size() == 0) {
            log.error("sparkMetaErr:appsNull:{}", shs);
            return;
        }
        log.info("sparkApps:{},{}", shs, apps.size());
        Map<String, Map<String, Object>> sparkAppMap = new HashMap<>();

        for (SparkApplication info : apps) {
            if (info.getAttempts() == null || info.getAttempts().size() == 0) {
                log.error("sparkHistoryInfoAttemptSizeZero {},{}", shs, info);
                continue;
            }
            Attempt attempt = info.getAttempts().get(0);
            try {
                SparkApp sparkApp = new SparkApp(info.getId(), eventLogDirectory, attempt, shs);
                log.debug("sparkApp:{}", sparkApp);
                String id = sparkApp.getSparkHistoryServer() + "_" + sparkApp.getAppId();
                sparkAppMap.put(id, sparkApp.getSparkAppMap());
            } catch (Exception e) {
                log.error("saveSparkAppsErr:{},{},{}", shs, e.getMessage(), e);
            }
        }

        BulkResponse response;
        try {
            // TODO SAVE TO ES ,通过 Id 更新或插入
            response = BulkApi.bulkByIds(client, sparkAppPrefix + DateUtil.getDay(0), sparkAppMap);
        } catch (IOException e) {
            log.error("bulkSparkAppsErr:{}", e.getMessage());
            return;
        }
        BulkItemResponse[] responses = response.getItems();

        for (BulkItemResponse r : responses) {
            if (r.isFailed()) {
                log.info("failedInsertApp:{},{}", r.getId(), r.status());
            }
        }

        log.info("saveSparkAppCount:{},{}", shs, sparkAppMap.size());
    }

    /**
     * spark 任务获取
     */
    public List<SparkApplication> sparkRequest(String shs) {
        String url = String.format(SPARK_APPS_URL, shs, limitCount, DateUtil.getDay(-1));
        log.info("sparkUrl:{}", url);
        ResponseEntity<String> responseEntity = null;
        try {
            responseEntity = restTemplate.getForEntity(url, String.class);
        } catch (RestClientException e) {
            log.error("sparkRequestErr:{},{}", shs, e.getMessage());
            return null;
        }
        if (responseEntity.getBody() == null) {
            log.error("sparkRequestErr:{}", shs);
            return null;
        }
        List<SparkApplication> value;
        try {
            value = objectMapper.readValue(responseEntity.getBody(),
                    TypeFactory.defaultInstance().constructCollectionType(List.class, SparkApplication.class));
        } catch (JsonProcessingException e) {
            log.error("sparkRequestErr:{},{}", shs, e.getMessage());
            return null;
        }
        return value;
    }

    /**
     * 获取SparkHistoryServer Event log directory: hdfs://ip:port/spark/
     */
    public String getEventLogDirectory(String ip) throws Exception {
        String key = Constant.SPARK_EVENT_LOG_DIRECTORY + ip;
        String cacheResult = (String) redisService.get(key);
        if (StringUtils.isNotBlank(cacheResult)) {
            log.info("getEventLogDirectoryFromCache:{},{}", key, cacheResult);
            return cacheResult;
        }
        ResponseEntity<String> responseEntity;
        try {
            responseEntity = restTemplate.getForEntity(String.format(SPARK_HOME_URL, ip),
                    String.class);
        } catch (Exception e) {
            log.error("getEventLogDirectoryErr:{},{}", ip, e.getMessage());
            return "";
        }
        if (responseEntity.getBody() != null) {
            Matcher m = hdfsPattern.matcher(responseEntity.getBody());
            if (m.matches()) {
                String path = m.group("hdfs");
                if (StringUtils.isNotBlank(path)) {
                    log.info("cacheEventLogDirectory:{},{}", key, path);
                    // TODO 缓存 REDIS
                    redisService.set(key, path, Constant.DAY_SECONDS);
                }
                return path;
            }
        }
        return "";
    }

}
