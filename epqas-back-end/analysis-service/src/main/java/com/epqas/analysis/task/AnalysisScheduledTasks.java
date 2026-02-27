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
     * Periodically check for exams that have student results but no quality
     * analysis generated.
     * Runs every 1 minute (60,000 milliseconds) for demonstration purposes.
     * In a production environment, this might run daily or post grading phase.
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
