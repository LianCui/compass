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

package com.oppo.cloud.application.consumer;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.TypeReference;
import com.oppo.cloud.application.constant.RetCode;
import com.oppo.cloud.application.domain.DelayedTaskInfo;
import com.oppo.cloud.application.domain.ParseRet;
import com.oppo.cloud.application.service.DelayTaskParserService;
import com.oppo.cloud.application.service.DelayedTaskService;
import com.oppo.cloud.application.service.LogParserService;
import com.oppo.cloud.common.domain.syncer.TableMessage;
import com.oppo.cloud.model.TaskInstance;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.Consumer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.UUID;

/**
 * 消费task-instance任务实例
 */
@Slf4j
@Component
public class ConsumerMessage {

    @Autowired
    private DelayTaskParserService delayTaskParserService;

    /**
     * 日志消费
     */
    @KafkaListener(topics = "${spring.kafka.taskinstancetopics}", containerFactory = "kafkaListenerContainerFactory")
    public void receive(@Payload String message, @Header(KafkaHeaders.RECEIVED_PARTITION_ID) int partition,
                        @Header(KafkaHeaders.RECEIVED_TOPIC) String topic, Consumer consumer,
                        Acknowledgment ack) throws Exception {
        log.debug(String.format("%d, From partition %d: %s", consumer.hashCode(), partition, message));

        TableMessage tableMessage = JSON.parseObject(message, TableMessage.class);

        delayTaskParserService.handle(tableMessage);

        consumer.commitSync();
    }
}
