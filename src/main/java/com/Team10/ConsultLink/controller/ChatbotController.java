package com.Team10.ConsultLink.controller;

import com.Team10.ConsultLink.service.ChatbotService;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController
@RequestMapping("/api/chatbot")
public class ChatbotController {

    private final ChatbotService chatbotService;

    // 1. Add the Constructor to inject your service
    public ChatbotController(ChatbotService chatbotService) {
        this.chatbotService = chatbotService;
    }

    @PostMapping("/ask")
    public Map<String, String> askChatbot(@RequestBody Map<String, String> payload) {
        String userMessage = payload.get("message");

        // 2. DELETE the hardcoded 'being trained' string
        // 3. CALL the actual service logic
        String aiResponse = chatbotService.getResponse(userMessage);

        return Map.of("response", aiResponse);
    }
}
