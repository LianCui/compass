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

package com.oppo.cloud.parser.domain.job;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.oppo.cloud.common.domain.elasticsearch.TaskApp;
import com.oppo.cloud.common.domain.job.App;
import com.oppo.cloud.common.domain.job.LogInfo;
import com.oppo.cloud.common.domain.job.LogRecord;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

/**
 * 任务基本信息
 */
@Slf4j
@Data
public class TaskParam {
    private TaskApp taskApp;

    private Boolean isOneClick;

    public TaskParam(TaskApp taskApp, Boolean isOneClick) {
        this.taskApp = taskApp;
        this.isOneClick = isOneClick;
    }

}
