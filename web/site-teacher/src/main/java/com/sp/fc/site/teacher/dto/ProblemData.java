package com.sp.fc.site.teacher.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ProblemData {

    private Long paperTemplateId;
    private String content;
    private String answer;
}
