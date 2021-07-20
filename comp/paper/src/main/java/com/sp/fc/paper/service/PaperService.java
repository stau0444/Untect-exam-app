package com.sp.fc.paper.service;

import com.sp.fc.paper.domain.Paper;
import com.sp.fc.paper.domain.PaperAnswer;
import com.sp.fc.paper.repository.PaperAnswerRepository;
import com.sp.fc.paper.repository.PaperRepository;
import com.sp.fc.paper.repository.PaperTemplateRepository;
import com.sp.fc.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.access.prepost.PostAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@Service
@RequiredArgsConstructor
@Transactional
public class PaperService {

    private final UserRepository userRepository;
    private final PaperTemplateRepository paperTemplateRepository;
    private final PaperRepository paperRepository;
    private final PaperAnswerRepository paperAnswerRepository;
    private final PaperTemplateService paperTemplateService;

    protected Paper save(Paper paper){
        if(paper.getId() == null){
            paper.setCreatedAt(LocalDateTime.now());
        }
        return paperRepository.save(paper);
    }

    public List<Paper> publishPaper(long paperTemplateId,List<Long> studentIds){
        //등록된 페이퍼 리스트를 가져와서 하나씩꺼내고
        List<Paper> papers = paperTemplateRepository.findById(paperTemplateId).map(paperTemplate ->
                //파라미터로 들어온 학생아이디로 학생을 찾아 스트림을열어 하나씩 Paper를 배포한다.
                StreamSupport.stream(userRepository.findAllById(studentIds).spliterator(),false)
                    .map(student->{
                        //페이퍼 생성
                        Paper paper = Paper.builder()
                                .paperTemplateId(paperTemplate.getId())
                                .name(paperTemplate.getName())
                                .state(Paper.PaperState.READY)
                                //해당 Paper에 시험지를 풀어야하는 학생이 등록된다.
                                .studentUserId(student.getId())
                                .total(paperTemplate.getTotal())
                                .build();
                        return save(paper);
                        //리턴되는 페이퍼가 리스트로 저장된다.
                    }).collect(Collectors.toList())
                //만약 템플릿이 존재하지 않는다면 IllegalArgumentException이 발생한다.
        ).orElseThrow(()-> new IllegalArgumentException(paperTemplateId + "시험지 템플릿이 존재하지 않습니다."));
        //해당 시험지가 배포된 수를 업데이트 한다.
        paperTemplateService.updatePublishedCount(paperTemplateId,papers.size());
        return papers;
    }

    public void removePaper(long paperTemplateId , List<Long> studentList){
        //시험지와 해당 시험을 봐야하는 학생들을 파라미터로 시험지를 가져와 삭제한다.
        //paperTemplate는 학생당 하나씩 생기기 때문에 forEach로 하나씩 삭제한다 .
        paperRepository.findAllByPaperTemplateIdAndStudentUserIdIn(paperTemplateId,studentList)
                .forEach(paper -> {
                    paperRepository.delete(paper);
                });
    }

    public void answer(Long paperId , Long problemId , int num , String answer){
        //페이퍼를 가져오고
        paperRepository.findById(paperId).ifPresentOrElse(paper -> {

            Optional<PaperAnswer> pa =paper.getPaperAnswerList() == null ? Optional.empty() :
                    //페이퍼에 정답가 비어있지 않다면
                    //정답 리스트를 가져와 스트림으로 하나씩 꺼내 정답의 문제번호와 파라미터로 들어오는 문제번호를 비교하고
                    //첫번째로 찾아지는 것을 리턴한다
                    paper.getPaperAnswerList().stream().filter(a->a.getId().getNum() == num).findFirst();
            //만약 일치하는 문제가 있다면
            if(pa.isPresent()){
                      //paperAnswer 인스턴스를 가져와 파라미터의 정보를 바인딩하여
                      PaperAnswer pAnswer = pa.get();
                      pAnswer.setAnswer(answer);
                      pAnswer.setAnswered(LocalDateTime.now());
                      pAnswer.setProblemId(problemId);
                      //paperAnswer 저장
                      paperAnswerRepository.save(pAnswer);
                  }else {
                      //없다면 PaperAnswer를 새로 만들고
                      PaperAnswer pAnswer = PaperAnswer.builder()
                              .id(new PaperAnswer.PaperAnswerId(paperId,num))
                              .problemId(problemId)
                              .answer(answer)
                              .answered(LocalDateTime.now())
                              .build();
                      pAnswer.setPaper(paper);
                      //빈 ArrayList를 만들고
                      if(paper.getPaperAnswerList() == null) paper.setPaperAnswerList(new ArrayList<>());
                      //생성한 PaperAnswer를 리스트에 추가해준다.
                      paper.getPaperAnswerList().add(pAnswer);
                      //체점된 문제의 수를 증가시킨다.
                      paper.addAnswered();
                      //만약 시험의 상태가 START가 아니라면 START로 지정해준다.
                      if(paper.getState() != Paper.PaperState.START){
                          paper.setState(Paper.PaperState.START);
                          paper.setStartTime(LocalDateTime.now());
                      }
                      //변경된 내용을 저장해 준다.
                      paperRepository.save(paper);
                  }
            //없다면 에러를 터뜨린다.
        },()->new IllegalArgumentException(paperId + "시험지를 찾을 수 없음"));
    }

    public void paperDone(Long paperId){
        //페이퍼를 찾아서
        final Paper paper = paperRepository.findById(paperId).orElseThrow(()->new IllegalArgumentException(paperId + "시험지를 찾을 수 없음"));
        //페이퍼의 답안지를 가져오고
        final Map<Integer,String> answerSheet = paperTemplateService.getPaperAnswerSheet(paper.getPaperTemplateId());
        //정답수를 초기화 시킴
        paper.setCorrect(0);
        //학생의 답 리스트가 null이 아니라면
        if(paper.getPaperAnswerList() != null){
            //학생의 답 리스트를 가져와서
            paper.getPaperAnswerList().forEach(a->{
                //정답과 학생의 답을 비교하고
                if(a.getAnswer() != null && a.getAnswer().equals(answerSheet.get(a.getId().getNum()))){
                    //맞다면 해당 답을 정답처리
                    a.setCorrect(true);
                    //정답수 증가
                    paper.addCorrect();
                    //학생답리파지도티에 저
                    paperAnswerRepository.save(a);
                }
            });
            //시험이 끝났음으로 상태를 END로 만들고
            paper.setState(Paper.PaperState.END);
            //끝난 시간을 셋팅
            paper.setEndTime(LocalDateTime.now());
            //변경사항 저장
            Paper saved = paperRepository.save(paper);
            //체점 완료 수를 증가
            paperTemplateService.updateCompleteCount(saved.getPaperTemplateId());
        }
    }

    //
    @Transactional(readOnly = true)
    public List<Paper> getPapers(Long paperTemplateId) {
        return paperRepository.findAllByPaperTemplateId(paperTemplateId);
    }

    @Transactional(readOnly = true)
    public List<Paper> getPapersByUser(Long studyUserId) {
        return paperRepository.findAllByStudentUserIdOrderByCreatedDesc(studyUserId);
    }

    @Transactional(readOnly = true)
    public List<Paper> getPapersByUserState(Long studyUserId, Paper.PaperState state) {
        return paperRepository.findAllByStudentUserIdAndStateOrderByCreatedAtDesc(studyUserId,state);
    }

    @Transactional(readOnly = true)
    public List<Paper> getPapersByUserIng(Long studyUserId) {
        return paperRepository.findAllByStudentUserIdAndStateInOrderByCreatedAtDesc(studyUserId, List.of(Paper.PaperState.READY, Paper.PaperState.START));
    }

    @Transactional(readOnly = true)
    public long countPapersByUserIng(Long studyUserId) {
        return paperRepository.countByStudentUserIdAndStateIn(studyUserId, List.of(Paper.PaperState.READY, Paper.PaperState.START));
    }

    @Transactional(readOnly = true)
    public Page<Paper> getPapersByUserResult(Long studyUserId, int pageNum, int size) {
        return paperRepository.findAllByStudentUserIdAndStateInOrderByCreatedAtDesc(studyUserId, Paper.PaperState.END, PageRequest.of(pageNum-1, size));
    }

    @Transactional(readOnly = true)
    public long countPapersByUserResult(Long studyUserId) {
        return paperRepository.countByStudentUserIdAndState(studyUserId, Paper.PaperState.END);
    }

    @Transactional(readOnly = true)
    public List<Paper> getPapersByUserState(Long studyUserId, List<Paper.PaperState> states) {
        return paperRepository.findAllByStudentUserIdAndStateInOrderByCreatedAtDesc(studyUserId, states);
    }

    @PostAuthorize("returnObject.isEmpty() || returnObject.get().studyUserId == principal.userId")
    @Transactional(readOnly = true)
    public Optional<Paper> findPaper(Long paperId) {
        return paperRepository.findById(paperId).map(paper->{
            paper.setUser(userRepository.getOne(paper.getStudentUserId()));
            return paper;
        });
    }
}
