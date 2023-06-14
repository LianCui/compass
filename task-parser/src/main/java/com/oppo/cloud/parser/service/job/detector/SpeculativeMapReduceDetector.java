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

package com.oppo.cloud.parser.service.job.detector;

import com.oppo.cloud.common.constant.AppCategoryEnum;
import com.oppo.cloud.common.domain.eventlog.DetectorResult;
import com.oppo.cloud.common.domain.eventlog.SpeculativeMapReduceAbnormal;
import com.oppo.cloud.common.domain.eventlog.config.GlobalSortConfig;
import com.oppo.cloud.parser.domain.job.DetectorParam;
import com.oppo.cloud.parser.domain.mapreduce.jobhistory.JobFinishedEvent;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class SpeculativeMapReduceDetector implements IDetector {

    private final DetectorParam param;

    private final GlobalSortConfig config;

    public SpeculativeMapReduceDetector(DetectorParam param) {
        this.param = param;
        this.config = param.getConfig().getGlobalSortConfig();

    }

    @Override
    public DetectorResult detect() {
        log.info("start SpeculativeMapReduceDetector");
        DetectorResult<SpeculativeMapReduceAbnormal> detectorResult =
                new DetectorResult<>(AppCategoryEnum.SPECULATIVE_MAP_REDUCE.getCategory(), false);

        SpeculativeMapReduceAbnormal globalSortAbnormalList = new SpeculativeMapReduceAbnormal();
        JobFinishedEvent jobFinishedEvent = this.param.getReplayEventLogs().getJobFinishedEvent();
        if (jobFinishedEvent.getFinishedMaps() > 0) {
            globalSortAbnormalList.setAbnormal(true);
            globalSortAbnormalList.setFinishedMaps(jobFinishedEvent.getFinishedMaps());
            globalSortAbnormalList.setFinishedReduces(jobFinishedEvent.getFailedReduces());
            detectorResult.setData(globalSortAbnormalList);
            detectorResult.setAbnormal(true);
            return detectorResult;
        }
        return null;
    }
}