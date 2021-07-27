package com.sp.fc.user.service;

import com.sp.fc.user.domain.Authority;
import com.sp.fc.user.domain.School;
import com.sp.fc.user.domain.User;
import com.sp.fc.user.repository.SchoolRepository;
import com.sp.fc.user.repository.UserRepository;
import com.sp.fc.user.service.helper.SchoolTestHelper;
import com.sp.fc.user.service.helper.UserTestCommon;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;


import javax.persistence.EntityManager;
import java.util.List;
import java.util.Optional;

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



    private User user;
    @Rollback(false)
    @Transactional
    @BeforeEach
    public void before(){
        setService();
        user = userTesthelper.saveUser("ugo","123","1",school);
        user = userTesthelper.addAuthority(user.getId(),Authority.ROLE_STUDENT,Authority.ROLE_TEACHER);
    }

    @DisplayName("1. 유저 생성")
    @Test
    void test_1(){
        Optional<User> searchedUser = userRepository.findByEmail("ugo@test.com");
        assertTrue(searchedUser.isPresent());
        searchedUser.ifPresent(System.out::println);
    }

    @DisplayName("2.유저 이름 변경")
    @Test
    void test_2(){
        Optional<User> searchedUser = userService.findUser(user.getId());
        searchedUser.ifPresent(u -> u.setName("황우고"));
        List<User> allUser = userRepository.findAll();

        assertEquals("황우고" , allUser.get(0).getUsername());
    }

    @DisplayName("3.권한 부여")
    @Test
    void test_3(){
        userTesthelper.addAuthority(user.getId(), Authority.ROLE_STUDENT);
        userService.addAuthority(user.getId(),Authority.ROLE_TEACHER);

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
        assertThrows(DataIntegrityViolationException.class, ()->{
            userRepository.save(user);
        });
    }

    @DisplayName("8.페이징 처리 테스트")
    @Test
    void test_8(){
        userTesthelper.saveSeveralUser(10,school);

        Page<User> users = userService.pagingUser(1, 5);

        assertEquals(11,users.getTotalElements());
        assertEquals(3,users.getTotalPages());
        assertEquals(5,users.getContent().size());

    }

    @DisplayName("9.학생 , 선생님 목록 가져오기")
    @Test
    void test_9(){
        userTesthelper.saveSeveralUser(20,school);
        List<User> studentList = userService.findStudentList();
        List<User> teacherList = userService.findTeacherList();

        assertEquals(5,teacherList.size());
        assertEquals(14,studentList.size());

        for (User user1 : teacherList) {
            System.out.println("teacher = " + user1);
        }
        for (User user1 : studentList) {
            System.out.println("student = " + user1);
        }
    }
}
