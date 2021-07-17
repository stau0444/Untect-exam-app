package com.sp.fc.user.service.helper;

import com.sp.fc.user.domain.Authority;
import com.sp.fc.user.domain.School;
import com.sp.fc.user.domain.User;
import com.sp.fc.user.repository.SchoolRepository;
import com.sp.fc.user.repository.UserRepository;
import com.sp.fc.user.service.SchoolService;
import com.sp.fc.user.service.UserSecurityService;
import com.sp.fc.user.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.transaction.annotation.Transactional;


public class UserTestCommon {
    @Autowired
    protected UserRepository userRepository;
    @Autowired
    protected SchoolRepository schoolRepository;

    protected UserService userService;
    protected UserTestHelper userTesthelper;
    protected UserSecurityService userSecurityService;
    protected School school;
    private boolean prepared;


    protected void setService() {
        if(prepared) return;
        prepared = true;
        this.schoolRepository.deleteAll();
        this.userRepository.deleteAll();
        this.userService = new UserService(schoolRepository,userRepository);
        this.userTesthelper = new UserTestHelper(userService,new BCryptPasswordEncoder());
        this.school = new SchoolTestHelper(new SchoolService(schoolRepository)).createSchool("서울학교","서울");
        this.userSecurityService = new UserSecurityService(userRepository);
    }
}
