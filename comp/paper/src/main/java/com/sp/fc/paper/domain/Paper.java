package com.sp.fc.paper.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.security.core.userdetails.User;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
public class Paper {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "paper_id")
    private Long id;

    private Long paperTemplateId;
    private String name;

    private Long studentUserId;

    @Transient
    private User user;

    @OneToMany(fetch = FetchType.LAZY,cascade = CascadeType.ALL)
    private List<PaperAnswer> paperAnswerList;

    private LocalDateTime createdAt;
    //시험 시작 시점
    private LocalDateTime startTime;
    //시험 종료 시점
    private LocalDateTime endTime;

    private PaperState state;

    public static enum PaperState{
        READY,
        START,
        END,
        CANCELED
    }

    //시험 문제(problem)의 개수
    private int total;
    //
    private int answered;
    private int correct;

    @Transient
    public double getScore(){
        if(total < 1) return 0;
        return correct * 100.0 / total;
    }

    public void addAnswered(){answered++;}

    public void addCorrect(){correct++;}

    //문제번호와 , 답안을 갖는 Map을 생성
    public Map<Integer,PaperAnswer> answerMap(){
        if(paperAnswerList == null) return new HashMap<>();
        return paperAnswerList.stream().collect(Collectors.toMap(PaperAnswer::num, Function.identity()));
    }


}
