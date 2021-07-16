package com.sp.fc.user.service;

import com.sp.fc.user.domain.Authority;
import com.sp.fc.user.domain.School;
import com.sp.fc.user.domain.User;
import com.sp.fc.user.repository.SchoolRepository;
import com.sp.fc.user.repository.UserRepository;
import com.sp.fc.user.service.helper.SchoolTestHelper;
import com.sp.fc.user.service.helper.UserTestCommon;
import com.sp.fc.user.service.helper.UserTestHelper;
import org.hibernate.exception.ConstraintViolationException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

/**

 사용자 생성
 이름 수정
 권한 부여
 권한 취소
 email검색
 role 중복해서 추가되지 않는다.
 email이 중복되어서 들어가는가?

 */
@DataJpaTest
public class UserTest  extends UserTestCommon {


    @BeforeEach
    public void before(){
        setService();
    }

    @DisplayName("1. 유저 생성")
    @Test
    void test_1(){
        Optional<User> searchedUser = userRepository.findByEmail("ugo@test.com");
        assertTrue(searchedUser.isPresent());
        searchedUser.ifPresent(System.out::println);
    }

    @Transactional
    @DisplayName("2.유저 이름 변경")
    @Test
    void test_2(){
        Optional<User> searchedUser = userService.findUser(user.getId());
        searchedUser.ifPresent(u -> u.setUsername("황우고"));
        List<User> allUser = userRepository.findAll();

        assertEquals("황우고" , allUser.get(0).getUsername());
    }

    @DisplayName("3.권한 부여")
    @Test
    void test_3(){
        User authorizeUser = userTesthelper.addAuthority(user.getId(), Authority.ROLE_STUDENT);
        userService.addAuthority(authorizeUser.getId(),Authority.ROLE_TEACHER);

        List<User> allUser = userRepository.findAll();

        assertEquals(2,allUser.get(0).getAuthorities().size());


    }
    @DisplayName("4.권한 취소")
    @Test
    void test_4(){
        userService.removeAuthority(user.getId(),Authority.ROLE_STUDENT);
        User user = userRepository.findAll().get(0);
        for (Authority authority : user.getAuthorities()) {
            System.out.println("authority = " + authority);
        }
        assertEquals(1,user.getAuthorities().size());
        assertTrue(user.getAuthorities().contains(new Authority(user.getId(),Authority.ROLE_TEACHER)));
    }

    @DisplayName("5. email로 user검색")
    @Test
    void test_5(){
        Optional<User> byEmail = userRepository.findByEmail(user.getEmail());
        byEmail.ifPresent(u->
        {
            userTesthelper.assertUser(u,Authority.ROLE_STUDENT,Authority.ROLE_TEACHER);
        });
    }

    @DisplayName("6. role 중복해서 추가되지 않는다.")
    @Test
    void test_6(){
       userService.addAuthority(user.getId(),Authority.ROLE_STUDENT);

        Optional<User> user = userService.findUser(this.user.getId());
        user.ifPresent(
                u->{userTesthelper.assertUser(u,Authority.ROLE_STUDENT,Authority.ROLE_TEACHER);}
        );
    }

    @DisplayName("7.email 중복 문제")
    @Test
    void test_7(){
        User.builder()
                .school(school)
                .username("ugo2")
                .email("ugo@test.com")
                .build();
        assertThrows(DataIntegrityViolationException.class, ()->{userRepository.save(user);});
    }
}
