package com.sp.fc.paper.service.helper;

import com.sp.fc.paper.domain.PaperTemplate;
import com.sp.fc.paper.domain.Problem;
import com.sp.fc.paper.service.PaperTemplateService;
import com.sp.fc.user.domain.User;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class PaperTemplateTestHelper {

    private final PaperTemplateService paperTemplateService;

    public PaperTemplate createPaperTemplate(User teacher , String paperName){
        PaperTemplate template = PaperTemplate.builder()
                .name(paperName)
                .creator(teacher)
                .build();
       return paperTemplateService.save(template);
    }

    public Problem addProblem(Long ptId, String answer , String content){
        return Problem.builder()
                .answer(answer)
                .paperTemplateId(ptId)
                .content(content)
                .build();
    }
}
