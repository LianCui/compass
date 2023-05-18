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

package com.oppo.cloud.application.domain;

import lombok.Data;

/**
 * 文本解析规则
 */
@Data
public class Rule {

    /**
     * 依赖查询路径
     */
    private LogPathDep logPathDep;
    /**
     * 日志路径字段组成
     */
    private LogPathJoin logPathJoins;
    /**
     * 提取规则
     */
    private ExtractLog extractLog;
}
