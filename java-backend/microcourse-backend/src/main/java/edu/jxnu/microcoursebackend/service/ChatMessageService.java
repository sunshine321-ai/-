package edu.jxnu.microcoursebackend.service;

import edu.jxnu.microcoursebackend.pojo.ChatMessage;

import java.util.List;

public interface ChatMessageService {
    List<ChatMessage> list(Long userId, String context);
    ChatMessage append(Long userId, String context, String role, String content);
    void clear(Long userId, String context);
}
