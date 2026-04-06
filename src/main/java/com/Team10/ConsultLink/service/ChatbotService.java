package com.Team10.ConsultLink.service;

import org.springframework.ai.openai.OpenAiChatModel; 
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import jakarta.annotation.PostConstruct;
import java.nio.charset.StandardCharsets;

@Service
public class ChatbotService {

    private final OpenAiChatModel chatModel;

 @Value("classpath:prompts/platform-policies.md")
private Resource policyResource;

    private String systemPrompt;

    public ChatbotService(OpenAiChatModel chatModel) {
        this.chatModel = chatModel;
    }

    @PostConstruct
    public void init() {
        try {
            String policies = policyResource.getContentAsString(StandardCharsets.UTF_8);
            this.systemPrompt = "You are the ConsultLink assistant. Use these policies to answer: " + policies;
            System.out.println("✅ DEBUG: Groq System Prompt loaded with policies.");
        } catch (Exception e) {
            this.systemPrompt = "You are a helpful assistant.";
            System.err.println("❌ DEBUG: Failed to load policies.md: " + e.getMessage());
        }
    }

public String getResponse(String userMessage) {
    System.out.println("🤖 DEBUG: Sending request to Groq...");
    try {
        // We add a simple manual timeout check or just a log to see if we get past this line
        String prompt = systemPrompt + "\nUser: " + userMessage;
        String response = chatModel.call(prompt); 
        
        System.out.println("🤖 DEBUG: Groq responded successfully.");
        return response;
    } catch (Exception e) {
        System.err.println("❌ DEBUG: Groq Call Failed: " + e.getMessage());
        return "Error: Could not reach the AI engine. " + e.getMessage();
    }
}
}