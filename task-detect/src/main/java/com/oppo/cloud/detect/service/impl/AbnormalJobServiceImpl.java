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
import com.oppo.cloud.common.util.DateUtil;
import com.oppo.cloud.detect.service.AbnormalJobService;
import com.oppo.cloud.detect.service.TaskAppService;
import com.oppo.cloud.detect.service.ElasticSearchService;
import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * 异常任务数据更新
 */
@Slf4j
@Service
public class AbnormalJobServiceImpl implements AbnormalJobService {

    @Autowired
    private ElasticSearchService elasticSearchService;

    @Autowired
    private TaskAppService taskAppService;

    @Value("${custom.elasticsearch.job-index}")
    private String jobIndex;

    /**
     * 更新内存数据和Vcore数据
     */
    @Override
    public void updateVcoreAndMemory(JobAnalysis jobAnalysis) throws Exception {
        List<TaskApp> taskAppList = taskAppService.searchTaskApps(jobAnalysis);
        double vcoreSeconds = 0;
        double memorySeconds = 0;
        for (TaskApp taskApp : taskAppList) {
            vcoreSeconds += taskApp.getVcoreSeconds() == null ? 0 : taskApp.getVcoreSeconds();
            memorySeconds += taskApp.getMemorySeconds() == null ? 0 : taskApp.getMemorySeconds();
        }
        JobAnalysis jobAnalysisEs = this.searchJob(jobAnalysis);
        jobAnalysis.setVcoreSeconds(vcoreSeconds);
        jobAnalysis.setMemorySeconds(memorySeconds);
        elasticSearchService.insertOrUpDateEs(jobAnalysisEs.getIndex(), jobAnalysisEs.getDocId(),
                jobAnalysisEs.genDoc());
    }

    /**
     * 增量更新内存数据和vCore数据
     */
    @Override
    public void updateResource(JobAnalysis jobAnalysis, List<TaskApp> taskAppList) {
        double memorySecond = jobAnalysis.getMemorySeconds() == null ? 0.0 : jobAnalysis.getMemorySeconds();
        double vcoreSecond = jobAnalysis.getVcoreSeconds() == null ? 0.0 : jobAnalysis.getVcoreSeconds();
        for (TaskApp taskApp : taskAppList) {
            memorySecond += taskApp.getMemorySeconds();
            vcoreSecond += taskApp.getVcoreSeconds();
        }
        jobAnalysis.setMemorySeconds(memorySecond);
        jobAnalysis.setVcoreSeconds(vcoreSecond);
    }

    @Override
    public JobAnalysis searchJob(JobAnalysis jobAnalysis) throws Exception {
        HashMap<String, Object> termQuery = new HashMap<>();
        termQuery.put("projectName.keyword", jobAnalysis.getProjectName());
        termQuery.put("flowName.keyword", jobAnalysis.getFlowName());
        termQuery.put("taskName.keyword", jobAnalysis.getTaskName());
        termQuery.put("executionDate", DateUtil.timestampToUTCDate(jobAnalysis.getExecutionDate().getTime()));
        SearchSourceBuilder searchSourceBuilder = elasticSearchService.genSearchBuilder(termQuery, null, null, null);
        List<JobAnalysis> jobAnalysisList =
                elasticSearchService.find(JobAnalysis.class, searchSourceBuilder, jobIndex + "-*");
        if (jobAnalysisList.size() != 0) {
            return jobAnalysisList.get(0);
        } else {
            return null;
        }
    }

    @Override
    public void insertOrUpdate(JobAnalysis jobAnalysis) throws Exception {
        Map<String, Object> termQuery = new HashMap<>();
        termQuery.put("projectName.keyword", jobAnalysis.getProjectName());
        termQuery.put("flowName.keyword", jobAnalysis.getFlowName());
        termQuery.put("taskName.keyword", jobAnalysis.getTaskName());
        termQuery.put("executionDate", DateUtil.timestampToUTCDate(jobAnalysis.getExecutionDate().getTime()));
        SearchSourceBuilder searchSourceBuilder = elasticSearchService.genSearchBuilder(termQuery, null, null, null);
        List<JobAnalysis> jobAnalysisList =
                elasticSearchService.find(JobAnalysis.class, searchSourceBuilder, jobIndex + "-*");
        if (jobAnalysisList.size() != 0) {
            JobAnalysis jobAnalysisEs = jobAnalysisList.get(0);
            jobAnalysis.setDocId(jobAnalysisEs.getDocId());
            jobAnalysis.setIndex(jobAnalysisEs.getIndex());
            jobAnalysis.setCategories(jobAnalysisEs.getCategories());
        }
        elasticSearchService.insertOrUpDateEs(jobAnalysis.genIndex(jobIndex), jobAnalysis.genDocId(),
                jobAnalysis.genDoc());
    }
}
