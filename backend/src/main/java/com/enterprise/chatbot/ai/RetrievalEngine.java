package com.enterprise.chatbot.ai;

import com.enterprise.chatbot.model.KnowledgeBase;
import com.enterprise.chatbot.repository.KnowledgeBaseRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * FAQ Retrieval Engine using TF-IDF for intelligent text matching.
 * 
 * This engine maintains a TF-IDF model per company for efficient FAQ matching.
 * The model is automatically rebuilt when knowledge base changes are detected.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class RetrievalEngine {

    private final KnowledgeBaseRepository knowledgeBaseRepository;
    private final TfIdfVectorizer tfIdfVectorizer;

    // Cache TF-IDF models per company for multi-tenant support
    private final Map<Long, CompanyTfIdfModel> companyModels = new ConcurrentHashMap<>();

    @Value("${app.retrieval.threshold}")
    private double threshold;

    @Value("${app.retrieval.max-results}")
    private int maxResults;

    /**
     * Initialize TF-IDF models on startup
     */
    @PostConstruct
    public void initialize() {
        log.info("Initializing TF-IDF Retrieval Engine...");
        // Models will be lazily loaded per company on first request
    }

    /**
     * Refresh TF-IDF models periodically (every 5 minutes)
     */
    @Scheduled(fixedRate = 300000)
    public void refreshModels() {
        log.debug("Checking for TF-IDF model updates...");
        companyModels.keySet().forEach(this::rebuildModelForCompany);
    }

    /**
     * Retrieve relevant FAQs using TF-IDF similarity matching
     */
    public RetrievalResult retrieveRelevantFAQs(Long companyId, String query) {
        // Ensure TF-IDF model exists for this company
        CompanyTfIdfModel model = getOrCreateModel(companyId);

        if (model.isEmpty()) {
            log.warn("No knowledge base entries found for company {}", companyId);
            return new RetrievalResult(List.of(), 0.0, false);
        }

        // Use TF-IDF to find most similar FAQs
        List<TfIdfVectorizer.SimilarityResult> similarResults = model.getVectorizer().findMostSimilar(query,
                maxResults);

        // Map similarity results back to KnowledgeBase entities
        List<KnowledgeBase> results = similarResults.stream()
                .filter(sr -> sr.getScore() > 0.0) // Filter out zero-similarity results
                .map(sr -> model.getKnowledgeBaseEntries().get(sr.getIndex()))
                .collect(Collectors.toList());

        // Get the best similarity score
        double bestScore = similarResults.isEmpty() ? 0.0 : similarResults.get(0).getScore();

        boolean hasHighConfidenceMatch = bestScore >= threshold;

        log.debug("TF-IDF retrieval for company {}: query='{}', bestScore={:.4f}, matches={}",
                companyId, query.substring(0, Math.min(50, query.length())), bestScore, results.size());

        return new RetrievalResult(results, bestScore, hasHighConfidenceMatch);
    }

    /**
     * Calculate TF-IDF similarity between query and a specific question
     */
    public double calculateSimilarity(String query, String question) {
        return tfIdfVectorizer.getSimilarity(query, question);
    }

    /**
     * Force rebuild of TF-IDF model for a specific company
     * Call this when knowledge base is updated
     */
    public void rebuildModelForCompany(Long companyId) {
        log.info("Rebuilding TF-IDF model for company {}", companyId);
        companyModels.remove(companyId);
        getOrCreateModel(companyId);
    }

    /**
     * Invalidate model for a company (will be rebuilt on next query)
     */
    public void invalidateModel(Long companyId) {
        companyModels.remove(companyId);
        log.info("Invalidated TF-IDF model for company {}", companyId);
    }

    /**
     * Get or create TF-IDF model for a company
     */
    private synchronized CompanyTfIdfModel getOrCreateModel(Long companyId) {
        return companyModels.computeIfAbsent(companyId, this::buildModelForCompany);
    }

    /**
     * Build TF-IDF model for a company
     */
    private CompanyTfIdfModel buildModelForCompany(Long companyId) {
        log.info("Building TF-IDF model for company {}", companyId);

        // Fetch all knowledge base entries for this company
        List<KnowledgeBase> entries = knowledgeBaseRepository.findByCompanyIdAndIsVerified(companyId, true);

        if (entries.isEmpty()) {
            log.warn("No active knowledge base entries for company {}", companyId);
            return new CompanyTfIdfModel(new TfIdfVectorizer(), entries);
        }

        // Extract questions for TF-IDF fitting
        List<String> questions = entries.stream()
                .map(KnowledgeBase::getQuestion)
                .collect(Collectors.toList());

        // Create and fit a new TF-IDF vectorizer
        TfIdfVectorizer vectorizer = new TfIdfVectorizer();
        vectorizer.fit(questions);

        log.info("TF-IDF model built for company {}: {} documents, {} vocabulary terms",
                companyId, vectorizer.getDocumentCount(), vectorizer.getVocabularySize());

        return new CompanyTfIdfModel(vectorizer, entries);
    }

    /**
     * Get statistics about the TF-IDF models
     */
    public Map<Long, ModelStats> getModelStats() {
        return companyModels.entrySet().stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        e -> new ModelStats(
                                e.getValue().getVectorizer().getDocumentCount(),
                                e.getValue().getVectorizer().getVocabularySize())));
    }

    /**
     * Inner class to hold company-specific TF-IDF model and data
     */
    private static class CompanyTfIdfModel {
        private final TfIdfVectorizer vectorizer;
        private final List<KnowledgeBase> knowledgeBaseEntries;

        public CompanyTfIdfModel(TfIdfVectorizer vectorizer, List<KnowledgeBase> entries) {
            this.vectorizer = vectorizer;
            this.knowledgeBaseEntries = entries;
        }

        public TfIdfVectorizer getVectorizer() {
            return vectorizer;
        }

        public List<KnowledgeBase> getKnowledgeBaseEntries() {
            return knowledgeBaseEntries;
        }

        public boolean isEmpty() {
            return knowledgeBaseEntries.isEmpty();
        }
    }

    /**
     * Statistics record for model info
     */
    public record ModelStats(int documentCount, int vocabularySize) {
    }
}
