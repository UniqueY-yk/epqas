package com.epqas.common.feign;

import com.epqas.common.dto.QuestionBatchDTO;
import com.epqas.common.result.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@FeignClient(name = "question-service")
public interface QuestionFeignClient {

    /**
     * 批量获取题目信息
     * 
     * @param ids 题目ID列表
     * @return 题目信息列表
     */
    @GetMapping("/questions/batch")
    Result<List<QuestionBatchDTO>> getQuestionsByIds(@RequestParam("ids") List<Long> ids);
}
