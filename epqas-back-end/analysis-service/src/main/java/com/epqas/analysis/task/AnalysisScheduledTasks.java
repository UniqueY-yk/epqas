package com.epqas.analysis.task;

import com.epqas.analysis.mapper.ExamMetricsComputeMapper;
import com.epqas.analysis.service.AnalysisComputationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class AnalysisScheduledTasks {

    private final ExamMetricsComputeMapper computeMapper;
    private final AnalysisComputationService computationService;

    /**
     * 定期检查是否有已生成学生成绩但未生成质量分析的考试。
     * 演示目的：每1分钟（60,000毫秒）运行一次。
     * 在生产环境中，这可能会每天运行或在评分阶段后运行。
     */
    @Scheduled(fixedDelay = 60000)
    public void calculatePendingExamMetrics() {
        try {
            List<Long> pendingExams = computeMapper.findPendingExamIdsForAnalysis();
            if (pendingExams != null && !pendingExams.isEmpty()) {
                log.info("Found {} pending exams requiring indicator calculation: {}", pendingExams.size(),
                        pendingExams);

                for (Long examId : pendingExams) {
                    try {
                        computationService.calculateExamIndicators(examId);
                        log.info("Successfully automatically computed indicators for examId: {}", examId);
                    } catch (Exception e) {
                        log.error("Failed to automatically compute indicators for examId: {}", examId, e);
                    }
                }
            }
        } catch (Exception e) {
            log.error("Error running pending exam metrics calculation task", e);
        }
    }
}
