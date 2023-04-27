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
import com.oppo.cloud.common.constant.Constant;
import com.oppo.cloud.common.domain.cluster.yarn.YarnApp;
import com.oppo.cloud.common.domain.cluster.yarn.YarnResponse;
import com.oppo.cloud.common.util.DateUtil;
import com.oppo.cloud.common.util.elastic.BulkApi;
import com.oppo.cloud.meta.config.HadoopConfig;
import com.oppo.cloud.meta.service.IClusterConfigService;
import com.oppo.cloud.meta.service.ITaskSyncerMetaService;
import lombok.extern.slf4j.Slf4j;
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

/**
 * 同步yarn app元数据
 */
@Slf4j
@Service("YarnMetaServiceImpl")
public class YarnMetaServiceImpl implements ITaskSyncerMetaService {

    @Resource
    private HadoopConfig config;

    @Value("${scheduler.yarnMeta.startedTimeBegin}")
    private long startedTimeBegin;

    @Value("${spring.elasticsearch.yarn-app-prefix}")
    private String yarnAppPrefix;

    @Resource
    private Executor yarnMetaExecutor;

    @Resource(name = "restTemplate")
    private RestTemplate restTemplate;

    @Resource
    private IClusterConfigService iClusterConfigService;

    @Resource
    private ObjectMapper objectMapper;

    @Resource
    private RestHighLevelClient client;
    /**
     * 限定开始运行时间戳
     */
    private static final String YARN_APPS_URL = "http://%s/ws/v1/cluster/apps?startedTimeBegin=%d";

    /**
     * 集群并发同步
     */
    @Override
    public void syncer() {
        Map<String, String> yarnClusters = iClusterConfigService.getYarnClusters();
        log.info("yarnClusters:{}", yarnClusters);
        if (yarnClusters == null || yarnClusters.size() == 0) {
            log.error("yarnClusters empty");
            return;
        }

        CompletableFuture[] array = new CompletableFuture[yarnClusters.size()];
        int i = 0;
        for (Map.Entry<String, String> yarnCluster : yarnClusters.entrySet()) {
            array[i] = CompletableFuture.supplyAsync(() -> {
                try {
                    // TODO
                    pull(yarnCluster.getKey(), yarnCluster.getValue());
                } catch (Exception e) {
                    log.error(e.getMessage());
                }
                return null;
            }, yarnMetaExecutor);
            i++;
        }

        try {
            CompletableFuture.allOf(array).get();
        } catch (Exception e) {
            log.error(e.getMessage());
        }
    }

    /**
     * 拉取集群app元数据
     */
    public void pull(String ip, String clusterName) {
        log.info("start to pull yarn tasks:{}", ip);
        // TODO 获取 YARN APP
        List<YarnApp> apps = yarnRequest(ip);
        if (apps == null) {
            log.error("yarnMetaErr:appsNull:{}", ip);
            return;
        }

        Map<String, Map<String, Object>> yarnAppMap = new HashMap<>();
        for (YarnApp app : apps) {
            String id = ip + "_" + app.getId();
            app.setCreateTime(System.currentTimeMillis());
            app.setIp(ip);
            app.setClusterName(clusterName);
            yarnAppMap.put(id, app.getYarnAppMap());
            log.info("yarnApp-->{},{},{},{}", ip, app.getId(), app.getFinishedTime(), app.getFinalStatus());
        }
        BulkResponse response;
        try {
            // TODO SAVE TO ES ，通过id判断更新还是插入
            response = BulkApi.bulkByIds(client, yarnAppPrefix + DateUtil.getDay(0), yarnAppMap);
        } catch (IOException e) {
            log.error("bulkYarnAppsErr:{}", e.getMessage());
            return;
        }
        BulkItemResponse[] responses = response.getItems();

        for (BulkItemResponse r : responses) {
            if (r.isFailed()) {
                log.info("failedInsertApp:{},{}", r.getId(), r.status());
            }
        }

        log.info("saveYarnAppCount:{},{}", ip, yarnAppMap.size());
    }

    /**
     * yarn 任务获取
     */
    public List<YarnApp> yarnRequest(String ip) {
        long begin = System.currentTimeMillis() - startedTimeBegin * Constant.HOUR_MS;
        String url = String.format(YARN_APPS_URL, ip, begin);
        log.info("yarnUrl:{}", url);
        ResponseEntity<String> responseEntity;
        try {
            responseEntity = restTemplate.getForEntity(url, String.class);
        } catch (RestClientException e) {
            log.error("yarnRequestErr:{},{}", ip, e.getMessage());
            return null;
        }
        if (responseEntity.getBody() == null) {
            log.error("yarnRequestErr:{}", ip);
            return null;
        }
        YarnResponse value;
        try {
            value = objectMapper.readValue(responseEntity.getBody(), YarnResponse.class);
        } catch (JsonProcessingException e) {
            log.error(e.getMessage());
            return null;
        }
        if (value == null || value.getApps() == null || value.getApps().getApp() == null
                || value.getApps().getApp().size() == 0) {
            log.error("yarnRequestErr:null");
            return null;
        }
        return value.getApps().getApp();
    }

}
