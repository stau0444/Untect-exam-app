package com.sp.fc.paper.service;

import com.sp.fc.paper.domain.Problem;
import com.sp.fc.paper.repository.ProblemRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Transactional
public class ProblemService {

    private final ProblemRepository problemRepository;

    public Problem save(Problem problem) {
        if(problem.getId() == null){
            problem.setCreatedAt(LocalDateTime.now());
        }
        problem.setUpdatedAt(LocalDateTime.now());
        return problemRepository.save(problem);
    }

    public void delete(Problem problem) {
        problemRepository.delete(problem);
    }
}
