package com.sp.fc.user.service.helper;

import com.sp.fc.user.domain.Authority;
import com.sp.fc.user.domain.School;
import com.sp.fc.user.domain.User;
import com.sp.fc.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

@RequiredArgsConstructor
public class UserTestHelper {

    private final UserService userService;
    private final BCryptPasswordEncoder encoder;

    public User saveUser(String username, String password, String grade, School school){

        User user = User.builder()
                .name(username)
                .email(username+"@test.com")
                .enabled(true)
                .password(encoder.encode(password))
                .grade(grade)
                .school(school)
                .build();
        return userService.save(user);
    }

    public void addAuthoritySeveral(Long userId,String ... authority){
        Stream.of(authority).forEach(a->userService.addAuthority(userId,a));
    }

    public User addAuthority(Long userId,String ... authority){
        Stream.of(authority).forEach(a->userService.addAuthority(userId,a));
        return userService.findUser(userId).orElseThrow(NullPointerException::new);
    }


    public  void assertUser(User u) {
        assertEquals("ugo",u.getUsername());
        assertEquals("μμΈνκ΅",u.getSchool().getName());
        assertNotNull(u.getCreatedAt());
        assertNotNull(u.getUpdatedAt());
        assertTrue(u.isEnabled());
    }
    public  void assertUser(User user,String ... authorities) {
        assertUser(user);
        assertTrue(user
                .getAuthorities()
                .containsAll(
                        Stream.of(authorities)
                                .map(a->new Authority(user.getId(),a))
                                .collect(Collectors.toSet())
                ));
    }

    public void saveSeveralUser(int num, School school) {
        for (int i = 1; i<=num; i++){
            saveUser("user"+i,"user"+i, String.valueOf(i),school);
            //5λͺ idλ 2λΆν°μμ
            //μ΄ 21 λͺμ€ 5λͺμ teacher 16λͺμ student
            if(i<=6 && i != 1){
                addAuthoritySeveral((long)i,Authority.ROLE_TEACHER);
            }else{
                addAuthoritySeveral((long)i,Authority.ROLE_STUDENT);
            }
        }

    }


    public User saveTeacher(String name, String pwd , String grade , School school) {
        User teacher = saveUser("teacher1", "1111", "1", school);
        addAuthority(teacher.getId() , Authority.ROLE_TEACHER);

        return teacher;
    }

    public void saveSeveralTeacher(int num,School school) {
        for(int i = 0 ; i<num; i++){
            User user = saveUser("teacher" + i+1, i + "123", String.valueOf(i), school);
            addAuthority(user.getId(),Authority.ROLE_TEACHER);
        }
    }

    public User saveStudent(String studentName , String pwd , String grade , School school,User teacher) {
        User student = saveUser(studentName, pwd, grade, school);
        student.setTeacher(teacher);
        addAuthority(student.getId(),Authority.ROLE_STUDENT);
        return  student;
    }

    public void saveSeveralStudent(int num,School school,User teacher) {
        for(int i = 0 ; i<num; i++){
            User user = saveUser("student" + i, i + "123", String.valueOf(i), school);
            addAuthority(user.getId(),Authority.ROLE_STUDENT);
            user.setTeacher(teacher);
        }
    }

    public void assertStudent(User student, String name, User teacher , School school, String grade) {
        assertEquals(name, student.getUsername());
        assertEquals(teacher.getUsername(), student.getTeacher().getUsername());
        assertEquals(school.getName(), student.getSchool().getName());
        assertEquals(grade, student.getGrade());
    }
}
