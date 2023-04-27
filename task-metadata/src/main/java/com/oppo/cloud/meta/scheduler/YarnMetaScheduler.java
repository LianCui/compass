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

package com.oppo.cloud.meta.scheduler;

import com.oppo.cloud.meta.service.ITaskSyncerMetaService;
import lombok.extern.slf4j.Slf4j;
import org.apache.curator.framework.recipes.locks.InterProcessMutex;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.Scheduled;

import javax.annotation.Resource;
import java.util.concurrent.TimeUnit;

/**
 * YARN任务app列表数据同步
 */
@Slf4j
@Configuration
@ConditionalOnProperty(prefix = "scheduler.yarnMeta", name = "enable", havingValue = "true")
public class YarnMetaScheduler {

    @Resource(name = "YarnMetaServiceImpl")
    private ITaskSyncerMetaService yarn;

    @Resource(name = "yarnMetaLock")
    private InterProcessMutex lock;

    @Scheduled(cron = "${scheduler.yarnMeta.cron}")
    private void run() {
        try {
            lock();
        } catch (Exception e) {
            log.error("Exception:",e);
        }
    }

    /**
     * zk锁，防止多实例同时同步数据
     */
    private void lock() throws Exception {
        if (!lock.acquire(1, TimeUnit.SECONDS)) {
            log.warn("cannot get {}", lock.getParticipantNodes());
            return;
        }
        try {
            log.info("get {}", lock.getParticipantNodes());
            // TODO
            yarn.syncer();
        } finally {
            log.info("release {}", lock.getParticipantNodes());
            lock.release();
        }
    }
}
