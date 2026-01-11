package com.enterprise.chatbot.ai;

import com.enterprise.chatbot.model.KnowledgeBase;
import com.enterprise.chatbot.repository.KnowledgeBaseRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class RetrievalEngine {
    
    private final KnowledgeBaseRepository knowledgeBaseRepository;
    
    @Value("${app.retrieval.threshold}")
    private double threshold;
    
    @Value("${app.retrieval.max-results}")
    private int maxResults;
    
    public RetrievalResult retrieveRelevantFAQs(Long companyId, String query) {
        // Try full-text search first (if MySQL full-text is configured)
        List<KnowledgeBase> results;
        try {
            results = knowledgeBaseRepository.searchByFullText(companyId, query, maxResults);
            if (results.isEmpty()) {
                // Fallback to keyword search
                results = knowledgeBaseRepository.searchByKeyword(companyId, query);
                if (results.size() > maxResults) {
                    results = results.subList(0, maxResults);
                }
            }
        } catch (Exception e) {
            log.warn("Full-text search failed, using keyword search", e);
            results = knowledgeBaseRepository.searchByKeyword(companyId, query);
            if (results.size() > maxResults) {
                results = results.subList(0, maxResults);
            }
        }
        
        // Calculate simple similarity score
        double bestScore = 0.0;
        if (!results.isEmpty()) {
            bestScore = calculateSimilarity(query, results.get(0).getQuestion());
        }
        
        boolean hasHighConfidenceMatch = bestScore >= threshold;
        
        return new RetrievalResult(results, bestScore, hasHighConfidenceMatch);
    }
    
    private double calculateSimilarity(String query, String question) {
        // Simple keyword overlap similarity
        String[] queryWords = query.toLowerCase().split("\\s+");
        String[] questionWords = question.toLowerCase().split("\\s+");
        
        int matches = 0;
        for (String qw : queryWords) {
            for (String aw : questionWords) {
                if (qw.equals(aw) || aw.contains(qw) || qw.contains(aw)) {
                    matches++;
                    break;
                }
            }
        }
        
        return (double) matches / Math.max(queryWords.length, questionWords.length);
    }
}
