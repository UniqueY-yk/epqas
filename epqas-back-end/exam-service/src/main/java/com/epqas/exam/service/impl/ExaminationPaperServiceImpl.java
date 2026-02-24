package com.epqas.exam.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.epqas.exam.dto.ExaminationPaperDTO;
import com.epqas.exam.dto.PaperQuestionDTO;
import com.epqas.exam.entity.ExaminationPaper;
import com.epqas.exam.entity.ExaminationPaperQuestion;
import com.epqas.exam.mapper.ExaminationPaperMapper;
import com.epqas.exam.mapper.ExaminationPaperQuestionMapper;
import com.epqas.exam.service.ExaminationPaperService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class ExaminationPaperServiceImpl extends ServiceImpl<ExaminationPaperMapper, ExaminationPaper>
        implements ExaminationPaperService {

    @Autowired
    private ExaminationPaperQuestionMapper paperQuestionMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void createPaperWithQuestions(ExaminationPaperDTO dto) {
        ExaminationPaper paper = new ExaminationPaper();
        BeanUtils.copyProperties(dto, paper);
        paper.setCreatedAt(LocalDateTime.now());

        // Ensure status is at least Draft
        if (paper.getStatus() == null || paper.getStatus().isEmpty()) {
            paper.setStatus("Draft");
        }

        this.save(paper);

        // Save questions
        if (dto.getQuestions() != null && !dto.getQuestions().isEmpty()) {
            for (PaperQuestionDTO pqDto : dto.getQuestions()) {
                ExaminationPaperQuestion epq = new ExaminationPaperQuestion();
                epq.setPaperId(paper.getPaperId());
                epq.setQuestionId(pqDto.getQuestionId());
                epq.setScoreValue(pqDto.getScoreValue());
                epq.setQuestionOrder(pqDto.getQuestionOrder());
                paperQuestionMapper.insert(epq);
            }
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updatePaperWithQuestions(ExaminationPaperDTO dto) {
        ExaminationPaper paper = new ExaminationPaper();
        BeanUtils.copyProperties(dto, paper);
        this.updateById(paper);

        // Delete old question mappings
        LambdaQueryWrapper<ExaminationPaperQuestion> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ExaminationPaperQuestion::getPaperId, paper.getPaperId());
        paperQuestionMapper.delete(queryWrapper);

        // Insert new ones
        if (dto.getQuestions() != null && !dto.getQuestions().isEmpty()) {
            for (PaperQuestionDTO pqDto : dto.getQuestions()) {
                ExaminationPaperQuestion epq = new ExaminationPaperQuestion();
                epq.setPaperId(paper.getPaperId());
                epq.setQuestionId(pqDto.getQuestionId());
                epq.setScoreValue(pqDto.getScoreValue());
                epq.setQuestionOrder(pqDto.getQuestionOrder());
                paperQuestionMapper.insert(epq);
            }
        }
    }

    @Override
    public ExaminationPaperDTO getPaperWithQuestions(Long paperId) {
        ExaminationPaper paper = this.getById(paperId);
        if (paper == null) {
            return null;
        }

        ExaminationPaperDTO dto = new ExaminationPaperDTO();
        BeanUtils.copyProperties(paper, dto);

        LambdaQueryWrapper<ExaminationPaperQuestion> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ExaminationPaperQuestion::getPaperId, paperId);
        queryWrapper.orderByAsc(ExaminationPaperQuestion::getQuestionOrder);
        List<ExaminationPaperQuestion> paperQuestions = paperQuestionMapper.selectList(queryWrapper);

        List<PaperQuestionDTO> questionDtos = new ArrayList<>();
        for (ExaminationPaperQuestion pq : paperQuestions) {
            PaperQuestionDTO pqDto = new PaperQuestionDTO();
            pqDto.setQuestionId(pq.getQuestionId());
            pqDto.setScoreValue(pq.getScoreValue());
            pqDto.setQuestionOrder(pq.getQuestionOrder());
            questionDtos.add(pqDto);
        }

        dto.setQuestions(questionDtos);
        return dto;
    }

    @Override
    public Page<ExaminationPaper> getPaperPage(Integer current, Integer size, Integer courseId, String keyword) {
        Page<ExaminationPaper> page = new Page<>(current, size);
        LambdaQueryWrapper<ExaminationPaper> queryWrapper = new LambdaQueryWrapper<>();

        if (courseId != null) {
            queryWrapper.eq(ExaminationPaper::getCourseId, courseId);
        }

        if (keyword != null && !keyword.isEmpty()) {
            queryWrapper.like(ExaminationPaper::getTitle, keyword);
        }

        queryWrapper.orderByDesc(ExaminationPaper::getCreatedAt);

        return this.page(page, queryWrapper);
    }
}
