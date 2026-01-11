package com.enterprise.chatbot.service;

import com.enterprise.chatbot.ai.GeminiService;
import com.enterprise.chatbot.ai.PromptBuilder;
import com.enterprise.chatbot.ai.RetrievalEngine;
import com.enterprise.chatbot.ai.RetrievalResult;
import com.enterprise.chatbot.dto.ChatRequest;
import com.enterprise.chatbot.dto.ChatResponse;
import com.enterprise.chatbot.model.ChatHistory;
import com.enterprise.chatbot.model.Company;
import com.enterprise.chatbot.model.UnansweredQuestion;
import com.enterprise.chatbot.repository.ChatHistoryRepository;
import com.enterprise.chatbot.repository.CompanyRepository;
import com.enterprise.chatbot.repository.UnansweredQuestionRepository;
import com.enterprise.chatbot.security.TenantContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
@Slf4j
public class ChatbotService {
    
    private final RetrievalEngine retrievalEngine;
    private final GeminiService geminiService;
    private final PromptBuilder promptBuilder;
    private final ChatHistoryRepository chatHistoryRepository;
    private final UnansweredQuestionRepository unansweredQuestionRepository;
    private final CompanyRepository companyRepository;
    
    @Value("${app.ai.confidence-threshold}")
    private double confidenceThreshold;
    
    @Transactional
    public ChatResponse processQuestion(ChatRequest request, Long userId) {
        Long companyId = TenantContext.getCompanyId();
        String question = request.getQuestion();
        
        // Get company name
        Company company = companyRepository.findById(companyId)
                .orElseThrow(() -> new RuntimeException("Company not found"));
        
        // Step 1: Retrieve relevant FAQs
        RetrievalResult retrievalResult = retrievalEngine.retrieveRelevantFAQs(companyId, question);
        
        ChatResponse response;
        
        // Step 2: Check if we have a high-confidence match
        if (retrievalResult.isHasHighConfidenceMatch() && !retrievalResult.getResults().isEmpty()) {
            // Return direct answer from knowledge base
            String answer = retrievalResult.getResults().get(0).getAnswer();
            response = ChatResponse.builder()
                    .answer(answer)
                    .source("KB")
                    .confidence(BigDecimal.valueOf(retrievalResult.getBestScore()))
                    .answered(true)
                    .build();
            
            saveChatHistory(companyId, userId, question, answer, BigDecimal.valueOf(retrievalResult.getBestScore()));
            
        } else if (!retrievalResult.getResults().isEmpty()) {
            // Step 3: Use AI with context from knowledge base
            String prompt = promptBuilder.buildPrompt(company.getCompanyName(), question, retrievalResult.getResults());
            String aiAnswer = geminiService.generateResponse(prompt);
            
            // Step 4: Validate AI response
            if (aiAnswer.contains("I don't have that information")) {
                // AI couldn't answer - log as unanswered
                response = ChatResponse.builder()
                        .answer("I don't have that information yet. Our team will review your question and update our knowledge base.")
                        .source("NONE")
                        .confidence(BigDecimal.ZERO)
                        .answered(false)
                        .build();
                
                captureUnansweredQuestion(companyId, question);
                saveChatHistory(companyId, userId, question, aiAnswer, BigDecimal.ZERO);
                
            } else {
                // AI provided an answer
                response = ChatResponse.builder()
                        .answer(aiAnswer)
                        .source("AI")
                        .confidence(BigDecimal.valueOf(retrievalResult.getBestScore()))
                        .answered(true)
                        .build();
                
                saveChatHistory(companyId, userId, question, aiAnswer, BigDecimal.valueOf(retrievalResult.getBestScore()));
            }
            
        } else {
            // No relevant FAQs found
            response = ChatResponse.builder()
                    .answer("I don't have that information yet. Our team will review your question and update our knowledge base.")
                    .source("NONE")
                    .confidence(BigDecimal.ZERO)
                    .answered(false)
                    .build();
            
            captureUnansweredQuestion(companyId, question);
            saveChatHistory(companyId, userId, question, "No answer available", BigDecimal.ZERO);
        }
        
        return response;
    }
    
    private void saveChatHistory(Long companyId, Long userId, String question, String answer, BigDecimal confidence) {
        ChatHistory chatHistory = new ChatHistory();
        chatHistory.setCompanyId(companyId);
        chatHistory.setUserId(userId);
        chatHistory.setQuestion(question);
        chatHistory.setAiAnswer(answer);
        chatHistory.setConfidenceScore(confidence);
        chatHistoryRepository.save(chatHistory);
    }
    
    private void captureUnansweredQuestion(Long companyId, String question) {
        // Check if question already exists
        var existing = unansweredQuestionRepository.findByCompanyIdAndQuestion(companyId, question);
        
        if (existing.isPresent()) {
            // Increment frequency
            UnansweredQuestion uq = existing.get();
            uq.setFrequency(uq.getFrequency() + 1);
            unansweredQuestionRepository.save(uq);
        } else {
            // Create new unanswered question
            UnansweredQuestion uq = new UnansweredQuestion();
            uq.setCompanyId(companyId);
            uq.setQuestion(question);
            uq.setFrequency(1);
            uq.setStatus(UnansweredQuestion.QuestionStatus.NEW);
            unansweredQuestionRepository.save(uq);
        }
    }
}
