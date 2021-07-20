package com.sp.fc.paper.service;

import com.sp.fc.paper.domain.PaperTemplate;
import com.sp.fc.paper.domain.Problem;
import com.sp.fc.paper.repository.PaperAnswerRepository;
import com.sp.fc.paper.repository.PaperRepository;
import com.sp.fc.paper.service.helper.PaperTestCommon;
import com.sp.fc.user.domain.User;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

/**
 *  Paper 설명
 *   페이퍼는 학생이 답을 적어 제출한 시험지를 의미한다 .
 *   페이퍼와 페이퍼 템플릿은 1대1의 관계이
 *   페이퍼 템플릿 -> 페이퍼 방향으로만 연결되어 있다 . (Paper에서 paperTemplate는 참조불가)
 *   페이퍼는 학생유저 아이디를 가지고 있다.
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


    private User student1;
    private User student2;

    private PaperTemplate paperTemplate;

    private PaperService paperService;

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
}