package com.epqas.exam.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.epqas.exam.dto.ExaminationPaperDTO;
import com.epqas.exam.entity.ExaminationPaper;

public interface ExaminationPaperService extends IService<ExaminationPaper> {

    void createPaperWithQuestions(ExaminationPaperDTO dto);

    void updatePaperWithQuestions(ExaminationPaperDTO dto);

    ExaminationPaperDTO getPaperWithQuestions(Long paperId);

    Page<ExaminationPaperDTO> getPaperPage(Integer current, Integer size, Integer courseId, String keyword);
}
