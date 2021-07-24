package com.sp.fc.paper.service;

import com.sp.fc.paper.domain.Paper;
import com.sp.fc.paper.domain.PaperTemplate;
import com.sp.fc.paper.domain.Problem;
import com.sp.fc.paper.repository.PaperAnswerRepository;
import com.sp.fc.paper.repository.PaperRepository;
import com.sp.fc.paper.service.helper.PaperTestCommon;
import com.sp.fc.user.domain.User;
import com.sp.fc.user.repository.UserRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

/**
 *  Paper 클래스 설명
 *   페이퍼는 학생이 답을 적어 제출한 시험지를 의미한다 .
 *   페이퍼와 페이퍼 템플릿은 1대1의 관계이
 *   페이퍼 템플릿 -> 페이퍼 방향으로만 연결되어 있다 . (Paper에서 paperTemplate는 참조불가)
 *   페이퍼는 학생유저 아이디를 가지고 있다.
 *   페이퍼는 학생이 작성한 답인 PaperAnswer를 리스트로 가지고 있다.
 *   페이퍼의 상태는 READY,START,END,CANCELED 네 가지이고
 *   순서대로 준비됨 , 시작함 , 끝남 , 취소됨을 의미한다 .
 *   페이퍼에는 체점에 관한 필드와 메서드들이 같이 있다 .
 *   //페이퍼 엔서 넘 살펴보기
 *
 *  시나리오
 *
 *      1) 선생님1, 학습자1, 학습자2
 *      2) 시험지 템플릿을 만든다.
 *      3) 템플릿을 가지고 학습자들에게 시험지를 낸다.
 *      4) 시험지 템플릿을 학습자1에게 시험지를 낸다.
 *      5) 시험지를 2명 이상의 user 를 검색해 낸다. (학습자1, 학습자2)
 *      6)  시험지 삭제 기능
 */

@DataJpaTest
class PaperServiceTest extends PaperTestCommon{

    @Autowired
    private PaperRepository paperRepository;
    @Autowired
    private PaperAnswerRepository answerRepository;


    private PaperTemplate paperTemplate;

    private PaperService paperService;

    private User student1;
    private User student2;


    @BeforeEach
    void before(){
        paperRepository.deleteAll();
        preparePaperTemplate();
        this.paperService = new PaperService(userRepository,paperTemplateRepository,paperRepository,answerRepository,paperTemplateService);
        this.student1 = userTesthelper.saveStudent("student1","123","1",school,teacher);
        this.student2 = userTesthelper.saveStudent("student2","123","1",school,teacher);

        this.paperTemplate = this.paperTemplateTestHelper.createPaperTemplate(teacher,"영어시험");

        this.paperTemplateTestHelper.addProblem(this.paperTemplate.getId(),"정답1","문제1입니다");
        this.paperTemplateTestHelper.addProblem(this.paperTemplate.getId(),"정답2","문제2입니다");
    }
    @DisplayName("1.시험지 배포")
    @Test
    void test_1(){
        List<Paper> papers = paperService.publishPaper(paperTemplate.getId(), List.of(student1.getId(), student2.getId()));
        Paper paper = papers.get(0);

        //문제수
        assertEquals(2,paper.getTotal());

        //페이퍼 생성시간
        assertNotNull(paper.getCreatedAt());
        assertEquals("영어시험" , paper.getName());
        //문제 푸는 학생
        assertEquals(student1.getId(),paper.getStudentUserId());
    }
    @DisplayName("2.시험지를 받은 학생불러오기")
    @Test
    void test_2(){
        paperService.publishPaper(paperTemplate.getId(), List.of(student1.getId(), student2.getId()));
        List<Paper> papers = paperService.getPapers(paperTemplate.getId());
        List<Long> studentIds = List.of(student1.getId(), student2.getId());

        assertEquals(2,papers.size());

        assertTrue(papers.stream().allMatch(
                p->studentIds.contains(p.getStudentUserId())
        ));

    }
    @Transactional
    @DisplayName("3.시험지 삭제")
    @Test
    void test_3(){
        List<Paper> papers = paperService.publishPaper(paperTemplate.getId(), List.of(student1.getId(), student2.getId()));
        PaperTemplate template = paperTemplateRepository.findById(papers.get(0).getPaperTemplateId()).get();

        paperService.removePaper(template.getId(),List.of(student1.getId()));
        List<Paper> publishedPapers = paperService.getPapers(template.getId());
        assertEquals(1,publishedPapers.size());
        assertEquals(student2.getId(),publishedPapers.get(0).getStudentUserId());
    }

    @Transactional
    @DisplayName("4.Paper.PaperState에 따른 시험지 검색")
    @Test
    void test_4(){
        PaperTemplate comTemplate = paperTemplateTestHelper.createPaperTemplate(teacher, "컴퓨터시험");
        List<Paper> comExam = paperService.publishPaper(comTemplate.getId(), List.of(student1.getId(), student2.getId()));
        List<Paper> engExam = paperService.publishPaper(paperTemplate.getId(), List.of(student1.getId(), student2.getId()));
        //student1이 컴퓨터시험을 끝낸상태
        comExam.get(0).setState(Paper.PaperState.END);

        //학생1이 끝내지 못한 시험지 갯수
        long ingPaper = paperService.countPapersByUserIng(student1.getId());
        //학생1이 끝낸 시험지 갯수
        long finishedPaper = paperService.countPapersByUserResult(student1.getId());
        //학생 1이 아직 풀고 있는 시험지를 가져옴
        List<Paper> s1papers = paperService.getPapersByUserIng(student1.getId());
        //학생 1이 끝낸 시험지를 가져옴
        Page<Paper> s1papers2 = paperService.getPapersByUserResult(student1.getId(), 1, 10);
        List<Paper> contents = s1papers2.getContent();

        assertEquals(1,s1papers.size());
        assertEquals("컴퓨터시험",contents.get(0).getName());
        assertEquals("영어시험",s1papers.get(0).getName());

        assertEquals(1L,ingPaper);
        assertEquals(1L, finishedPaper);


    }
}