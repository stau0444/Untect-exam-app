package com.sp.fc.paper.service;

import com.sp.fc.paper.domain.PaperTemplate;
import com.sp.fc.paper.domain.Problem;
import com.sp.fc.paper.repository.PaperAnswerRepository;
import com.sp.fc.paper.repository.PaperRepository;
import com.sp.fc.paper.service.helper.PaperTestCommon;
import com.sp.fc.user.domain.User;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
public class PaperTemplateTest extends PaperTestCommon {

    @Autowired
    private PaperRepository paperRepository;

    @Autowired
    private PaperAnswerRepository paperAnswerRepository;

    PaperService paperService;

    PaperTemplate paperTemplate;

    User student1;
    User student2;

    @BeforeEach
    public void before(){
        preparePaperTemplate();
        this.paperService = new PaperService(userRepository,paperTemplateRepository,paperRepository,paperAnswerRepository,paperTemplateService);
        this.paperTemplate = paperTemplateTestHelper.createPaperTemplate(teacher,"영어시험");
        this.student1 = userTesthelper.saveStudent("student1" , "111","1",school,teacher);
        this.student2 = userTesthelper.saveStudent("student2" , "111","1",school,teacher);


    }

    @DisplayName("1. 템플릿 생성")
    @Test
    void test_1(){
        PaperTemplate template = paperTemplateRepository.findById(paperTemplate.getId()).get();
        assertEquals(teacher.getId() , template.getCreator().getId());
        assertNotNull(template.getCreatedAt());
        assertEquals(0,template.getPublishCount());
    }

    @DisplayName("2.템플릿에 problem 추가")
    @Test
    void test_2(){
        paperTemplateTestHelper.addProblem(paperTemplate.getId(),"정답1","문제1");
        paperTemplateTestHelper.addProblem(paperTemplate.getId(),"정답2","문제2");
        paperTemplateTestHelper.addProblem(paperTemplate.getId(),"정답3","문제3");
        paperTemplateTestHelper.addProblem(paperTemplate.getId(),"정답4","문제4");

        assertEquals(4,paperTemplate.getProblemList().size());
    }

    @DisplayName("3.템플릿이 배포된 수")
    @Test
    void test_3(){
        paperService.publishPaper(paperTemplate.getId(), List.of(student1.getId(),student2.getId()));
        assertEquals(2 , paperTemplate.getPublishCount());
    }

    @DisplayName("4.문제 삭제")
    @Test
    void test_4(){
        Problem problem = paperTemplateTestHelper.addProblem(paperTemplate.getId(), "정답1", "문제1");
        paperTemplateTestHelper.addProblem(paperTemplate.getId(),"정답2","문제2");
        paperTemplateTestHelper.addProblem(paperTemplate.getId(),"정답3","문제3");
        paperTemplateTestHelper.addProblem(paperTemplate.getId(),"정답4","문제4");

        assertEquals(4,paperTemplate.getProblemList().size());

        paperTemplateService.removeProblem(paperTemplate.getId(),problem.getId());

        assertEquals(3,paperTemplate.getProblemList().size());

    }

}
