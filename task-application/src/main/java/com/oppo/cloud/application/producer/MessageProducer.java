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

package com.oppo.cloud.application.producer;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import org.springframework.util.concurrent.ListenableFutureCallback;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * 生产者消费消息
 */
@Component
public class MessageProducer {

    @Autowired
    @Qualifier("kafkaTemplate")
    private KafkaTemplate kafkaTemplate;

    /**
     * 同步发送消息
     */
    public void sendMessageSync(String topic,
                                String message) throws ExecutionException, InterruptedException, TimeoutException {
        kafkaTemplate.send(topic, message).get(10, TimeUnit.SECONDS);
    }

    /**
     * 异步发送消息
     */
    public void sendMessageAsync(String topic, String message) {
        kafkaTemplate.send(topic, message).addCallback(new ListenableFutureCallback() {

            @Override
            public void onFailure(Throwable ex) {
                // todo: log
            }

            @Override
            public void onSuccess(Object result) {
                // todo: log
            }
        });
    }
}
