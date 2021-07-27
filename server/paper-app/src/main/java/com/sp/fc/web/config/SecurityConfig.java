package com.sp.fc.web.config;

import com.sp.fc.user.domain.Authority;
import com.sp.fc.user.service.UserSecurityService;
import lombok.RequiredArgsConstructor;
import lombok.extern.java.Log;
import org.springframework.boot.autoconfigure.security.servlet.PathRequest;
import org.springframework.boot.web.servlet.ServletListenerRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.userdetails.UserDetailsChecker;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.RememberMeServices;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.authentication.rememberme.TokenBasedRememberMeServices;
import org.springframework.security.web.session.HttpSessionEventPublisher;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSessionEvent;

@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true)
@RequiredArgsConstructor
public class SecurityConfig extends WebSecurityConfigurerAdapter {

    private final UserSecurityService userSecurityService;

    @Bean
    PasswordEncoder passwordEncoder(){
        return new BCryptPasswordEncoder();
    }

    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
        auth.userDetailsService(userSecurityService)
                .passwordEncoder(passwordEncoder());
    }

    private RememberMeServices rememberMeServices(){
        TokenBasedRememberMeServices rememberMeService = new TokenBasedRememberMeServices(
                "paper-app-token",userSecurityService
        );
        rememberMeService.setAlwaysRemember(true);
        rememberMeService.setParameter("remember-me");
        rememberMeService.setTokenValiditySeconds(3600);
        return rememberMeService;

    }

    @Bean
    public ServletListenerRegistrationBean<HttpSessionEventPublisher> httpSessionEventPublisher() {
        return new ServletListenerRegistrationBean<HttpSessionEventPublisher>(new HttpSessionEventPublisher(){
            @Override
            public void sessionDestroyed(HttpSessionEvent event) {
                super.sessionDestroyed(event);
                System.out.println("=============세션만료됨===========");
            }
        });
    };

    @Override
    public void configure(HttpSecurity http) throws Exception {
        CustomLoginFilter filter = new CustomLoginFilter(
                authenticationManagerBean(),
                rememberMeServices()
        );
        http
                .csrf().disable()
                .formLogin(
                        login->{
                            login.loginPage("/login");
                        }
                )
                .logout(
                        logout->{
                            logout.logoutSuccessUrl("/");
                        }
                )
                .userDetailsService(userSecurityService)
                .addFilterAt(filter , UsernamePasswordAuthenticationFilter.class)
                .authorizeRequests(
                    r->{
                        r.antMatchers("/manager/**").hasAuthority("ROLE_ADMIN");
                        r.antMatchers("/study/**").hasAnyAuthority("ROLE_ADMIN","ROLE_STUDENT");
                        r.antMatchers("/teacher/**").hasAnyAuthority("ROLE_ADMIN","ROLE_TEACHER");
                    }
                )
                .exceptionHandling(e->{
                    e.accessDeniedPage("/access-denied");
                })
                .rememberMe(config->{
                    config.rememberMeServices(rememberMeServices());
                });
    }

    @Override
    public void configure(WebSecurity web) throws Exception {
        web.ignoring()
                .requestMatchers(PathRequest.toStaticResources().atCommonLocations());
                //.requestMatchers(PathRequest.toH2Console());
    }
}
