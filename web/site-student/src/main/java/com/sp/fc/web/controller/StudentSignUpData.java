package com.sp.fc.web.controller;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class StudentSignUpData {
    private String city;
    private Long schoolId;
    private Long teacherId;
    private String grade;
    private String name;
    private String email;
    private String password;
}
