package com.epqas.common.feign;

import com.epqas.common.dto.QuestionBatchDTO;
import com.epqas.common.result.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@FeignClient(name = "question-service")
public interface QuestionFeignClient {

    @GetMapping("/questions/batch")
    Result<List<QuestionBatchDTO>> getQuestionsByIds(@RequestParam("ids") List<Long> ids);
}
