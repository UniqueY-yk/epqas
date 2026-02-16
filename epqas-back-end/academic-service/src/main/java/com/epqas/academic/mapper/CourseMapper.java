package com.epqas.academic.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.epqas.academic.entity.Course;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface CourseMapper extends BaseMapper<Course> {
}
