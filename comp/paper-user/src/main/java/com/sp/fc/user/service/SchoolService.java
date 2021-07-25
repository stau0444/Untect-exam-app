package com.sp.fc.user.service;

import com.sp.fc.user.domain.School;
import com.sp.fc.user.repository.SchoolRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.data.web.SpringDataWebProperties;
import org.springframework.data.domain.*;
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

    public Optional<School> updateName(Long schoolId,String name,String city){
        Optional<School> school = schoolRepository.findById(schoolId);
        school.ifPresent(s->{
            s.setName(name);
            s.setCity(city);
        });
        return school;
    }

    public List<School> findAllByCity(String city) {
        return schoolRepository.findAllByCity(city);
    }

    public School findById(Long schoolId) {
        return schoolRepository.findById(schoolId).orElseThrow(()->new IllegalArgumentException("해당 학교 없음"));
    }
    public Optional<School> findByIdOp(Long schoolId) {
        return schoolRepository.findById(schoolId);
    }

    public Long countSchool() {
        return schoolRepository.count();
    }

    public Page<School> getSchoolList(Integer pageNum, Integer size) {
        return schoolRepository.findAll(PageRequest.of(pageNum-1 , size));
    }
}
