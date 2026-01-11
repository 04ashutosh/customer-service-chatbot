package com.enterprise.chatbot.service;

import com.enterprise.chatbot.dto.KnowledgeBaseRequest;
import com.enterprise.chatbot.model.KnowledgeBase;
import com.enterprise.chatbot.repository.KnowledgeBaseRepository;
import com.enterprise.chatbot.security.TenantContext;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class KnowledgeBaseService {

    private final KnowledgeBaseRepository knowledgeBaseRepository;

    @Transactional(readOnly = true)
    public Page<KnowledgeBase> getAllFAQs(Pageable pageable) {
        Long companyId = TenantContext.getCompanyId();
        return knowledgeBaseRepository.findByCompanyId(companyId, pageable);
    }

    @Transactional
    public KnowledgeBase createFAQ(KnowledgeBaseRequest request, Long userId) {
        Long companyId = TenantContext.getCompanyId();

        KnowledgeBase kb = new KnowledgeBase();
        kb.setCompanyId(companyId);
        kb.setQuestion(request.getQuestion());
        kb.setAnswer(request.getAnswer());
        kb.setIsVerified(request.getIsVerified());
        kb.setCreatedBy(userId);

        return knowledgeBaseRepository.save(kb);
    }

    @Transactional
    public KnowledgeBase updateFAQ(Long kbId, KnowledgeBaseRequest request) {
        Long companyId = TenantContext.getCompanyId();

        KnowledgeBase kb = knowledgeBaseRepository.findById(kbId)
                .orElseThrow(() -> new RuntimeException("FAQ not found"));

        // Verify tenant isolation
        if (!kb.getCompanyId().equals(companyId)) {
            throw new RuntimeException("Unauthorized access");
        }

        kb.setQuestion(request.getQuestion());
        kb.setAnswer(request.getAnswer());
        kb.setIsVerified(request.getIsVerified());

        return knowledgeBaseRepository.save(kb);
    }

    @Transactional
    public void deleteFAQ(Long kbId) {
        Long companyId = TenantContext.getCompanyId();

        KnowledgeBase kb = knowledgeBaseRepository.findById(kbId)
                .orElseThrow(() -> new RuntimeException("FAQ not found"));

        // Verify tenant isolation
        if (!kb.getCompanyId().equals(companyId)) {
            throw new RuntimeException("Unauthorized access");
        }

        knowledgeBaseRepository.delete(kb);
    }

    @Transactional
    public List<KnowledgeBase> uploadCsv(MultipartFile file, Long userId) {
        Long companyId = TenantContext.getCompanyId();
        List<KnowledgeBase> uploadedFaqs = new ArrayList<>();

        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8));
                CSVParser csvParser = new CSVParser(reader,
                        CSVFormat.DEFAULT.builder()
                                .setHeader()
                                .setSkipHeaderRecord(true)
                                .setIgnoreHeaderCase(true)
                                .setTrim(true)
                                .build())) {

            for (CSVRecord record : csvParser) {
                String question = record.get("question");
                String answer = record.get("answer");

                if (question == null || question.trim().isEmpty() ||
                        answer == null || answer.trim().isEmpty()) {
                    continue; // Skip empty rows
                }

                KnowledgeBase kb = new KnowledgeBase();
                kb.setCompanyId(companyId);
                kb.setQuestion(question.trim());
                kb.setAnswer(answer.trim());
                kb.setIsVerified(true); // Auto-verify CSV uploads
                kb.setCreatedBy(userId);

                KnowledgeBase saved = knowledgeBaseRepository.save(kb);
                uploadedFaqs.add(saved);
            }

        } catch (Exception e) {
            throw new RuntimeException("Failed to parse CSV file: " + e.getMessage(), e);
        }

        return uploadedFaqs;
    }
}
