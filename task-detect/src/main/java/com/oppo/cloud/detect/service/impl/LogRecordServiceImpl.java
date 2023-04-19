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

package com.oppo.cloud.detect.service.impl;

import com.oppo.cloud.common.domain.elasticsearch.JobAnalysis;
import com.oppo.cloud.common.domain.elasticsearch.TaskApp;
import com.oppo.cloud.common.domain.job.App;
import com.oppo.cloud.detect.service.LogRecordService;
import com.oppo.cloud.detect.service.SchedulerLogService;
import com.oppo.cloud.detect.service.TaskInstanceService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 *  日志解析消息接口
 */
@Service
@Slf4j
public class LogRecordServiceImpl implements LogRecordService {

    @Autowired
    SchedulerLogService schedulerLogService;

    @Autowired
    TaskInstanceService taskInstanceService;

    @Override
    public List<App> getSchedulerLog(JobAnalysis detectJobAnalysis) {
        List<App> apps = new ArrayList<>();
        // 根据任务重试次数构造出调度日志
        for (int i = 0; i <= detectJobAnalysis.getRetryTimes(); i++) {
            List<String> logPaths = schedulerLogService.getSchedulerLog(detectJobAnalysis.getProjectName(),
                    detectJobAnalysis.getFlowName(), detectJobAnalysis.getTaskName(),
                    detectJobAnalysis.getExecutionDate(), i);
            log.error("-------------------");
            log.error(detectJobAnalysis.toString());
            for(int j=0;j<logPaths.size();j++){
                log.error(logPaths.get(j));
            }
            log.error(detectJobAnalysis.toString());
            if (logPaths != null && logPaths.size() != 0) {
                App app = new App();
                app.formatSchedulerLog(logPaths, i);
                apps.add(app);
            }
        }
        // 构造出每次重试的调度日志才算成功
        if (apps.size() < detectJobAnalysis.getRetryTimes() + 1) {
            apps = new ArrayList<>();
        }
        return apps;
    }

    @Override
    public List<App> getAppLog(List<TaskApp> taskAppList) {
        List<App> apps = new ArrayList<>();
        // 如果有信息完整的appId,则一起发送
        for (TaskApp taskApp : taskAppList) {
            App app = new App();
            app.formatAppLog(taskApp);
            apps.add(app);
        }
        return apps;
    }
}
