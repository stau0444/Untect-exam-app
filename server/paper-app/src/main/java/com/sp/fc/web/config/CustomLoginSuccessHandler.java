package com.sp.fc.web.config;

import org.apache.juli.logging.Log;
import org.apache.juli.logging.LogFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.DefaultRedirectStrategy;
import org.springframework.security.web.RedirectStrategy;
import org.springframework.security.web.WebAttributes;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.savedrequest.HttpSessionRequestCache;
import org.springframework.security.web.savedrequest.RequestCache;
import org.springframework.security.web.savedrequest.SavedRequest;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;

public class CustomLoginSuccessHandler implements AuthenticationSuccessHandler {

    //이미 요청된적이 있는 request를 캐시에 저장해 놓고 다음번 요청에서 해당 request 객체를 반환한다.
    private RequestCache requestCache = new HttpSessionRequestCache();
    //스프링에서 사용되는 모든 리다이렉션 전략을 캡슐화한 인터페이스
    private RedirectStrategy redirectStrategy = new DefaultRedirectStrategy();
    protected Log logger = LogFactory.getLog(this.getClass());


    //로그인 성공시에 수행될 로직을 정의하는 메서드
    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,Authentication authentication) throws IOException, ServletException {
        handle(request,response,requestCache.getRequest(request,response),authentication);
        clearAuthenticationAttributes(request);
    }

    //요청이 끝난 후에 에러 세션을 지운다 .
    protected void clearAuthenticationAttributes(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if(session == null){
            return;
        }
        session.removeAttribute(WebAttributes.AUTHENTICATION_EXCEPTION);
    }

    protected void handle(
            HttpServletRequest request,
            HttpServletResponse response,
            SavedRequest savedRequest,
            Authentication authentication) throws IOException {

        String targetUrl = determineTargetUrl(request,savedRequest,authentication);
        if(response.isCommitted()){
            logger.debug("Response has already been committed. Unable to redirect to" + targetUrl);
            return;
        }
        redirectStrategy.sendRedirect(request,response,targetUrl);
    }

    protected String determineTargetUrl(HttpServletRequest request,SavedRequest savedRequest,final Authentication authentication) {
        if(savedRequest != null){
            String redirectUrl = savedRequest.getRedirectUrl();
            if(redirectUrl != null && !redirectUrl.startsWith("/login")){
                return savedRequest.getRedirectUrl();
            }
        }


        if (request.getParameter("site").equals("manager")){
            return  "/manager";
        }else if(request.getParameter("site").equals("study")){
            return "/study";
        }else if(request.getParameter("site").equals("teacher")) {
            return "/teacher";
        }
        return "/";
    }

}
