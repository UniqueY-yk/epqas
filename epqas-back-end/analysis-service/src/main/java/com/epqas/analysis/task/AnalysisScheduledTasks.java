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
     * 定期检查是否有已生成学生成绩但未生成质量分析的试卷。
     * 演示目的：每1分钟（60,000毫秒）运行一次。
     * 在生产环境中，这可能会每天运行或在评分阶段后运行。
     */
    @Scheduled(fixedDelay = 60000)
    public void calculatePendingPaperMetrics() {
        try {
            List<Long> pendingPapers = computeMapper.findPendingPaperIdsForAnalysis();
            if (pendingPapers != null && !pendingPapers.isEmpty()) {
                log.info("Found {} pending papers requiring indicator calculation: {}", pendingPapers.size(),
                        pendingPapers);

                for (Long paperId : pendingPapers) {
                    try {
                        computationService.calculatePaperIndicators(paperId);
                        log.info("Successfully automatically computed indicators for paperId: {}", paperId);
                    } catch (Exception e) {
                        log.error("Failed to automatically compute indicators for paperId: {}", paperId, e);
                    }
                }
            }
        } catch (Exception e) {
            log.error("Error running pending paper metrics calculation task", e);
        }
    }
}
