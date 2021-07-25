package com.sp.fc.web.controller;

import com.sp.fc.paper.domain.Paper;
import com.sp.fc.paper.domain.PaperAnswer;
import com.sp.fc.paper.domain.PaperTemplate;
import com.sp.fc.paper.domain.Problem;
import com.sp.fc.paper.service.PaperService;
import com.sp.fc.paper.service.PaperTemplateService;
import com.sp.fc.user.domain.User;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Map;
import java.util.Optional;

@Controller
@RequestMapping("/study")
@RequiredArgsConstructor
public class StudentController {

    private final PaperService paperService;
    private final PaperTemplateService paperTemplateService;

    @PreAuthorize("hasAuthority('ROLE_STUDENT')")
    @GetMapping(value = {"","/"})
    public String studentHome(@AuthenticationPrincipal User student, Model model){
        model.addAttribute("paperCount",paperService.countPapersByUserIng(student.getId()));
        model.addAttribute("resultCount", paperService.countPapersByUserResult(student.getId()));
        return "/study/index";
    }

    @GetMapping("/papers")
    public String papers(@AuthenticationPrincipal User student , Model model){
        model.addAttribute("papers",paperService.getPapersByUserIng(student.getId()));
        return "/study/paper/papers";
    }

    @GetMapping("/paper/apply")
    public String applyPaper(@AuthenticationPrincipal User student , Model model , @RequestParam Long paperId){
        model.addAttribute("menu", "paper");
        Paper paper = paperService.findPaper(paperId).orElseThrow(()->new IllegalArgumentException("시험지가 존재하지 않습니다"));
        if (paper.getState() == Paper.PaperState.END){
            return "redirect:/study/paper/result?paperId="+paperId;
        }

        Map<Integer, PaperAnswer> answerMap = paper.answerMap();

        PaperTemplate template = paperTemplateService.findById(paper.getPaperTemplateId()).orElseThrow(()->new IllegalArgumentException("시험지가 없습니다"));
        Optional<Problem> notAnswered = template.getProblemList().stream().filter(problem -> !answerMap.containsKey(problem.getIndexNum())).findFirst();


        model.addAttribute("paperId",paperId);
        model.addAttribute("paperName",paper.getName());
        if(notAnswered.isPresent()){
            model.addAttribute("problem",notAnswered.get());
            model.addAttribute("allDone",false);
        }else{
            model.addAttribute("allDone",true);
        }

        return "/study/paper/apply";
    }

    @PostMapping("/paper/answer")
    public String answer(AnswerData answerData){
        paperService
                .answer(
                        answerData.getPaperId(),
                        answerData.getProblemId(),
                        answerData.getIndexNum(),
                        answerData.getAnswer()
                );
        return "redirect:/study/paper/apply?paperId="+answerData.getPaperId();
    }
    @GetMapping("/paper/done")
    public String paperDone(@RequestParam Long paperId){
        System.out.println("paperId = " + paperId);
        paperService.paperDone(paperId);
        return "redirect:/study/paper/result?paperId="+paperId;
    }

    @GetMapping("/paper/result")
    public String paperResult(@RequestParam Long paperId, Model model){
        model.addAttribute("paper",paperService.findPaper(paperId).get());
        return "/study/paper/result";
    }

    @GetMapping("/results")
    public String resultList(
            @AuthenticationPrincipal User student,
                                     Model model,
                        @RequestParam(value = "pageNum" , defaultValue = "1") Integer pageNum,
                        @RequestParam(value = "size" , defaultValue = "2") Integer size
            ){

        Page<Paper> papersByUserResult = paperService.getPapersByUserResult(student.getId(), pageNum, size);
        for (Paper paper : papersByUserResult.getContent()) {
            System.out.println("paper = " + paper);
        }
        model.addAttribute("page",papersByUserResult);
        model.addAttribute("paging",true);

        return "/study/paper/results";
    }
}
