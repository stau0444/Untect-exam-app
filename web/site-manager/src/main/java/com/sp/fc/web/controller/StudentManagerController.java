package com.sp.fc.web.controller;

import com.sp.fc.user.domain.User;
import com.sp.fc.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping("/manager/study")
@RequiredArgsConstructor
public class StudentManagerController {

    private final UserService userService;

    @GetMapping("/list")
    public String list(
            Model model,
            @RequestParam(value = "pageNum",defaultValue = "1") Integer pageNum,
            @RequestParam(value = "size",defaultValue = "10") Integer size
    ){

        Page<StudentData> studentData = userService.findStudentListPaging(pageNum, size)
                .map(
                        s ->
                                new StudentData(
                                        s.getSchool().getName(),
                                        s.getGrade(),
                                        s.getUsername(),
                                        s.getTeacher().getUsername(),
                                        s.getEmail()
                                )
                );
        model.addAttribute("menu","student");
        model.addAttribute("paging", true);
        model.addAttribute("page",studentData);

        return "/manager/study/list";
    }
}
