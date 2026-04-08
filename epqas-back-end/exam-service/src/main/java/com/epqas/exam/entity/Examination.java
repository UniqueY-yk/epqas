package com.epqas.exam.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "examination")
@TableName("examination")
public class Examination {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @TableId(type = IdType.AUTO)
    private Long examId; // 考试ID

    private Long paperId; // 试卷ID

    private Integer classId; // 班级ID

    private LocalDateTime examDate; // 考试日期

    private Integer totalCandidates; // 考生总数

    private Integer actualExaminees; // 实际参考人数
}
