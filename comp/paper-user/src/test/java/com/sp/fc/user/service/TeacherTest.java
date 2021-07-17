package com.sp.fc.user.service;

// 선생님을 등록한다.
// 선생님으로 등록한 학생 리스트를 조회한다.
// 선생님 리스트를 조회 한다.
// 학교로 선생님이 조회된다.

import com.sp.fc.user.domain.Authority;
import com.sp.fc.user.domain.User;
import com.sp.fc.user.service.helper.UserTestCommon;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
public class TeacherTest extends UserTestCommon {

    private User teacher;

    @BeforeEach
    public void before(){
        setService();
        this.teacher = userTesthelper.createTeacher("teacher1" , "123", "1",school);
    }


    @DisplayName("1. 선생님 등록")
    @Test
    void test_1(){

        List<User> teacherList = userService.findTeacherList();
        User searchedTeacher  = teacherList.get(0);

        assertEquals(1,teacherList.size());
        assertEquals("teacher1", searchedTeacher.getUsername());
        assertTrue(searchedTeacher
                .getAuthorities()
                .contains(new Authority(searchedTeacher.getId(),Authority.ROLE_TEACHER)));
    }

    @DisplayName("2.등록한 선생님으로 학생 수 조회")
    @Test
    void test_2(){

        userTesthelper.saveSeveralStudent(10,school,teacher);
        Long teacherStudentCount = userService.findTeacherStudentCount(teacher.getId());
        assertEquals(10L, teacherStudentCount);

    }

    @DisplayName("3.등록한 선생님으로 학생 리스트 조회")
    @Test
    void test_3(){
        userTesthelper.saveSeveralStudent(10,school,teacher);

        List<User> studentsByTeacher = userService.findStudentsByTeacher(teacher.getId());

        assertEquals(10,studentsByTeacher.size());
        assertTrue(studentsByTeacher
                .stream()
                .allMatch(s->s.getTeacher().getUsername().equals(teacher.getUsername())));

        //supplier는 받는거 없이 리턴만해주는 아이
        //comsumer는 받는것만 있고 리턴은 안해주는 아이
        //byConsumer는 두개받고 리턴 안하는 아이
        //predicate는 불린을 리턴해주는 아이
        //studentsByTeacher.stream().collect(() -> {return "asd";},(a, b)->{},(a, b)->{});
        for (User user : studentsByTeacher) {
            System.out.println("user = " + user);
        }
    }
    @DisplayName("4.선생님 리스트 조회")
    @Test
    void test_4(){
        userTesthelper.saveSeveralTeacher(10,school);
        List<User> teacherList = userService.findTeacherList();
        assertEquals(11,teacherList.size());
        assertTrue(
                teacherList
                        .stream()
                        .allMatch(t->t.getAuthorities().contains(new Authority(t.getId(),Authority.ROLE_TEACHER))));
        for (User user : teacherList) {
            System.out.println("user = " + user);
        }
    }

    @DisplayName("5.학교로 선생님 리스트 조회")
    @Test
    void test_5(){
        userTesthelper.saveSeveralTeacher(5,school);

        List<User> schoolTeacherList = userService.findBySchoolTeacherList(school.getId());

        assertEquals(6,schoolTeacherList.size());
        assertTrue(schoolTeacherList.stream().allMatch(s->s.getSchool().getName().equals("서울학교")));
    }
}
