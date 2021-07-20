package com.sp.fc.paper.service.helper;

import com.sp.fc.paper.repository.PaperTemplateRepository;
import com.sp.fc.paper.repository.ProblemRepository;
import com.sp.fc.paper.service.PaperTemplateService;
import com.sp.fc.paper.service.ProblemService;
import com.sp.fc.user.domain.User;
import com.sp.fc.user.service.helper.UserTestCommon;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;


public class PaperTestCommon extends UserTestCommon {

    @Autowired
    protected PaperTemplateRepository paperTemplateRepository;

    @Autowired
    protected ProblemRepository problemRepository;

    protected PaperTemplateService paperTemplateService;
    protected PaperTemplateTestHelper paperTemplateTestHelper;
    protected ProblemService problemService;
    protected User teacher;

    protected void preparePaperTemplate(){
        this.problemRepository.deleteAll();
        this.paperTemplateRepository.deleteAll();
        setService();

        this.paperTemplateTestHelper = new PaperTemplateTestHelper(this.paperTemplateService);
        this.problemService = new ProblemService(problemRepository);
        this.paperTemplateService = new PaperTemplateService(paperTemplateRepository,problemService);
        this.teacher = this.userTesthelper.saveTeacher("teacher1" , "123","1",school);

    }

}
