package com.sp.fc.web.controller;


import com.sp.fc.user.domain.Authority;
import com.sp.fc.user.domain.School;
import com.sp.fc.user.domain.User;
import com.sp.fc.user.repository.UserRepository;
import com.sp.fc.user.service.SchoolService;
import com.sp.fc.user.service.UserService;
import com.sp.fc.web.dto.UpdateSchoolData;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.lang.Nullable;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@Controller
@RequestMapping("/manager/school")
@RequiredArgsConstructor
public class SchoolManagerController {

    private final SchoolService schoolService;
    private final UserService userService;

    @GetMapping("/list")
    public String schoolList(
                        @AuthenticationPrincipal User teacher,
                        Model model,
                        @RequestParam(value = "pageNum",defaultValue = "1") Integer pageNum,
                        @RequestParam(value = "size",defaultValue = "10") Integer size

    ){
        Page<School> schoolList = schoolService.getSchoolList(pageNum, size);
        userService.setSchoolUserCount(schoolList);
        model.addAttribute("paging", true);
        model.addAttribute("page",schoolService.getSchoolList(pageNum,size));
        model.addAttribute("menu","school");
        return "/manager/school/list";
    }


    @GetMapping("/edit")
    public String updateSchoolInfoView(@RequestParam @Nullable Long schoolId, Model model){

        model.addAttribute("menu", "school");

        if(schoolId == null) {
            model.addAttribute("school", School.builder().id(-2L).build());
            return "/manager/school/edit";
        }
        model.addAttribute("school",schoolService.findById(schoolId));
        return "/manager/school/edit";
    }

    @PostMapping("/save")
    public String updateOrSaveSchoolInfo(UpdateSchoolData updateSchoolData){
        Optional<School> school = schoolService.findByIdOp(updateSchoolData.getSchoolId());
        school.ifPresentOrElse(s->
            schoolService.updateName(
                    updateSchoolData.getSchoolId(),
                    updateSchoolData.getName(),
                    updateSchoolData.getCity()
            )
        ,
                ()->schoolService.save(
                School.builder()
                        .city(updateSchoolData.getCity())
                        .name(updateSchoolData.getName())
                        .build()
        ));
        return "redirect:/manager/school/list";
    }
}
