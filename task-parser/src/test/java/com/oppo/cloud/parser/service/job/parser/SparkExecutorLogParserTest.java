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

package com.oppo.cloud.parser.service.job.parser;

import com.oppo.cloud.common.constant.LogType;
import com.oppo.cloud.common.domain.job.LogPath;
import com.oppo.cloud.common.domain.job.LogRecord;
import com.oppo.cloud.parser.domain.job.CommonResult;
import com.oppo.cloud.parser.domain.job.ParserParam;
import com.oppo.cloud.parser.service.ParamUtil;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;
import java.util.Map;

class SparkExecutorLogParserTest {
    /*

    @Test
    void run() {
        LogRecord logRecord = ParamUtil.getLogRecord();
        Map<String, List<LogPath>> logPathMap = logRecord.getApps().get(0).getLogInfoList().get(1).getLogPathMap();

        ParserParam param = new ParserParam(
                LogType.SPARK_EXECUTOR.getName(),
                logRecord, logRecord.getApps().get(0),
                logPathMap.get(LogType.SPARK_EXECUTOR.getName())
        );

        SparkExecutorLogParser parser = new SparkExecutorLogParser(param);
        CommonResult commonResult = parser.run();
        System.out.println(commonResult);
    }
     */
}
