package com.sp.fc.paper.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
public class PaperAnswer {

    @JsonIgnore
    @ManyToOne
    @JoinColumn(name = "paper_id")
    private Paper paper;


    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Embeddable
    public static class PaperAnswerId implements Serializable{
        //시험지 아이디
        private Long pId;
        //문제 번호
        private Integer num;
    }

    @EmbeddedId
    private PaperAnswerId id;

    private Long problemId;
    private String answer;
    private boolean correct;

    private LocalDateTime answered;


    public Integer num(){return id.getNum();}

}
