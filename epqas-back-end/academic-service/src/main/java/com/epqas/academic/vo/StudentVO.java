package com.epqas.academic.vo;

import lombok.Data;

@Data
public class StudentVO {
    private Long studentId;
    private Integer classId;
    private String username;
    private String realName;
    private String email;
}
