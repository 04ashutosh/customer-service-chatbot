package com.enterprise.chatbot.controller;

import com.enterprise.chatbot.model.UnansweredQuestion;
import com.enterprise.chatbot.model.User;
import com.enterprise.chatbot.repository.UserRepository;
import com.enterprise.chatbot.service.UnansweredQuestionService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/admin/unanswered")
@RequiredArgsConstructor
public class UnansweredQuestionController {
    
    private final UnansweredQuestionService unansweredQuestionService;
    private final UserRepository userRepository;
    
    @GetMapping
    public ResponseEntity<Page<UnansweredQuestion>> getUnansweredQuestions(
            @RequestParam(defaultValue = "NEW") String status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        
        Sort sort = Sort.by("frequency").descending();
        Pageable pageable = PageRequest.of(page, size, sort);
        
        UnansweredQuestion.QuestionStatus questionStatus = UnansweredQuestion.QuestionStatus.valueOf(status);
        Page<UnansweredQuestion> questions = unansweredQuestionService.getUnansweredQuestions(questionStatus, pageable);
        
        return ResponseEntity.ok(questions);
    }
    
    @PostMapping("/{id}/approve")
    public ResponseEntity<Void> approveQuestion(@PathVariable Long id,
                                                 @RequestBody Map<String, String> body,
                                                 Authentication authentication) {
        try {
            String email = authentication.getName();
            User user = userRepository.findByEmail(email)
                    .orElseThrow(() -> new RuntimeException("User not found"));
            
            String answer = body.get("answer");
            unansweredQuestionService.approveAndConvertToFAQ(id, answer, user.getUserId());
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> rejectQuestion(@PathVariable Long id) {
        try {
            unansweredQuestionService.rejectQuestion(id);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
}
