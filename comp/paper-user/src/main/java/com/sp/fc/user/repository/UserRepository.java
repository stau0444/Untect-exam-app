package com.sp.fc.user.repository;

import com.sp.fc.user.domain.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;


public interface UserRepository extends JpaRepository<User,Long> {

    Optional<User> findByEmail(String username);

    @Query("select u from User u ,Authority a where u.id = a.userId and a.authority = ?1")
    List<User> findUserByAuthorities(String authorities);

    @Query("select u from User u ,Authority a where u.id = a.userId and a.authority = ?1")
    Page<User> findUserByAuthorities(String authorities , Pageable pageable);

    @Query("select u from User u ,Authority a where u.school.id = ?1 and u.id = a.userId and a.authority =?2")
    List<User> findAllBySchool(Long schoolId,String authority);

    @Query("select a from User a ,User b where a.teacher.id = b.id and b.id = ?1")
    List<User> findTeacherUser(Long userId);

    @Query("select count(a) from User a ,User b where a.teacher.id = b.id and b.id = ?1")
    Long countStudentByTeacher(Long userId);

    @Query("select count(u) from User u , Authority a where u.id = a.userId and a.authority = ?1")
    long countAllByAuthoritiesIn(String authority);

    @Query("select count(u) from User u , Authority a where u.school.id = ?1 and a.userId = ?2")
    long countAllByAuthoritiesIn(Long schoolId , String authority);

    @Query("select u from User u where u.teacher.id = ?1")
    List<User> findStudentByTeacher(Long teacherId);
}
