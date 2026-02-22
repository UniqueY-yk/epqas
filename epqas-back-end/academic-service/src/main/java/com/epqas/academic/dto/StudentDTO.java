package com.epqas.academic.dto;

import lombok.Data;

@Data
public class StudentDTO {

    // User fields
    private Long userId; // For updates
    private String username;
    private String password;
    private String realName;
    private String email;

    // Student fields
    private Integer classId;
    private String studentNumber;
}
