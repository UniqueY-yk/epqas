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
import com.epqas.common.feign.UserFeignClient;
import com.epqas.common.entity.User;
import com.epqas.common.result.Result;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import com.epqas.common.dto.QuestionBatchDTO;
import com.epqas.common.feign.QuestionFeignClient;


@Service
public class ExaminationPaperServiceImpl extends ServiceImpl<ExaminationPaperMapper, ExaminationPaper>
        implements ExaminationPaperService {

    @Autowired
    private ExaminationPaperQuestionMapper paperQuestionMapper;

    @Autowired
    private UserFeignClient userFeignClient;

    @Autowired
    private QuestionFeignClient questionFeignClient;

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

        // Populate setter name
        try {
            if (paper.getSetterId() != null) {
                Result<User> userResult = userFeignClient.getUserById(1, paper.getSetterId());
                if (userResult.getCode() == 200 && userResult.getData() != null) {
                    dto.setSetterName(userResult.getData().getRealName());
                }
            }
        } catch (Exception e) {
            // Ignore
        }

        LambdaQueryWrapper<ExaminationPaperQuestion> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ExaminationPaperQuestion::getPaperId, paperId);
        queryWrapper.orderByAsc(ExaminationPaperQuestion::getQuestionOrder);
        List<ExaminationPaperQuestion> paperQuestions = paperQuestionMapper.selectList(queryWrapper);

        List<PaperQuestionDTO> questionDtos = new ArrayList<>();
        List<Long> questionIds = new ArrayList<>();
        for (ExaminationPaperQuestion pq : paperQuestions) {
            PaperQuestionDTO pqDto = new PaperQuestionDTO();
            pqDto.setQuestionId(pq.getQuestionId());
            pqDto.setScoreValue(pq.getScoreValue());
            pqDto.setQuestionOrder(pq.getQuestionOrder());
            questionDtos.add(pqDto);
            questionIds.add(pq.getQuestionId());
        }

        // Enrich with question details
        if (!questionIds.isEmpty()) {
            try {
                Result<List<QuestionBatchDTO>> questionResult = questionFeignClient.getQuestionsByIds(questionIds);
                if (questionResult.getCode() == 200 && questionResult.getData() != null) {
                    Map<Long, QuestionBatchDTO> qMap = questionResult.getData().stream()
                            .collect(Collectors.toMap(QuestionBatchDTO::getQuestionId, q -> q));
                    
                    for (PaperQuestionDTO pqDto : questionDtos) {
                        QuestionBatchDTO qDetail = qMap.get(pqDto.getQuestionId());
                        if (qDetail != null) {
                            pqDto.setQuestionContent(qDetail.getQuestionContent());
                            pqDto.setQuestionType(qDetail.getQuestionType());
                            pqDto.setOptionsJson(qDetail.getOptionsJson());
                            pqDto.setCorrectAnswer(qDetail.getCorrectAnswer());
                        }
                    }
                }
            } catch (Exception e) {
                // Log error or ignore if question-service is down
            }
        }

        dto.setQuestions(questionDtos);
        return dto;
    }

    @Override
    public Page<ExaminationPaperDTO> getPaperPage(Integer current, Integer size, Integer courseId, String keyword) {
        Page<ExaminationPaper> page = new Page<>(current, size);
        LambdaQueryWrapper<ExaminationPaper> queryWrapper = new LambdaQueryWrapper<>();

        if (courseId != null) {
            queryWrapper.eq(ExaminationPaper::getCourseId, courseId);
        }

        if (keyword != null && !keyword.isEmpty()) {
            queryWrapper.like(ExaminationPaper::getTitle, keyword);
        }

        queryWrapper.orderByDesc(ExaminationPaper::getCreatedAt);

        Page<ExaminationPaper> resultPage = this.page(page, queryWrapper);

        Page<ExaminationPaperDTO> dtoPage = new Page<>(resultPage.getCurrent(), resultPage.getSize(), resultPage.getTotal());
        List<ExaminationPaperDTO> dtoList = new ArrayList<>();

        for (ExaminationPaper paper : resultPage.getRecords()) {
            ExaminationPaperDTO dto = new ExaminationPaperDTO();
            BeanUtils.copyProperties(paper, dto);
            try {
                if (paper.getSetterId() != null) {
                    Result<User> userResult = userFeignClient.getUserById(1, paper.getSetterId());
                    if (userResult.getCode() == 200 && userResult.getData() != null) {
                        dto.setSetterName(userResult.getData().getRealName());
                    }
                }
            } catch (Exception e) {
                // Safe ignore if feign fails for a user
            }
            dtoList.add(dto);
        }
        
        dtoPage.setRecords(dtoList);
        return dtoPage;
    }
}
