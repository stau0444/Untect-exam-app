package com.sp.fc.paper.service;

import com.sp.fc.paper.domain.PaperAnswer;
import com.sp.fc.paper.domain.PaperTemplate;
import com.sp.fc.paper.domain.Problem;
import com.sp.fc.paper.repository.PaperTemplateRepository;
import com.sp.fc.user.domain.Authority;
import com.sp.fc.user.domain.User;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.awt.print.Pageable;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Transactional
@Service
@RequiredArgsConstructor
public class PaperTemplateService {

    private final PaperTemplateRepository templateRepository;
    private final ProblemService problemService;

    public PaperTemplate save(PaperTemplate paperTemplate){
        if(paperTemplate.getId() == null){
            paperTemplate.setCreatedAt(LocalDateTime.now());
        }
        paperTemplate.setUpdatedAt(LocalDateTime.now());
        return templateRepository.save(paperTemplate);
    }

    public Problem addProblem(long paperTemplateId , Problem problem){
        //파라미터로 들어온 문제에 시험지번호를 지정한다.
        problem.setTemplateId(paperTemplateId);
        //시험지를 찾아오고
        return findById(paperTemplateId).map(paperTemplate -> {
            //문제 리스트를 꺼내온다 없으면 빈 리스트를 넣어준다.
            if(paperTemplate.getProblemList() == null){
                paperTemplate.setProblemList(new ArrayList<>());
            }
            //문제 작성 시간을 지정한다.
            problem.setCreatedAt(LocalDateTime.now());
            //시험지 문제리스트에 문제를 추가해준다.
            paperTemplate.getProblemList().add(problem);
            //인트스트림으로 인덱스를 줘서 문제를를 하나씩 가져와 문제에 인덱스번호를 지정해준다.
            //문제가 추가될 떄마다 아래 코드가 실행되어 인덱스번호가 셋팅된다.
            IntStream.rangeClosed(1,paperTemplate.getProblemList().size())
                    .forEach(i->{
                        paperTemplate.getProblemList().get(i-1).setIndexNum(i);
                    });
            //시험지 문제 총수를 셋팅한다.
            paperTemplate.setTotal(paperTemplate.getProblemList().size());
            //problem 저장
            Problem saved = problemService.save(problem);
            //시험지 저장
            save(paperTemplate);
            //저장된 문제를 리턴
            return saved;
        }).orElseThrow(()->new IllegalArgumentException(paperTemplateId + "시험지기 존재하지 않습니다." ));
    }

    public Optional<PaperTemplate> findById(long paperTemplateId) {
        return templateRepository.findById(paperTemplateId);
    }


    //시험지에서 문제 삭제
    public PaperTemplate removeProblem(long paperTemplateId , long problemId){
        //시험지를 찾아서
        return findById(paperTemplateId).map(paperTemplate -> {
            //시험지의 문제리스트가 비어있다면 빈 시험지를 리턴한다.
            if(paperTemplate.getProblemList() == null){
                return paperTemplate;
            }
            //시험지에서 문제 리스트를 가져오고 파라미터로 받은 문제아이디와 시험지 문제 리스트가 일치하는 것을 찾아낸다
            Optional<Problem> problem  = paperTemplate.getProblemList().stream().filter(p->p.getId().equals(problemId)).findFirst();
            //만약 일치하는 문제가 있다면
            if(problem.isPresent()) {
                //시험지의 문제리스트에서
                paperTemplate.setProblemList(
                        //삭제하는 문제를 제외하고 문제 리스트를 초기화한다.
                        paperTemplate.getProblemList().stream().filter(p -> !p.getId().equals(problemId))
                                .collect(Collectors.toList())
                );
                //문제를 삭제한다
                problemService.delete(problem.get());
                //문제가 지워졌음으로 문제의 인덱스를 리셋 해준다.
                IntStream.rangeClosed(1, paperTemplate.getProblemList().size())
                        .forEach(i -> {
                            paperTemplate.getProblemList().get(i - 1).setIndexNum(i);
                        });
            }
                //문제의 totalCount를 리셋 시켜준다.
                paperTemplate.setTotal(paperTemplate.getProblemList().size());
            //수정된 시험지를 저장한다.
            return save(paperTemplate);
            //시험지가 존재하지 않는다면 예외를 터트린다.
        }).orElseThrow(()->new IllegalArgumentException(paperTemplateId + "시험지가 존재하지 않습니다."));
    }

    //시험지와 문제를 함께가져오는 메서드
    @Transactional(readOnly = true)
    public Optional<PaperTemplate> findProblemTemplate(Long paperTemplateId){
        //시험지를 가져오고
        return templateRepository.findById(paperTemplateId).map(pt->{
            //problem이 lazy로딩으로 셋팅되어 있기 때문에
            //시험지의 문제수와 실제 문제리스트의 사이즈가 다를 수 있기 때문에
            //아래와 같은 메서드를 추가한다.
            //시험지의 문제수와 실제 문제리스트의 사이즈가 다르다면 total을 리스트의 사이즈로 설정한다.
            if(pt.getProblemList().size() != pt.getTotal()){
                pt.setTotal(pt.getProblemList().size());
            }
            //다른 관리자가 아닌 다른 사람이 접근하는 것을 막는다 .
            if(pt.getUserId() != ((User)SecurityContextHolder.getContext().getAuthentication().getPrincipal()).getId()){
                if(!((User)SecurityContextHolder.getContext().getAuthentication().getPrincipal()).getAuthorities().contains(Authority.ROLE_TEACHER)){
                    throw new AccessDeniedException("접근 권한 없음");
                }
            }
            return pt;
        });
    }

    //정답지를 가져오는 메서드
    @Transactional(readOnly = true)
    public Map<Integer , String> getPaperAnswerSheet(Long paperTemplateId){
        //시험지를 찾아서
        Optional<PaperTemplate> paperTemplate = findById(paperTemplateId);
        //시험지가 없다면 빈 Map을 리턴한다.
        if(!paperTemplate.isPresent()) return new HashMap<>();
        //시험지에서 문제리스트를 가져와 Problem의 indexNum을 key answer를 value로 하는 맵을 만든다.
        return paperTemplate.get().getProblemList().stream().collect(Collectors.toMap(Problem::getIndexNum, Problem::getAnswer));
    }

    @Transactional(readOnly = true)
    //유저의 아이디로 시험지 List를 가져온다 .
    //컨트롤러에서 유저의 ROLE이 학생인지 선생님인지를 구분하는 코드 필요
    public List<PaperTemplate> findByTeacherId(Long userId){
        return templateRepository.findAllByUserIdOrderByCreatedAtDesc(userId);
    }

    @Transactional(readOnly = true)
    //유저의 아이디로 시험지 List를 가져온다 .
    //컨트롤러에서 유저의 ROLE이 학생인지 선생님인지를 구분하는 코드 필요
    public Page<PaperTemplate> findByTeacherId(Long userId, int pageNum , int size){
        return templateRepository.findAllByUserIdOrderByCreatedAtDesc(userId, PageRequest.of(pageNum-1,size));
    }

    @Transactional(readOnly = true)
    public Object countByUserId(Long userId){return templateRepository.countByUserId(userId);}


    //배포된 시험지의 수를 업데이트 해주는 메서드
    public void updatePublishedCount(long paperTemplateId, long publishedCount){
        templateRepository.findById(paperTemplateId).ifPresent(paperTemplate -> {
            paperTemplate.setPublishCount(publishedCount);
            templateRepository.save(paperTemplate);
        });
    }

    //체점이 완료된 시험지의 수를 리턴해주는 업데이트 해주는 메서드
    public void updateCompleteCount(Long paperTemplateId){
        templateRepository.findById(paperTemplateId).ifPresent(paperTemplate -> {
            paperTemplate.setCompleteCount(paperTemplate.getCompleteCount()+1);
            templateRepository.save(paperTemplate);
        });
    }




}
