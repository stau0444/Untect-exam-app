package com.sp.fc.user.service.helper;

import com.sp.fc.user.domain.Authority;
import com.sp.fc.user.domain.School;
import com.sp.fc.user.domain.User;
import com.sp.fc.user.repository.SchoolRepository;
import com.sp.fc.user.repository.UserRepository;
import com.sp.fc.user.service.SchoolService;
import com.sp.fc.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.junit.platform.commons.util.StringUtils;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

@RequiredArgsConstructor
public class UserTestHelper {

    private final UserService userService;
    private final BCryptPasswordEncoder encoder;

    public User saveUser(String username, String password, String grade, School school){

        User user = User.builder()
                .username(username)
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
        assertEquals("서울학교",u.getSchool().getName());
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
            //5명 id는 2부터시작
            //총 21 명중 5명은 teacher 16명은 student
            if(i<=6 && i != 1){
                addAuthoritySeveral((long)i,Authority.ROLE_TEACHER);
            }else{
                addAuthoritySeveral((long)i,Authority.ROLE_STUDENT);
            }
        }

    }


    public User createTeacher(String name, String pwd , String grade ,School school) {
        User teacher = saveUser("teacher1", "1111", "1", school);
        addAuthority(teacher.getId() , Authority.ROLE_TEACHER);

        return teacher;
    }

    public void saveSeveralStudent(int num,School school,User teacher) {
        for(int i = 0 ; i<num; i++){
            User user = saveUser("student" + i, i + "123", String.valueOf(i), school);
            addAuthority(user.getId(),Authority.ROLE_STUDENT);
            user.setTeacher(teacher);
        }
    }


    public void saveSeveralTeacher(int num,School school) {
        for(int i = 0 ; i<num; i++){
            User user = saveUser("teacher" + i+1, i + "123", String.valueOf(i), school);
            addAuthority(user.getId(),Authority.ROLE_TEACHER);
        }
    }
}
