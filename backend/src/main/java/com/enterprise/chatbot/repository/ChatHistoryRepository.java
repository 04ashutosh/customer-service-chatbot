package com.enterprise.chatbot.repository;

import com.enterprise.chatbot.model.ChatHistory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ChatHistoryRepository extends JpaRepository<ChatHistory, Long> {
    
    Page<ChatHistory> findByCompanyIdAndUserId(Long companyId, Long userId, Pageable pageable);
    
    Page<ChatHistory> findByCompanyId(Long companyId, Pageable pageable);
    
    @Query("SELECT ch.question, COUNT(ch) as count FROM ChatHistory ch " +
            "WHERE ch.companyId = :companyId " +
            "GROUP BY ch.question " +
            "ORDER BY count DESC")
    List<Object[]> findTopQuestionsByCompany(@Param("companyId") Long companyId, Pageable pageable);
}
