package com.sp.fc.user.service;

import com.sp.fc.user.domain.School;
import com.sp.fc.user.repository.SchoolRepository;
import com.sp.fc.user.service.helper.SchoolTestHelper;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import javax.print.attribute.standard.Severity;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
//Repository만 컨테이너에 생성한다.
class SchoolServiceTest {

    @Autowired
    private SchoolRepository repository;

    private SchoolService service;

    private SchoolTestHelper helper;

    private School school;

    @BeforeEach
    void before(){
        this.service = new SchoolService(repository);
        this.helper = new SchoolTestHelper(service);
        school = this.helper.createSchool("테스트학교", "서울");
    }

    @DisplayName("1.학교 생성")
    @Test
    void test_1(){

        List<School> schoolList = repository.findAll();
        helper.assertSchool(school,"테스트학교","서울");
    }
    
    @DisplayName("2. 학교 이름 수정")
    @Test
    void test_2(){
        Optional<School> updatedSchool = service.updateName(school.getId(), "이름바뀜","서울");
        updatedSchool.ifPresent(s-> System.out.println(s));

        List<School> schoolList = repository.findAll();
        //helper.assertSchool(updatedSchool.orElseThrow(),"이름바뀜","서울");
    }

    @DisplayName("3.지역 목록 가져오기")
    @Test
    void test_3(){
        List<String> cities = service.cities();

        assertEquals(1,cities.size());
        assertEquals("서울",cities.get(0));

        helper.createSchool("부산학교","부산");
        cities = service.cities();

        assertEquals(2,cities.size());
    }

    @DisplayName("4.지역으로 학교목록 가져오기")
    @Test
    void test_4(){
        helper.createSchool("서울 학교","서울");

        List<School> schoolList = service.findAllByCity("서울");

        assertEquals(1, schoolList.size());
    }
}