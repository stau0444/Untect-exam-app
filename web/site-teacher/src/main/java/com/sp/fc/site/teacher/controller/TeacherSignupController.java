package com.sp.fc.site.teacher.controller;

import com.sp.fc.site.teacher.dto.SignUpData;
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

@Controller
@RequiredArgsConstructor
public class TeacherSignupController {

    private final SchoolService schoolService;
    private final UserService userService;
    private final PasswordEncoder encoder;

    @ResponseBody
    @GetMapping(value="/schools")
    public List<School> getSchoolList(@RequestParam(value="city", required = true) String city){
        return schoolService.findAllByCity(city);
    }

    @GetMapping("/signup/teacher")
    public String signUpForm(Model model){
        model.addAttribute("site","study");
        model.addAttribute("cityList",schoolService.cities());
        return "/teacher/signup";
    }

    @PostMapping("/signup/teacher")
    public String signUp(SignUpData signUpData, Model model){
        School school = schoolService.findById(signUpData.getSchoolId());
        User newTeacher = userService.save(User.builder()
                .name(signUpData.getName())
                .school(school)
                .email(signUpData.getEmail())
                .password(encoder.encode(signUpData.getPassword()))
                .enabled(true)
                .build());
        userService.addAuthority(newTeacher.getId(), Authority.ROLE_TEACHER);
        model.addAttribute("site", "teacher");

        return "loginForm";
    }
}
