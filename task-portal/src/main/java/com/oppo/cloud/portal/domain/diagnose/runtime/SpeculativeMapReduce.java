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

package com.oppo.cloud.portal.domain.diagnose.runtime;

import com.oppo.cloud.portal.domain.diagnose.IsAbnormal;
import com.oppo.cloud.portal.domain.diagnose.Table;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.LinkedHashMap;


@Data
@ApiModel("Map/Reduce数量过多")
public class SpeculativeMapReduce extends IsAbnormal {

    @ApiModelProperty(value = "Map/Reduce数量过多表单数据")
    private Table<TaskInfo> taskList = new Table<>();

    public SpeculativeMapReduce() {
        LinkedHashMap<String, String> titleMap = new LinkedHashMap<>();
        titleMap.put("taskType", "任务类型");
        titleMap.put("count", "任务数量");
        titleMap.put("threshold", "阈值");
        taskList.setTitles(titleMap);
    }

    @Data
    public static class TaskInfo {

        @ApiModelProperty(value = "任务类型")
        private String taskType;

        @ApiModelProperty(value = "任务数量")
        private int count;

        @ApiModelProperty(value = "阈值")
        private int threshold;
    }
}