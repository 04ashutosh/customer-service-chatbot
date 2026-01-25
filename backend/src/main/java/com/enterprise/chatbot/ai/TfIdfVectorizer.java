package com.enterprise.chatbot.ai;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

/**
 * TF-IDF (Term Frequency-Inverse Document Frequency) Vectorizer
 * 
 * TF-IDF is a numerical statistic that reflects how important a word is
 * to a document in a collection. It's used for text similarity matching
 * in the FAQ retrieval system.
 * 
 * TF(t,d) = (Number of times term t appears in document d) / (Total terms in d)
 * IDF(t) = log(Total documents / Documents containing term t)
 * TF-IDF(t,d) = TF(t,d) * IDF(t)
 */
@Component
@Slf4j
public class TfIdfVectorizer {

    private Map<String, Double> idfScores = new HashMap<>();
    private List<String> vocabulary = new ArrayList<>();
    private List<Map<String, Double>> documentVectors = new ArrayList<>();
    private List<String> originalDocuments = new ArrayList<>();
    private boolean isFitted = false;

    /**
     * Tokenize and normalize text
     */
    private List<String> tokenize(String text) {
        if (text == null || text.isEmpty()) {
            return Collections.emptyList();
        }

        return Arrays.stream(text.toLowerCase()
                .replaceAll("[^a-zA-Z0-9\\s]", " ")
                .split("\\s+"))
                .filter(word -> word.length() > 2) // Filter out very short words
                .collect(Collectors.toList());
    }

    /**
     * Calculate Term Frequency for a document
     */
    private Map<String, Double> calculateTf(List<String> tokens) {
        Map<String, Double> tf = new HashMap<>();

        if (tokens.isEmpty()) {
            return tf;
        }

        // Count occurrences
        Map<String, Long> counts = tokens.stream()
                .collect(Collectors.groupingBy(t -> t, Collectors.counting()));

        // Normalize by total tokens
        double totalTokens = tokens.size();
        counts.forEach((term, count) -> tf.put(term, count / totalTokens));

        return tf;
    }

    /**
     * Fit the vectorizer on a corpus of documents (FAQ questions)
     */
    public void fit(List<String> documents) {
        if (documents == null || documents.isEmpty()) {
            log.warn("Cannot fit TF-IDF on empty document list");
            return;
        }

        // Reset state
        idfScores.clear();
        vocabulary.clear();
        documentVectors.clear();
        originalDocuments.clear();
        originalDocuments.addAll(documents);

        int totalDocs = documents.size();
        Map<String, Integer> documentFrequency = new HashMap<>();
        Set<String> vocabSet = new HashSet<>();

        // Calculate document frequency for each term
        for (String doc : documents) {
            List<String> tokens = tokenize(doc);
            Set<String> uniqueTokens = new HashSet<>(tokens);

            for (String token : uniqueTokens) {
                documentFrequency.merge(token, 1, Integer::sum);
                vocabSet.add(token);
            }
        }

        vocabulary.addAll(vocabSet);

        // Calculate IDF scores: log(N / df(t)) + 1 (smoothed)
        for (String term : vocabulary) {
            int df = documentFrequency.getOrDefault(term, 0);
            double idf = Math.log((double) totalDocs / (df + 1)) + 1;
            idfScores.put(term, idf);
        }

        // Pre-compute TF-IDF vectors for all documents
        for (String doc : documents) {
            documentVectors.add(computeTfIdfVector(doc));
        }

        isFitted = true;
        log.info("TF-IDF vectorizer fitted on {} documents with {} vocabulary terms",
                totalDocs, vocabulary.size());
    }

    /**
     * Compute TF-IDF vector for a single document
     */
    private Map<String, Double> computeTfIdfVector(String document) {
        List<String> tokens = tokenize(document);
        Map<String, Double> tf = calculateTf(tokens);
        Map<String, Double> tfidf = new HashMap<>();

        for (Map.Entry<String, Double> entry : tf.entrySet()) {
            String term = entry.getKey();
            double tfValue = entry.getValue();
            double idfValue = idfScores.getOrDefault(term, 1.0);
            tfidf.put(term, tfValue * idfValue);
        }

        return tfidf;
    }

    /**
     * Calculate cosine similarity between two TF-IDF vectors
     */
    private double cosineSimilarity(Map<String, Double> vec1, Map<String, Double> vec2) {
        if (vec1.isEmpty() || vec2.isEmpty()) {
            return 0.0;
        }

        Set<String> allTerms = new HashSet<>();
        allTerms.addAll(vec1.keySet());
        allTerms.addAll(vec2.keySet());

        double dotProduct = 0.0;
        double norm1 = 0.0;
        double norm2 = 0.0;

        for (String term : allTerms) {
            double v1 = vec1.getOrDefault(term, 0.0);
            double v2 = vec2.getOrDefault(term, 0.0);

            dotProduct += v1 * v2;
            norm1 += v1 * v1;
            norm2 += v2 * v2;
        }

        if (norm1 == 0.0 || norm2 == 0.0) {
            return 0.0;
        }

        return dotProduct / (Math.sqrt(norm1) * Math.sqrt(norm2));
    }

    /**
     * Find the most similar documents to a query
     * Returns a list of (index, similarity score) pairs sorted by score descending
     */
    public List<SimilarityResult> findMostSimilar(String query, int topK) {
        if (!isFitted) {
            log.warn("TF-IDF vectorizer not fitted yet");
            return Collections.emptyList();
        }

        Map<String, Double> queryVector = computeTfIdfVector(query);
        List<SimilarityResult> results = new ArrayList<>();

        for (int i = 0; i < documentVectors.size(); i++) {
            double similarity = cosineSimilarity(queryVector, documentVectors.get(i));
            results.add(new SimilarityResult(i, similarity, originalDocuments.get(i)));
        }

        // Sort by similarity descending and return top K
        return results.stream()
                .sorted((a, b) -> Double.compare(b.getScore(), a.getScore()))
                .limit(topK)
                .collect(Collectors.toList());
    }

    /**
     * Get similarity score between query and a specific document
     */
    public double getSimilarity(String query, String document) {
        Map<String, Double> queryVector = computeTfIdfVector(query);
        Map<String, Double> docVector = computeTfIdfVector(document);
        return cosineSimilarity(queryVector, docVector);
    }

    public boolean isFitted() {
        return isFitted;
    }

    public int getVocabularySize() {
        return vocabulary.size();
    }

    public int getDocumentCount() {
        return originalDocuments.size();
    }

    /**
     * Result class for similarity search
     */
    public static class SimilarityResult {
        private final int index;
        private final double score;
        private final String document;

        public SimilarityResult(int index, double score, String document) {
            this.index = index;
            this.score = score;
            this.document = document;
        }

        public int getIndex() {
            return index;
        }

        public double getScore() {
            return score;
        }

        public String getDocument() {
            return document;
        }

        @Override
        public String toString() {
            return String.format("SimilarityResult{index=%d, score=%.4f, doc='%s'}",
                    index, score, document.substring(0, Math.min(50, document.length())));
        }
    }
}
