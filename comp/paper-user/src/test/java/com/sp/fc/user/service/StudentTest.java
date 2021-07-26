package com.sp.fc.user.service;

import com.sp.fc.user.domain.User;
import com.sp.fc.user.service.helper.UserTestCommon;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

// TODO: 2021/07/18
//  1.학생 등록
//  2.선생님에 등록된 학생 조회
//  3.학교로 학생 조회


@DataJpaTest
public class StudentTest extends UserTestCommon {

    User teacher;
    User student;


    @BeforeEach
    public void before(){
        setService();
        teacher = userTesthelper.saveTeacher("teacher1","123","1",school);
    }


    @DisplayName("1. 학생 등록 ")
    @Test
    void test_1(){
        student = userTesthelper.saveStudent("student1" , "123","1",school,teacher);
        Optional<User> searchedStudent = userService.findUser(student.getId());
        searchedStudent.ifPresent(
                s->{
                    userTesthelper.assertStudent(s,"student1",teacher,school,"1");
                });
    }

    @DisplayName("2.선생님에 등록된 학생 조회")
    @Test
    void test_2(){

        userTesthelper.saveSeveralStudent(5,school,teacher);

        List<User> studentsByTeacher = userService.findStudentsByTeacher(teacher.getId());

        assertEquals(5,studentsByTeacher.size());
        assertTrue(studentsByTeacher
                .stream()
                .allMatch(s->s.getTeacher().getUsername().equals("teacher1")));
        for (User user : studentsByTeacher) {
            System.out.println("Student = " + user);

        }

    }

    @DisplayName("3. 학교로 학생 조회")
    @Test
    void test_3(){
//        userTesthelper.saveSeveralStudent(5,school,teacher);
//        List<User> studentList = userService.findBySchoolStudentList(school.getId());
//
//        assertEquals(5, studentList.size());
//        assertTrue(studentList.stream().allMatch(s->s.getSchool().getName() == school.getName()));

    }
}
