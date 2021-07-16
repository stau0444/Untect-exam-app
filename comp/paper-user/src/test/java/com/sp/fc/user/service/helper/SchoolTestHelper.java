package com.sp.fc.user.service.helper;

import com.sp.fc.user.domain.School;
import com.sp.fc.user.service.SchoolService;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Assertions;

import static org.junit.jupiter.api.Assertions.*;

//테스트에 도움을 주는 Util성 클래스
@RequiredArgsConstructor
public class SchoolTestHelper {

    private final SchoolService service;

    //학교 생성 메서드
    public static School makeSchool(String name, String city){
        School school = School.builder()
                .name(name)
                .city(city)
                .build();
        return school;
    }

    //학교 db에 insert하는 메서드
    public School createSchool(String name , String city){
        return service.save(makeSchool(name,city));
    }

    //학교 검증 메서드
    public  void assertSchool(School school , String name ,String city){
        assertNotNull(school.getId());
        assertNotNull(school.getCreatedAt());
        assertNotNull(school.getUpdatedAt());

        assertEquals(name,school.getName());
        assertEquals(city,school.getCity());

    }
}
