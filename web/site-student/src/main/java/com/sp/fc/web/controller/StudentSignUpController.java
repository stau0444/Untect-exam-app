package com.sp.fc.web.controller;

import com.sp.fc.user.domain.Authority;
import com.sp.fc.user.domain.School;
import com.sp.fc.user.domain.User;
import com.sp.fc.user.service.SchoolService;
import com.sp.fc.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;
import java.util.Optional;

@Controller
@RequiredArgsConstructor
public class StudentSignUpController {

    private final SchoolService schoolService;
    private final UserService userService;
    private final PasswordEncoder passwordEncoder;

    @GetMapping("/signup/study")
    public String studentSignUpView(Model model){
        model.addAttribute("cityList",schoolService.cities());
        return "/study/signup";
    }

    @GetMapping("/teachers")
    @ResponseBody
    public List<User> teachers(@RequestParam Long schoolId){
        System.out.println("schoolId = " + schoolId);
        return userService.findBySchoolTeacherList(schoolId);
    }

    @PostMapping("/signUp/study")
    public String studentSignUp(StudentSignUpData studentSignUpData,Model model){

        School school = schoolService.findById(studentSignUpData.getSchoolId());
        User teacher = userService.findUser(studentSignUpData.getTeacherId()).orElseThrow(()->new IllegalArgumentException("등록되지 않은 선생님입니다."));
        User student = User.builder()
                .school(school)
                .teacher(teacher)
                .enabled(true)
                .grade(studentSignUpData.getGrade())
                .name(studentSignUpData.getName())
                .email(studentSignUpData.getEmail())
                .password(passwordEncoder.encode(studentSignUpData.getPassword()))
                .build();
        User savedStudent = userService.save(student);
        userService.addAuthority(savedStudent.getId(), Authority.ROLE_STUDENT);

        model.addAttribute("site", "study");
        return "loginForm.html";
    }
}
