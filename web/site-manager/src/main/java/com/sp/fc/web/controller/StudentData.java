package com.sp.fc.web.controller;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class StudentData {
    private String schoolName;
    private String grade;
    private String name;
    private String teacherName;
    private String email;
}
