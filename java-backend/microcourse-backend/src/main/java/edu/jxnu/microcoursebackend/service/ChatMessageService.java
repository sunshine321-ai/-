package edu.jxnu.microcoursebackend.service;

import edu.jxnu.microcoursebackend.pojo.ChatMessage;
import edu.jxnu.microcoursebackend.pojo.ChatSyncRequest;

import java.util.List;

public interface ChatMessageService {
    List<ChatMessage> list(Long userId, String context);
    void sync(Long userId, String context, List<ChatSyncRequest.Message> messages);
    ChatMessage append(Long userId, String context, String role, String content);
    void clear(Long userId, String context);
}
