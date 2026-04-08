package com.epqas.exam.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.epqas.exam.dto.ExaminationPaperDTO;
import com.epqas.exam.entity.ExaminationPaper;

public interface ExaminationPaperService extends IService<ExaminationPaper> {
    /**
     * 创建试卷
     * @param dto 试卷信息
     */
    void createPaperWithQuestions(ExaminationPaperDTO dto);
    /**
     * 更新试卷
     * @param dto 试卷信息
     */
    void updatePaperWithQuestions(ExaminationPaperDTO dto);
    /**
     * 获取试卷详情
     * @param paperId 试卷ID
     * @return 试卷详情
     */
    ExaminationPaperDTO getPaperWithQuestions(Long paperId);
    
    Page<ExaminationPaperDTO> getPaperPage(Integer current, Integer size, Integer courseId, String keyword);
}
