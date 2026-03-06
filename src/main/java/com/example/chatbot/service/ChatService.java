package com.example.chatbot.service;

import com.example.chatbot.dto.ChatResponseDTO;
import com.example.chatbot.entity.ChatMessage;
import com.example.chatbot.entity.ChatSession;
import com.example.chatbot.repository.ChatMessageRepository;
import com.example.chatbot.repository.ChatSessionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class ChatService {

    private final ChatSessionRepository chatSessionRepository;
    private final ChatMessageRepository chatMessageRepository;
    private final GroqApiService groqApiService;

    private static final String SYSTEM_PROMPT = "You are a helpful and friendly customer service assistant. " +
            "Answer user questions clearly, concisely, and politely. " +
            "If you don't know the answer, politely say you will forward it to the relevant team.";

    @Transactional
    public ChatResponseDTO sendMessage(String sessionId, String userMessage) {
        // Find or create session
        ChatSession session = chatSessionRepository.findById(sessionId)
                .orElseGet(() -> chatSessionRepository.save(ChatSession.builder().id(sessionId).build()));

        // Save user message
        ChatMessage userMsg = ChatMessage.builder()
                .session(session)
                .message(userMessage)
                .role("user")
                .build();
        chatMessageRepository.save(userMsg);

        // Fetch history for AI context
        List<ChatMessage> history = chatMessageRepository.findBySessionIdOrderByTimestampAsc(sessionId);

        // Build messages for Groq API
        List<Map<String, String>> apiMessages = new ArrayList<>();
        
        // Add system prompt
        Map<String, String> sysMsg = new HashMap<>();
        sysMsg.put("role", "system");
        sysMsg.put("content", SYSTEM_PROMPT);
        apiMessages.add(sysMsg);

        // Add history
        for (ChatMessage msg : history) {
            Map<String, String> m = new HashMap<>();
            m.put("role", msg.getRole());
            m.put("content", msg.getMessage());
            apiMessages.add(m);
        }

        // Call Groq API
        String aiResponseContent = groqApiService.getChatCompletion(apiMessages);

        // Save bot response
        ChatMessage botMsg = ChatMessage.builder()
                .session(session)
                .message(aiResponseContent)
                .role("assistant")
                .build();
        botMsg = chatMessageRepository.save(botMsg);

        // Return DTO
        return ChatResponseDTO.builder()
                .response(botMsg.getMessage())
                .role(botMsg.getRole())
                .timestamp(botMsg.getTimestamp() != null ? botMsg.getTimestamp() : LocalDateTime.now())
                .build();
    }

    public List<ChatMessage> getChatHistory(String sessionId) {
        return chatMessageRepository.findBySessionIdOrderByTimestampAsc(sessionId);
    }

    @Transactional
    public void clearSession(String sessionId) {
        chatSessionRepository.deleteById(sessionId);
    }
}
