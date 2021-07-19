package com.sp.fc.paper.repository;


import com.sp.fc.paper.domain.PaperAnswer;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import com.sp.fc.paper.domain.PaperTemplate;

import java.util.List;

public interface PaperTemplateRepository extends JpaRepository<PaperTemplate,Long> {

    List<PaperTemplate>  findAllByUserIdOrderByCreatedAtDesc(Long userId);

    Page<PaperTemplate> findAllByUserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);

    long countByUserId(Long userId);
}
