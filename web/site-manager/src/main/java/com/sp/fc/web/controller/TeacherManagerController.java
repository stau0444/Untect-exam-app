package com.sp.fc.web.controller;

import com.sp.fc.user.domain.User;
import com.sp.fc.user.service.SchoolService;
import com.sp.fc.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;

@Controller
@RequestMapping("/manager/teacher")
@RequiredArgsConstructor
public class TeacherManagerController {

    private final SchoolService schoolService;
    private final UserService userService;

    @GetMapping("/list")
    public String teacherList(
            Model model,
            @RequestParam(value = "pageNum",defaultValue = "1") Integer pageNum,
            @RequestParam(value = "size",defaultValue = "10") Integer size
    ){
        Page<TeacherData> teacherList = userService.findTeacherListPaging(pageNum, size)
                .map(
                        teacher ->
                                new TeacherData(
                                        teacher.getId(),
                                        teacher.getSchool().getName(),
                                        teacher.getUsername(),
                                        teacher.getEmail(),
                                        userService.countStudentByTeacher(teacher.getId())
                                )
                );

        model.addAttribute("menu","teacher");
        model.addAttribute("paging", true);
        model.addAttribute("page",teacherList);

        return "/manager/teacher/list";
    }

}
