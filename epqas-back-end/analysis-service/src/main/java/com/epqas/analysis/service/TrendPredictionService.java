package com.epqas.analysis.service;

import com.epqas.analysis.dto.TrendPredictionVO;

public interface TrendPredictionService {

    /**
     * Compute trend prediction for the given setter and course,
     * persist the result, and return it.
     *
     * @param setterId the setter (teacher) ID
     * @param courseId the course ID
     * @return prediction result, or null if insufficient data (< 2 records)
     */
    TrendPredictionVO predictTrend(Long setterId, Long courseId);
}
