package com.enterprise.chatbot.ai;

import com.enterprise.chatbot.model.KnowledgeBase;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class RetrievalResult {
    private List<KnowledgeBase> results;
    private double bestScore;
    private boolean hasHighConfidenceMatch;
}
