package com.sp.fc.web.config;

import com.sp.fc.paper.domain.Paper;
import com.sp.fc.paper.domain.PaperTemplate;
import com.sp.fc.paper.domain.Problem;
import com.sp.fc.paper.repository.PaperAnswerRepository;
import com.sp.fc.paper.repository.PaperRepository;
import com.sp.fc.paper.repository.PaperTemplateRepository;
import com.sp.fc.paper.repository.ProblemRepository;
import com.sp.fc.paper.service.PaperService;
import com.sp.fc.paper.service.PaperTemplateService;
import com.sp.fc.paper.service.ProblemService;
import com.sp.fc.user.domain.Authority;
import com.sp.fc.user.domain.School;
import com.sp.fc.user.domain.User;
import com.sp.fc.user.repository.SchoolRepository;
import com.sp.fc.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Component
@RequiredArgsConstructor
@Transactional
public class DBInit implements CommandLineRunner {

    private final UserService userService;
    private final PaperService paperService;
    private final PaperTemplateService paperTemplateService;
    private final PaperAnswerRepository paperAnswerRepository;
    private final ProblemService problemService;
    private final SchoolRepository schoolRepository;
    private final PasswordEncoder encoder;


    @Override
    public void run(String... args) throws Exception {
        School school = schoolRepository.save(School.builder()
                .city("서울")
                .name("서울학교")
                .build());
        School school2 = schoolRepository.save(School.builder()
                .city("서울")
                .name("명동학교")
                .build());

        User admin = User.builder()
                .password(encoder.encode("111"))
                .email("admin@test.com")
                .username("admin1")
                .enabled(true)
                .build();
        User savedAdmin = userService.save(admin);
        userService.addAuthority(savedAdmin.getId(),Authority.ROLE_ADMIN);

        User teacher = User.builder()
                .password(encoder.encode("111"))
                .school(school)
                .email("teacher1@test.com")
                .username("teacher1")
                .grade("1")
                .enabled(true)
                .build();
        User saveTeacher = userService.save(teacher);
        userService.addAuthority(saveTeacher.getId(),Authority.ROLE_TEACHER);

        User student1 = User.builder()
                .password(encoder.encode("111"))
                .school(school)
                .email("student1@test.com")
                .grade("1")
                .username("student1")
                .teacher(teacher)
                .enabled(true)
                .build();
        User saveStudent1 = userService.save(student1);
        userService.addAuthority(saveStudent1.getId(),Authority.ROLE_STUDENT);

        User student2 = User.builder()
                .password(encoder.encode("111"))
                .school(school)
                .email("student2@test.com")
                .username("student2")
                .grade("1")
                .enabled(true)
                .teacher(teacher)
                .build();
        User saveStudent2 = userService.save(student2);
        userService.addAuthority(saveStudent2.getId(),Authority.ROLE_STUDENT);

        PaperTemplate engTemplate = PaperTemplate.builder()
                .creator(teacher)
                .userId(teacher.getId())
                .name("영어시험")
                .build();
        PaperTemplate saveTemplate = paperTemplateService.save(engTemplate);

        Problem problem = Problem.builder()
                .answer("asd")
                .content("asd?")
                .templateId(saveTemplate.getId())
                .build();
        Problem problem2 = Problem.builder()
                .answer("asd2")
                .content("asd2?")
                .templateId(saveTemplate.getId())
                .build();
        paperTemplateService.addProblem(saveTemplate.getId(),problem);
        paperTemplateService.addProblem(saveTemplate.getId(),problem2);


        paperService.publishPaper(saveTemplate.getId(), List.of(saveStudent1.getId(),saveStudent2.getId()));

    }
}
