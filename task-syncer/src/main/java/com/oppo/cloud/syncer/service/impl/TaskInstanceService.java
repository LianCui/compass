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

package com.oppo.cloud.syncer.service.impl;

import com.alibaba.fastjson2.JSON;
import com.oppo.cloud.common.domain.syncer.TableMessage;
import com.oppo.cloud.model.TaskInstance;
import com.oppo.cloud.syncer.dao.TaskInstanceExtendMapper;
import com.oppo.cloud.syncer.domain.Mapping;
import com.oppo.cloud.syncer.domain.RawTable;
import com.oppo.cloud.syncer.producer.MessageProducer;
import com.oppo.cloud.syncer.service.ActionService;
import com.oppo.cloud.syncer.util.DataUtil;
import com.oppo.cloud.syncer.util.databuild.TaskInstanceBuilder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.Map;

/**
 * 任务或者job实例执行记录同步
 */
@Slf4j
@Service
public class TaskInstanceService extends CommonService implements ActionService {

    @Autowired
    private TaskInstanceExtendMapper taskInstanceMapper;

    @Autowired
    @Qualifier("sourceJdbcTemplate")
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private MessageProducer messageProducer;

    /**
     * 插入操作
     */
    @Override
    public void insert(RawTable rawTable, Mapping mapping) {
        dataMapping(jdbcTemplate, rawTable, mapping, "INSERT");
    }

    /**
     * 删除操作
     */
    @Override
    public void delete(RawTable rawTable, Mapping mapping) {
    }

    /**
     * 更新操作
     */
    @Override
    public void update(RawTable rawTable, Mapping mapping) {
        dataMapping(jdbcTemplate, rawTable, mapping, "UPDATE");
    }

    /**
     * 数据保存操作
     */
    @Override
    public void dataSave(Map<String, String> data, Mapping mapping, String action) {
        TaskInstance instance = (TaskInstance) DataUtil.parseInstance(data, TaskInstanceBuilder.class);
        log.info("dataSave instance: " + instance.toString());

        switch (action) {
            case "INSERT":
                if (instance.getCreateTime() == null) {
                    instance.setCreateTime(new Date());
                }
                taskInstanceMapper.saveSelective(instance);
                break;
            case "UPDATE":
                if (instance.getUpdateTime() == null) {
                    instance.setUpdateTime(new Date());
                }
                if (instance.getId() != null) {
                    taskInstanceMapper.updateByPrimaryKeySelective(instance);
                } else {
                    taskInstanceMapper.updateByCompositePrimaryKeySelective(instance);
                }
                break;
            default:
                return;
        }

        // 数据写回kafka订阅
        if (!DataUtil.isEmpty(mapping.getWriteKafkaTopic())) {
            try {
                String message = JSON.toJSONString(new TableMessage(
                        JSON.toJSONString(data),
                        JSON.toJSONString(instance),
                        action,
                        mapping.getTargetTable()));
                log.info("push to kafka. mapping:" + mapping + "\tmsg: " + message);
                messageProducer.sendMessageSync(mapping.getWriteKafkaTopic(), message);
            } catch (Exception ex) {
                log.error("failed to send insert data to kafka, err: " + ex.getMessage());
            }
        }
    }
}
