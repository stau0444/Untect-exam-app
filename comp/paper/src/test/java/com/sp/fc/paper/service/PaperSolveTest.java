package com.sp.fc.paper.service;

import com.sp.fc.paper.domain.Paper;
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
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
public class PaperSolveTest extends PaperTestCommon {

    @Autowired
    private PaperRepository paperRepository;

    @Autowired
    private PaperAnswerRepository paperAnswerRepository;

    private PaperService paperService;

    private PaperTemplate paperTemplate;

    private User student1;

    private User student2;


    private Problem problem1;

    private Problem problem2;

    private Problem problem3;
    private Problem problem4;

    @BeforeEach
    public void before(){
        paperRepository.deleteAll();;
        paperTemplateRepository.deleteAll();
        preparePaperTemplate();
        this.paperService = new PaperService(userRepository,paperTemplateRepository,paperRepository,paperAnswerRepository,paperTemplateService);
        this.paperTemplate = paperTemplateTestHelper.createPaperTemplate(teacher,"영어시험");
        this.student1 = userTesthelper.saveStudent("student1","111","1",school,teacher);
        this.student2 = userTesthelper.saveStudent("student2","111","1",school,teacher);
        this.problem1 = paperTemplateTestHelper.addProblem(paperTemplate.getId(),"정답1","문제1");
        this.problem2 = paperTemplateTestHelper.addProblem(paperTemplate.getId(), "정답2", "문제2");
        this.problem3 = paperTemplateTestHelper.addProblem(paperTemplate.getId(),"정답3","문제3");
        this.problem4 = paperTemplateTestHelper.addProblem(paperTemplate.getId(),"정답4","문제4");

    }

    @DisplayName("1.시험지 생성")
    @Test
    void test_1(){
        PaperTemplate template = paperTemplateRepository.findById(paperTemplate.getId()).get();

        assertEquals("영어시험",template.getName());

    }

    @DisplayName("2.학생 1, 2 에게 시험 배포")
    @Test
    void test_2(){
        List<Paper> papers = paperService.publishPaper(paperTemplate.getId(), List.of(student1.getId(), student2.getId()));

        assertEquals(student1.getId(), papers.get(0).getStudentUserId());
        assertEquals(student2.getId(), papers.get(1).getStudentUserId());

    }

    @DisplayName("3.학생1이 시험지를 푼다")
    @Test
    void test_3(){
        List<Paper> papers = paperService.publishPaper(paperTemplate.getId(), List.of(student1.getId(), student2.getId()));
        Paper paper = papers.get(0);
        paperService.answer(paper.getId(),problem1.getId(),problem1.getIndexNum(),"정답1");
        paperService.answer(paper.getId(),problem2.getId(),problem2.getIndexNum(),"정답2");

        assertEquals(2 , papers.get(0).getAnswered());
    }


    @DisplayName("4.학생1이 본 시험을 체점한다 - 100점")
    @Test
    void test_4(){
        List<Paper> papers = paperService.publishPaper(paperTemplate.getId(), List.of(student1.getId(), student2.getId()));
        Paper paper = papers.get(0);
        paperService.answer(paper.getId(),problem1.getId(),problem1.getIndexNum(),"정답1");
        paperService.answer(paper.getId(),problem2.getId(),problem2.getIndexNum(),"정답2");
        paperService.answer(paper.getId(),problem3.getId(),problem3.getIndexNum(),"정답3");
        paperService.answer(paper.getId(),problem4.getId(),problem4.getIndexNum(),"정답4");

        paperService.paperDone(paper.getId());

        assertEquals(100D,paper.getScore());
        assertEquals(4,paper.getCorrect());
        assertEquals(Paper.PaperState.END,paper.getState());
        assertNotNull(paper.getEndTime());
    }

    @DisplayName("5.학생1이 본 시험을 체점한다 - 50점")
    @Test
    void test_5(){
        List<Paper> papers = paperService.publishPaper(paperTemplate.getId(), List.of(student1.getId(), student2.getId()));
        Paper paper = papers.get(0);
        paperService.answer(paper.getId(),problem1.getId(),problem1.getIndexNum(),"정답1");
        paperService.answer(paper.getId(),problem2.getId(),problem2.getIndexNum(),"정답2");
        paperService.answer(paper.getId(),problem3.getId(),problem3.getIndexNum(),"오답1");
        paperService.answer(paper.getId(),problem4.getId(),problem4.getIndexNum(),"오답2");

        paperService.paperDone(paper.getId());

        assertEquals(50D,paper.getScore());
        assertEquals(2,paper.getCorrect());
        assertEquals(Paper.PaperState.END,paper.getState());
        assertNotNull(paper.getEndTime());
    }

    @DisplayName("6.학생1이 본 시험을 체점한다 - 시험지를 풀지 않는다")
    @Test
    void test_6(){
        List<Paper> papers = paperService.publishPaper(paperTemplate.getId(), List.of(student1.getId(), student2.getId()));
        Paper paper = papers.get(0);

        paperService.paperDone(paper.getId());

        assertEquals(0,paper.getScore());
        assertEquals(0,paper.getCorrect());
        assertEquals(Paper.PaperState.END,paper.getState());
        assertNotNull(paper.getEndTime());
        assertEquals(0,paper.getAnswered());
    }

    @DisplayName("7.학생1 이 시험을 전부 틀린다 ")
    @Test
    void test_7(){
        List<Paper> papers = paperService.publishPaper(paperTemplate.getId(), List.of(student1.getId(), student2.getId()));
        Paper paper = papers.get(0);

        paperService.answer(paper.getId(), problem1.getId(),problem1.getIndexNum(),"오답1");
        paperService.answer(paper.getId(), problem2.getId(),problem2.getIndexNum(),"오답1");
        paperService.answer(paper.getId(), problem3.getId(),problem3.getIndexNum(),"오답1");
        paperService.answer(paper.getId(), problem4.getId(),problem4.getIndexNum(),"오답1");

        paperService.paperDone(paper.getId());

        assertEquals(0,paper.getScore());
        assertEquals(0,paper.getCorrect());
        assertEquals(Paper.PaperState.END,paper.getState());
        assertNotNull(paper.getEndTime());
        assertEquals(4,paper.getAnswered());
    }
}
