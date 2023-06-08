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

package com.oppo.cloud.parser.service;

import com.oppo.cloud.common.constant.LogPathType;
import com.oppo.cloud.common.domain.elasticsearch.JobAnalysis;
import com.oppo.cloud.common.domain.elasticsearch.TaskApp;
import com.oppo.cloud.common.domain.elasticsearch.SimpleUser;
import com.oppo.cloud.common.domain.job.App;
import com.oppo.cloud.common.domain.job.LogInfo;
import com.oppo.cloud.common.domain.job.LogPath;
import com.oppo.cloud.common.domain.job.LogRecord;
import com.oppo.cloud.common.util.spring.SpringBeanUtil;
import com.oppo.cloud.parser.domain.job.DetectorParam;
import com.oppo.cloud.parser.service.rules.JobRulesConfigService;
import com.oppo.cloud.parser.utils.ReplayEventLogs;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.*;

public class ParamUtil {
/*
    public static String[] readLines(String path) throws IOException {
        File file = new File(ParamUtil.class.getClassLoader().getResource(path).getPath());
        String content = new String(Files.readAllBytes(file.toPath()));
        return content.split("\n");
    }

    public static ReplayEventLogs getReplayEventLogs() throws Exception {
        String[] lines = readLines("log/event/eventlog");
        ReplayEventLogs replayEventLogs = new ReplayEventLogs();
        replayEventLogs.replay(lines);
        return replayEventLogs;
    }

    public static DetectorParam getDetectorParam() throws Exception {
        ReplayEventLogs replayEventLogs = getReplayEventLogs();

        Long appDuration = replayEventLogs.getApplication().getAppDuration();
        if (appDuration == null || appDuration < 0) {
            appDuration = 0L;
        }

        JobRulesConfigService jobRulesConfigService = (JobRulesConfigService) SpringBeanUtil.getBean(JobRulesConfigService.class);
        LogRecord logRecord = getLogRecord();

        DetectorParam detectorParam = new DetectorParam(logRecord.getJobAnalysis().getFlowName(),
                logRecord.getJobAnalysis().getProjectName(), logRecord.getJobAnalysis().getTaskName(),
                logRecord.getJobAnalysis().getExecutionDate(), logRecord.getJobAnalysis().getRetryTimes(),
                logRecord.getApps().get(0).getAppId(), appDuration, "",
                jobRulesConfigService.detectorConfig, replayEventLogs, logRecord.getIsOneClick());

        return detectorParam;
    }

    public static LogRecord getLogRecord() {
        LogRecord logRecord = new LogRecord();
        logRecord.setIsOneClick(false);

        JobAnalysis jobAnalysis = new JobAnalysis();
        jobAnalysis.setProjectName("test");
        jobAnalysis.setFlowName("test");
        jobAnalysis.setTaskName("test");
        jobAnalysis.setExecutionDate(new Date());
        jobAnalysis.setDuration(10000D);
        jobAnalysis.setRetryTimes(1);
        jobAnalysis.setStartTime(new Date());
        jobAnalysis.setEndTime(new Date());

        List<SimpleUser> simpleUsers = new ArrayList<>();
        SimpleUser simpleUser = new SimpleUser();
        simpleUser.setUserId(1);
        simpleUser.setUsername("username");
        jobAnalysis.setUsers(simpleUsers);

        logRecord.setJobAnalysis(jobAnalysis);

        Map<String, TaskApp> taskAppList = new HashMap<>();
        TaskApp taskApp = new TaskApp();
        taskApp.setApplicationId("appId");
        String str =
                "User class threw exception: java.sql.SQLException: Access denied for user 'ads_da_user'@'10.39.12.161' (using password: YES)";
        taskApp.setDiagnostics(str);
        taskApp.setQueue("root");
        taskApp.setProjectName("test");
        taskApp.setFlowName("test");
        taskApp.setTaskName("test");
        taskApp.setStartTime(new Date());
        taskApp.setFinishTime(new Date());

        taskApp.setExecutionDate(new Date());
        taskAppList.put("appId", taskApp);
        logRecord.setTaskAppList(taskAppList);

        List<App> apps = new ArrayList<>();
        App app = new App();
        app.setAppId("appId");
        app.setTryNumber(0);
        app.setAmHost("driver");

        Map<String, List<LogPath>> logPathMap = new HashMap<>();
        LogPath scheduler = new LogPath();
        scheduler.setProtocol("hdfs");
        scheduler.setLogType("scheduler");
        scheduler.setLogPath("hdfs://logs-hdfs/scheduler/2");
        scheduler.setLogPathType(LogPathType.DIRECTORY);

        List<LogPath> schedulerList = new ArrayList<>();
        schedulerList.add(scheduler);

        logPathMap.put("scheduler", schedulerList);

        LogPath executor = new LogPath();
        executor.setProtocol("hdfs");
        executor.setLogType("executor");
        executor.setLogPathType(LogPathType.DIRECTORY);
        executor.setLogPath("hdfs://logs-hdfs/tmp/logs/root/logs/application_1673850090992_23147");
        List<LogPath> executorList = new ArrayList<>();
        executorList.add(executor);

        LogPath event = new LogPath();
        event.setProtocol("hdfs");
        event.setLogType("event");
        event.setLogPath("hdfs://logs-hdfs/spark/application_1662538803418_944346_1");
        event.setLogPathType(LogPathType.FILE);
        List<LogPath> eventList = new ArrayList<>();
        eventList.add(event);

        Map<String, List<LogPath>> logPathMap1 = new HashMap<>();
        logPathMap1.put("event", eventList);
        logPathMap1.put("executor", executorList);

        List<LogInfo> logInfoList = new ArrayList<>();
        LogInfo logInfo = new LogInfo();
        logInfo.setLogGroup("scheduler");
        logInfo.setLogPathMap(logPathMap);

        LogInfo logInfo1 = new LogInfo();
        logInfo1.setLogGroup("spark");
        logInfo1.setLogPathMap(logPathMap1);

        logInfoList.add(logInfo);
        logInfoList.add(logInfo1);

        app.setLogInfoList(logInfoList);
        apps.add(app);

        logRecord.setApps(apps);

        return logRecord;
    }
 */
}
