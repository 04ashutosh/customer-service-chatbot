package com.enterprise.chatbot.ai;

import com.enterprise.chatbot.model.KnowledgeBase;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class PromptBuilder {
    
    public String buildPrompt(String companyName, String userQuestion, List<KnowledgeBase> contextFAQs) {
        StringBuilder prompt = new StringBuilder();
        
        prompt.append("You are a helpful customer support assistant for ").append(companyName).append(".\n\n");
        
        if (!contextFAQs.isEmpty()) {
            prompt.append("Here is the relevant information from our knowledge base:\n\n");
            
            for (int i = 0; i < contextFAQs.size(); i++) {
                KnowledgeBase faq = contextFAQs.get(i);
                prompt.append(String.format("[FAQ %d]\n", i + 1));
                prompt.append("Q: ").append(faq.getQuestion()).append("\n");
                prompt.append("A: ").append(faq.getAnswer()).append("\n\n");
            }
        }
        
        prompt.append("Customer Question: ").append(userQuestion).append("\n\n");
        
        prompt.append("IMPORTANT RULES:\n");
        prompt.append("1. Answer ONLY using the information provided in the knowledge base above\n");
        prompt.append("2. If the answer is not in the knowledge base, respond EXACTLY with: \"I don't have that information yet.\"\n");
        prompt.append("3. Do not make assumptions or provide information outside the knowledge base\n");
        prompt.append("4. Be helpful, professional, and concise\n");
        prompt.append("5. If you use information from the FAQs, make sure it directly answers the customer's question\n\n");
        
        prompt.append("Your response:");
        
        return prompt.toString();
    }
}
