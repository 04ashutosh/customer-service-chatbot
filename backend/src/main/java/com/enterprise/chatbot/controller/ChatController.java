package com.enterprise.chatbot.controller;

import com.enterprise.chatbot.dto.ChatRequest;
import com.enterprise.chatbot.dto.ChatResponse;
import com.enterprise.chatbot.model.User;
import com.enterprise.chatbot.repository.UserRepository;
import com.enterprise.chatbot.service.ChatbotService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/chat")
@RequiredArgsConstructor
public class ChatController {
    
    private final ChatbotService chatbotService;
    private final UserRepository userRepository;
    
    @PostMapping
    public ResponseEntity<ChatResponse> chat(@Valid @RequestBody ChatRequest request, 
                                              Authentication authentication) {
        try {
            String email = authentication.getName();
            User user = userRepository.findByEmail(email)
                    .orElseThrow(() -> new RuntimeException("User not found"));
            
            ChatResponse response = chatbotService.processQuestion(request, user.getUserId());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(
                    ChatResponse.builder()
                            .answer("An error occurred processing your question")
                            .answered(false)
                            .build()
            );
        }
    }
}
