package com.sp.fc.web.contoller;

import com.sp.fc.user.domain.Authority;
import com.sp.fc.user.domain.User;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.web.savedrequest.HttpSessionRequestCache;
import org.springframework.security.web.savedrequest.RequestCache;
import org.springframework.security.web.savedrequest.SavedRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import java.net.MalformedURLException;
import java.net.URL;

@Controller
public class HomeController {

    private RequestCache requestCache = new HttpSessionRequestCache();


    @GetMapping(value = {"/",""})
    public String home(){
        return "index";
    }

    @GetMapping(value = "/login")
    public String login(
            @AuthenticationPrincipal User user,
            @RequestParam(value = "site" , required = false) String site,
            @RequestParam(value = "error" ,defaultValue = "false") Boolean error,
            HttpServletRequest request,
            Model model
            )
    {
        //인증을 마친 사용자가 로그인 페이지에 접속했을 시에는 인증을 따로 거치지않고
        //사용자 권한에 맞는 사이트로 리다일렉트시킨다.
        if(user != null && user.isEnabled()){
            if(user.getAuthorities().contains(Authority.ROLE_ADMIN)){
                return "redirect:/manager";
            }else if(user.getAuthorities().contains(Authority.ROLE_TEACHER)){
                return "redirect:/teacher";
            }else if(user.getAuthorities().contains(Authority.ROLE_STUDENT)){
                return "redirect:/student";
            }
        }

        //주소를 메인 페이지에서의 로그인이 아닌경우 site 는 null일 수 있기 때문에
        //site가 null일 경우의 코드

        if (site == null){
            SavedRequest savedRequest =requestCache.getRequest(request,null);
            if (savedRequest != null){
                site = estimateSite(savedRequest.getRedirectUrl());
            }
        }
        model.addAttribute("error", error);
        model.addAttribute("site",site);
        return "loginForm";
    }

    private String estimateSite(String redirectUrl) {
        if (redirectUrl == null){
           return  "study.html";
        }
        try {
            URL url = new URL(redirectUrl);
            String path  = url.getPath();
            if (path != null){
                if(path.startsWith("/teacher")) return "teacher";
                if(path.startsWith("/study")) return "study";
                if(path.startsWith("/manager")) return "manager";
            }
        }catch (MalformedURLException e){
            e.printStackTrace();
        }
        return "study.html";
    }

    @GetMapping("/access-denied")
    public String accessDenied(){
        return "/AccessDenied";
    }

}
