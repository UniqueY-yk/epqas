package com.epqas.academic.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.epqas.academic.entity.Student;
import org.apache.ibatis.annotations.Mapper;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.epqas.academic.vo.StudentVO;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Results;
import org.apache.ibatis.annotations.Result;
import com.epqas.common.handler.EncryptTypeHandler;

@Mapper
public interface StudentMapper extends BaseMapper<Student> {

    @Select("SELECT s.student_id as studentId, s.class_id as classId, u.username, u.real_name as realName, u.email " +
            "FROM student s LEFT JOIN user u ON s.student_id = u.user_id " +
            "ORDER BY s.student_id DESC")
    @Results({
        @Result(column = "username", property = "username", typeHandler = EncryptTypeHandler.class),
        @Result(column = "realName", property = "realName", typeHandler = EncryptTypeHandler.class),
        @Result(column = "email", property = "email", typeHandler = EncryptTypeHandler.class),
        @Result(column = "studentId", property = "studentId"),
        @Result(column = "classId", property = "classId")
    })
    Page<StudentVO> selectStudentVOPage(Page<StudentVO> page);
}
