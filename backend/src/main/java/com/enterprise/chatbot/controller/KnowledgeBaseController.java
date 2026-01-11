package com.enterprise.chatbot.controller;

import com.enterprise.chatbot.dto.KnowledgeBaseRequest;
import com.enterprise.chatbot.model.KnowledgeBase;
import com.enterprise.chatbot.model.User;
import com.enterprise.chatbot.repository.UserRepository;
import com.enterprise.chatbot.service.KnowledgeBaseService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/admin/kb")
@RequiredArgsConstructor
public class KnowledgeBaseController {

    private final KnowledgeBaseService knowledgeBaseService;
    private final UserRepository userRepository;

    @GetMapping
    public ResponseEntity<Page<KnowledgeBase>> getAllFAQs(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "DESC") String sortDir) {

        Sort sort = sortDir.equalsIgnoreCase("ASC") ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sort);

        Page<KnowledgeBase> faqs = knowledgeBaseService.getAllFAQs(pageable);
        return ResponseEntity.ok(faqs);
    }

    @PostMapping
    public ResponseEntity<KnowledgeBase> createFAQ(@Valid @RequestBody KnowledgeBaseRequest request,
            Authentication authentication) {
        try {
            String email = authentication.getName();
            User user = userRepository.findByEmail(email)
                    .orElseThrow(() -> new RuntimeException("User not found"));

            KnowledgeBase kb = knowledgeBaseService.createFAQ(request, user.getUserId());
            return ResponseEntity.ok(kb);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<KnowledgeBase> updateFAQ(@PathVariable Long id,
            @Valid @RequestBody KnowledgeBaseRequest request) {
        try {
            KnowledgeBase kb = knowledgeBaseService.updateFAQ(id, request);
            return ResponseEntity.ok(kb);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteFAQ(@PathVariable Long id) {
        try {
            knowledgeBaseService.deleteFAQ(id);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PostMapping("/upload-csv")
    public ResponseEntity<?> uploadCsv(@RequestParam("file") MultipartFile file,
            Authentication authentication) {
        try {
            if (file.isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("error", "File is empty"));
            }

            if (!file.getOriginalFilename().endsWith(".csv")) {
                return ResponseEntity.badRequest().body(Map.of("error", "File must be a CSV"));
            }

            String email = authentication.getName();
            User user = userRepository.findByEmail(email)
                    .orElseThrow(() -> new RuntimeException("User not found"));

            List<KnowledgeBase> uploadedFaqs = knowledgeBaseService.uploadCsv(file, user.getUserId());

            Map<String, Object> response = new HashMap<>();
            response.put("message", "CSV uploaded successfully");
            response.put("count", uploadedFaqs.size());
            response.put("faqs", uploadedFaqs);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to upload CSV: " + e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }
}
