package com.sp.fc.user.service;

import com.sp.fc.user.domain.Authority;
import com.sp.fc.user.domain.School;
import com.sp.fc.user.domain.User;
import com.sp.fc.user.repository.SchoolRepository;
import com.sp.fc.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@Service
@Transactional
@RequiredArgsConstructor
public class UserService {

    private final SchoolRepository schoolRepository;
    private final UserRepository userRepository;


    public User save(User user){
        if(user.getId() == null){
            user.setCreatedAt(LocalDateTime.now());
        }
        user.setUpdatedAt(LocalDateTime.now());
        return userRepository.save(user);
    }

    public Optional<User> findUser(Long userId){
        return userRepository.findById(userId);
    }

    public Page<User> pagingUser(int pageNum, int size){
        return userRepository.findAll(PageRequest.of(pageNum-1,size));
    }

    public Map<Long,User> getUsers(List<Long> userIds){
        return StreamSupport.stream(userRepository.findAllById(userIds).spliterator(),false)
                .collect(Collectors.toMap(User::getId, Function.identity()));
    }

    public void addAuthority(Long userId,String authority){
        userRepository.findById(userId).ifPresent(user -> {
            Authority newRole = new Authority(user.getId(),authority);
            if(user.getAuthorities() == null){
                HashSet<Authority> authorities = new HashSet<>();
                authorities.add(newRole);
                user.setAuthorities(authorities);
            }else if(!user.getAuthorities().contains(newRole)){
                HashSet<Authority> authorities = new HashSet<>();
                authorities.addAll(user.getAuthorities());
                authorities.add(newRole);
                user.setAuthorities(authorities);
            }
        });
    }

    public void removeAuthority(Long userId,String authority){
        userRepository.findById(userId).ifPresent(u->{
            Authority targetRole = new Authority(u.getId(),authority);
            if(u.getAuthorities().contains(targetRole)){
                u.setAuthorities(
                        u.getAuthorities().stream().filter(auth-> !auth.equals(targetRole))
                        .collect(Collectors.toSet())
                );
            }
        });
    }
    public List<User> findTeacherList(){return userRepository.findUserByAuthorities(Authority.ROLE_TEACHER);}
    public Page<User> findTeacherListPaging(Integer pageNum , Integer size){return userRepository.findUserByAuthoritiesPaging(Authority.ROLE_TEACHER,PageRequest.of(pageNum-1,size));}

    public List<User> findStudentList(){return userRepository.findUserByAuthorities(Authority.ROLE_STUDENT);}

    public Long findTeacherStudentCount(Long userId){return userRepository.countStudentByTeacher(userId);}

    public List<User> findBySchoolStudentList(Long schoolId){
        return userRepository.findAllBySchool(schoolId,Authority.ROLE_STUDENT);
    }

    public List<User> findBySchoolTeacherList(Long schoolId){
        return userRepository.findAllBySchool(schoolId,Authority.ROLE_TEACHER);
    }

    public void updateUserSchoolTeacher(Long userId, Long schoolId,Long teacherId){
        userRepository.findById(userId).ifPresent(
                user -> {
                    if(!user.getSchool().getId().equals(schoolId)){
                        schoolRepository.findById(schoolId).ifPresent(s->user.setSchool(s));
                    }
                    if(!user.getTeacher().getId().equals(teacherId)){
                        findUser(teacherId).ifPresent(t->user.setTeacher(t));
                    }
                    if(user.getSchool().getId() != user.getTeacher().getSchool().getId()){
                        throw new IllegalArgumentException("해당 학교의 선생님이 아닙니다.");
                    }
                }
        );
    }

    public List<User> findStudentsByTeacher(Long teacherId) {
        return userRepository.findStudentByTeacher(teacherId);
    }


    public Long countStudent() {
        return userRepository.countAllByAuthoritiesIn(Authority.ROLE_STUDENT);
    }

    public Long countTeacher() {
        return userRepository.countAllByAuthoritiesIn(Authority.ROLE_TEACHER);
    }

    public int schoolUserCountByAuthority(String authority,Long schoolId) {
        return userRepository.countAllByAuthoritiesInAndSchool(authority,schoolId);
    }


    public Long countStudentByTeacher(Long teacherId) {
        return userRepository.countStudentByTeacher(teacherId);
    }

    public Page<User> findStudentListPaging(Integer pageNum, Integer size) {
        return userRepository.findUserByAuthoritiesPaging(Authority.ROLE_STUDENT,PageRequest.of(pageNum-1,size));
    }
}
