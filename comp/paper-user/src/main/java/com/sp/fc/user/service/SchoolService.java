package com.sp.fc.user.service;

import com.sp.fc.user.domain.School;
import com.sp.fc.user.repository.SchoolRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
@RequiredArgsConstructor
public class SchoolService {

    private final SchoolRepository schoolRepository;

    public School save(School school){
        if(school.getId() == null){
            school.setCreatedAt(LocalDateTime.now());
        }
        school.setUpdatedAt(LocalDateTime.now());
        return schoolRepository.save(school);
    }

    public List<String> cities(){
        return schoolRepository.getCities();
    }

    public Optional<School> updateName(Long schoolId,String name){
        Optional<School> school = schoolRepository.findById(schoolId);
        school.ifPresent(s->s.setName(name));
        return school;
    }

    public List<School> findAllByCity(String city) {
        return schoolRepository.findAllByCity(city);
    }
}
