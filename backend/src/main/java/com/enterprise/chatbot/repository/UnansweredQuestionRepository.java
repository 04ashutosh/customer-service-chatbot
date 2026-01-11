package com.enterprise.chatbot.repository;

import com.enterprise.chatbot.model.UnansweredQuestion;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UnansweredQuestionRepository extends JpaRepository<UnansweredQuestion, Long> {
    
    Page<UnansweredQuestion> findByCompanyIdAndStatus(Long companyId, 
                                                       UnansweredQuestion.QuestionStatus status, 
                                                       Pageable pageable);
    
    @Query("SELECT uq FROM UnansweredQuestion uq WHERE uq.companyId = :companyId " +
            "AND LOWER(uq.question) = LOWER(:question)")
    Optional<UnansweredQuestion> findByCompanyIdAndQuestion(@Param("companyId") Long companyId, 
                                                             @Param("question") String question);
}
