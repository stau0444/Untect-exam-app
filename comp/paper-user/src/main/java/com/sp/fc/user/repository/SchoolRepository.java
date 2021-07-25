package com.sp.fc.user.repository;

import com.sp.fc.user.domain.School;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

public interface SchoolRepository extends JpaRepository<School,Long> {


    @Query("select distinct(s.city) from School s")
    List<String> getCities();

    List<School> findAllByCity(String city);

}
