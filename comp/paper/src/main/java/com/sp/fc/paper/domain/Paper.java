package com.sp.fc.paper.domain;

import com.sp.fc.user.domain.User;
import lombok.*;
import org.springframework.boot.autoconfigure.domain.EntityScan;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
/*

paperTemplate는 학생이 시험보기 전의 시험지
paper는 학생이 시험을 풀고 답을 적어놓은 시험지
AnserSheet은 정답의 답안지
Problem은 시험 문제
문제의 정답은 Problem의 answer필드에 저장된다.
PaperAnswer는 학생의 답을 의미한다.
 */
//한 시험에 여러개의 시험지가 존재한다
//시험은 Paper 클래스 , 시험지는 PaperTemplate

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

    @ToString.Exclude
    @OneToMany(fetch = FetchType.LAZY,cascade = CascadeType.ALL)
    private List<PaperAnswer> paperAnswerList = new ArrayList<>();

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
