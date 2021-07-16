package com.sp.fc.user.service.helper;

import com.sp.fc.user.domain.Authority;
import com.sp.fc.user.domain.School;
import com.sp.fc.user.domain.User;
import com.sp.fc.user.repository.SchoolRepository;
import com.sp.fc.user.repository.UserRepository;
import com.sp.fc.user.service.SchoolService;
import com.sp.fc.user.service.UserService;
import lombok.RequiredArgsConstructor;
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
}
