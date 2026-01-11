package com.enterprise.chatbot.repository;

import com.enterprise.chatbot.model.KnowledgeBase;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface KnowledgeBaseRepository extends JpaRepository<KnowledgeBase, Long> {
    
    Page<KnowledgeBase> findByCompanyId(Long companyId, Pageable pageable);
    
    List<KnowledgeBase> findByCompanyIdAndIsVerified(Long companyId, Boolean isVerified);
    
    @Query(value = "SELECT * FROM knowledge_base WHERE company_id = :companyId " +
            "AND MATCH(question, answer) AGAINST(:query IN NATURAL LANGUAGE MODE) " +
            "ORDER BY MATCH(question, answer) AGAINST(:query IN NATURAL LANGUAGE MODE) DESC " +
            "LIMIT :limit", nativeQuery = true)
    List<KnowledgeBase> searchByFullText(@Param("companyId") Long companyId, 
                                          @Param("query") String query, 
                                          @Param("limit") int limit);
    
    @Query("SELECT kb FROM KnowledgeBase kb WHERE kb.companyId = :companyId " +
            "AND (LOWER(kb.question) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
            "OR LOWER(kb.answer) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    List<KnowledgeBase> searchByKeyword(@Param("companyId") Long companyId, 
                                         @Param("keyword") String keyword);
}
