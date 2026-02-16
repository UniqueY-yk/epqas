package com.epqas.academic.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.epqas.academic.entity.Student;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface StudentMapper extends BaseMapper<Student> {
}
