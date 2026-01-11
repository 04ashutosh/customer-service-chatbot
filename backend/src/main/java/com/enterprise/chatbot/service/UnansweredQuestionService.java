package com.enterprise.chatbot.service;

import com.enterprise.chatbot.dto.KnowledgeBaseRequest;
import com.enterprise.chatbot.model.UnansweredQuestion;
import com.enterprise.chatbot.repository.UnansweredQuestionRepository;
import com.enterprise.chatbot.security.TenantContext;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UnansweredQuestionService {
    
    private final UnansweredQuestionRepository unansweredQuestionRepository;
    private final KnowledgeBaseService knowledgeBaseService;
    
    @Transactional(readOnly = true)
    public Page<UnansweredQuestion> getUnansweredQuestions(UnansweredQuestion.QuestionStatus status, Pageable pageable) {
        Long companyId = TenantContext.getCompanyId();
        return unansweredQuestionRepository.findByCompanyIdAndStatus(companyId, status, pageable);
    }
    
    @Transactional
    public void approveAndConvertToFAQ(Long questionId, String answer, Long userId) {
        Long companyId = TenantContext.getCompanyId();
        
        UnansweredQuestion uq = unansweredQuestionRepository.findById(questionId)
                .orElseThrow(() -> new RuntimeException("Question not found"));
        
        // Verify tenant isolation
        if (!uq.getCompanyId().equals(companyId)) {
            throw new RuntimeException("Unauthorized access");
        }
        
        // Create FAQ from unanswered question
        KnowledgeBaseRequest kbRequest = new KnowledgeBaseRequest();
        kbRequest.setQuestion(uq.getQuestion());
        kbRequest.setAnswer(answer);
        kbRequest.setIsVerified(true);
        
        knowledgeBaseService.createFAQ(kbRequest, userId);
        
        // Update status to approved
        uq.setStatus(UnansweredQuestion.QuestionStatus.APPROVED);
        unansweredQuestionRepository.save(uq);
    }
    
    @Transactional
    public void rejectQuestion(Long questionId) {
        Long companyId = TenantContext.getCompanyId();
        
        UnansweredQuestion uq = unansweredQuestionRepository.findById(questionId)
                .orElseThrow(() -> new RuntimeException("Question not found"));
        
        // Verify tenant isolation
        if (!uq.getCompanyId().equals(companyId)) {
            throw new RuntimeException("Unauthorized access");
        }
        
        uq.setStatus(UnansweredQuestion.QuestionStatus.REJECTED);
        unansweredQuestionRepository.save(uq);
    }
}
