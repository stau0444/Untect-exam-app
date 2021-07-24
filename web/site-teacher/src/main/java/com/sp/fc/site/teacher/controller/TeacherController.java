package com.sp.fc.site.teacher.controller;

import com.sp.fc.paper.domain.Paper;
import com.sp.fc.paper.domain.PaperTemplate;
import com.sp.fc.paper.domain.Problem;
import com.sp.fc.paper.service.PaperService;
import com.sp.fc.paper.service.PaperTemplateService;
import com.sp.fc.paper.service.ProblemService;
import com.sp.fc.site.teacher.dto.ProblemData;
import com.sp.fc.user.domain.User;
import com.sp.fc.user.repository.UserRepository;
import com.sp.fc.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/teacher")
@RequiredArgsConstructor
public class TeacherController {

    private final PaperTemplateService paperTemplateService;
    private final UserService userService;
    private final PaperService paperService;

    @PreAuthorize("hasAuthority('ROLE_TEACHER')")
    @GetMapping({"/",""})
    public String teacherHome(@AuthenticationPrincipal User teacher, Model model){

        model.addAttribute("studentCount", userService.findTeacherStudentCount(teacher.getId()));
        model.addAttribute("paperTemplateCount",paperTemplateService.countByUserId(teacher.getId()));

        return "/teacher/index";
    }

    @GetMapping("/study/list")
    public String studentList(@AuthenticationPrincipal User teacher, Model model){
        model.addAttribute("menu","student");
        model.addAttribute("studyList",userService.findStudentsByTeacher(teacher.getId()));
        return "/teacher/study/list";
    }

    @GetMapping("/paperTemplate/list")
    public String paperTemplateList(@AuthenticationPrincipal User teacher ,
                                    Model model,
                                    @RequestParam(value = "pageNum",defaultValue = "1") Integer pageNum,
                                    @RequestParam(value = "size",defaultValue = "10") Integer size
                                    ){

        model.addAttribute("menu", "paper");
        Page<PaperTemplate> page = paperTemplateService.findByTeacherId(teacher.getId(), pageNum, size);
        model.addAttribute("page",page);
        return "/teacher/paperTemplate/list";
    }

    @GetMapping("/paperTemplate/list1")
    @ResponseBody
    public Page<PaperTemplate> paperTemplateList1(@AuthenticationPrincipal User teacher ,
                                    Model model,
                                    @RequestParam(value = "pageNum",defaultValue = "1") Integer pageNum,
                                    @RequestParam(value = "size",defaultValue = "10") Integer size
    ){

        model.addAttribute("menu", "paper");
        Page<PaperTemplate> page = paperTemplateService.findByTeacherId(teacher.getId(), pageNum, size)
                .map(template->{
                    return template;
                });
        List<PaperTemplate> content = page.getContent();

        model.addAttribute("page",page);
        return page;
    }

    @GetMapping("/paperTemplate/create")
    public String createPaperTemplateView(){
        return "/teacher/paperTemplate/create";
    }

    @PostMapping(value = "/paperTemplate/create",consumes = {"application/x-www-form-urlencoded;charset=UTF-8", MediaType.APPLICATION_FORM_URLENCODED_VALUE})
    public String createPaperTemplate(@AuthenticationPrincipal User teacher ,
                                      @RequestParam String paperName ,
                                      Model model
    ){
        PaperTemplate template = paperTemplateService.save(
                PaperTemplate.builder()
                        .creator(teacher)
                        .name(paperName)
                        .userId(teacher.getId())
                        .build()
        );
        model.addAttribute("template",template);
        return "/teacher/paperTemplate/edit";
    }


    @PostMapping("/paperTemplate/problem/add")
    public String addProblemToTemplate(@AuthenticationPrincipal User teacher,
                                       ProblemData problemData,
                                       Model model){
        paperTemplateService.addProblem(
                problemData.getPaperTemplateId(),
                Problem.builder()
                        .templateId(problemData.getPaperTemplateId())
                        .content(problemData.getContent())
                        .answer(problemData.getAnswer())
                        .build());

        PaperTemplate template = paperTemplateService.findProblemTemplate(problemData.getPaperTemplateId()).orElseThrow(() -> new IllegalArgumentException("시험지 없음"));
        model.addAttribute("template",template);
        return "/teacher/paperTemplate/edit";
    }
    @GetMapping("/paperTemplate/edit")
    public String updateTemplate(@RequestParam Long id,
                                 Model model){
        PaperTemplate template = paperTemplateService.findProblemTemplate(id).orElseThrow(() -> new IllegalArgumentException("해당 템플릿이 없습니다"));
        model.addAttribute("template",template);
        return "/teacher/paperTemplate/edit";
    }

    @GetMapping("/paperTemplate/publish")
    public String publishTemplate(@RequestParam Long id, @AuthenticationPrincipal User teacher){

        List<Long> studentIds = userService.findStudentsByTeacher(teacher.getId()).stream()
                .map(s -> { return s.getId(); }).collect(Collectors.toList());

        paperService.publishPaper(id, studentIds);
        return "redirect:/teacher/paperTemplate/list";
    }

    @GetMapping("/study/results")
    public String paperResults(@RequestParam Long id , @AuthenticationPrincipal User teacher, Model model){

        model.addAttribute("menu", "paper");

        return paperTemplateService.findProblemTemplate(id).map(paperTemplate -> {
                List<Paper> papers = paperService.getPapers(id);
                Map<Long, User> userMap = userService.getUsers(papers.stream().map(p -> p.getStudentUserId()).collect(Collectors.toList()));
                papers.stream().forEach(p->p.setUser(userMap.get(p.getStudentUserId())));
                model.addAttribute("template",paperTemplateService.findById(id).get());
                model.addAttribute("papers",paperService.getPapers(id));
                return "/teacher/study/results";
        }).orElseThrow(()->new IllegalArgumentException("템플릿이 존재하지 않음"));

    }
}
