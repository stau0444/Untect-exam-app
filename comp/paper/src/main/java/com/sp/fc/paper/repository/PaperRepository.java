package com.sp.fc.paper.repository;

import com.sp.fc.paper.domain.Paper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;


public interface PaperRepository extends JpaRepository<Paper,Long> {

    List<Paper> findAllByPaperTemplateIdAndStudentUserIdIn(Long paperTemplateId, List<Long> studentIds);

    List<Paper> findAllByPaperTemplateId(Long paperTemplatedId);
    long countByPaperTemplateId(Long paperTemplateId);


    List<Paper> findAllByStudentUserIdOrderByCreatedDesc(Long studentUser);
    long countByStudentUserId(Long studentUserId);

    Page<Paper> findAllByStudentUserIdAndStateInOrderByCreatedAtDesc(Long studentId, Paper.PaperState state , Pageable pageable);

    List<Paper> findAllByStudentUserIdAndStateOrderByCreatedAtDesc(Long studentId,Paper.PaperState state);
    long countByStudentUserIdAndState(Long studentId,Paper.PaperState state);

    List<Paper> findAllByStudentUserIdAndStateInOrderByCreatedAtDesc(Long studentId,List<Paper.PaperState> states);
    long countByStudentUserIdAndStateIn(Long studentId,List<Paper.PaperState> states);

}
