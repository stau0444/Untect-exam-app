package com.sp.fc.web.config;

import com.sp.fc.paper.domain.Paper;
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

@Component
@RequiredArgsConstructor
public class DBInit implements CommandLineRunner {

    private final UserService userService;
//    private final PaperService paperService;
//    private final PaperTemplateService paperTemplateService;
//    private final PaperAnswerRepository paperAnswerRepository;
//    private final ProblemService problemService;
    private final SchoolRepository schoolRepository;
    private final PasswordEncoder encoder;


    @Override
    public void run(String... args) throws Exception {
        School school = schoolRepository.save(School.builder()
                .city("서울")
                .name("서울학교")
                .build());

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
                .build();
        User saveStudent1 = userService.save(student1);
        userService.addAuthority(saveStudent1.getId(),Authority.ROLE_STUDENT);

        User student2 = User.builder()
                .password(encoder.encode("111"))
                .school(school)
                .email("student2@test.com")
                .username("student2")
                .grade("1")
                .teacher(teacher)
                .build();
        User saveStudent2 = userService.save(student2);
        userService.addAuthority(saveStudent2.getId(),Authority.ROLE_STUDENT);
    }
}
