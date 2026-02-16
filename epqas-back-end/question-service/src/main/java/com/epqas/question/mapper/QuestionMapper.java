package com.epqas.question.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.epqas.question.entity.Question;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface QuestionMapper extends BaseMapper<Question> {
}
