package com.sp.fc.web.controller;

import com.sp.fc.user.domain.School;
import com.sp.fc.user.service.SchoolService;
import com.sp.fc.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/manager")
@RequiredArgsConstructor
public class AdminController {

    private final SchoolService schoolService;
    private final UserService userService;


    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @GetMapping(value = {"/",""})
    public String home(Model model){
        model.addAttribute("schoolCount",schoolService.countSchool());
        model.addAttribute("studyCount", userService.countStudent());
        model.addAttribute("teacherCount",userService.countTeacher());

        return "/manager/index";
    }

}
