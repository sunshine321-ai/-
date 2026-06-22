package edu.jxnu.microcoursebackend.service.impl;

import edu.jxnu.microcoursebackend.mapper.ChatMessageMapper;
import edu.jxnu.microcoursebackend.pojo.ChatMessage;
import edu.jxnu.microcoursebackend.pojo.ChatSyncRequest;
import edu.jxnu.microcoursebackend.service.ChatMessageService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;

@Service
public class ChatMessageServiceImpl implements ChatMessageService {
    private static final Set<String> CONTEXTS = Set.of(
            "home",
            "study",
            "tutor",
            "study_hint",
            "study_correction",
            "study_evaluation",
            "study_summary"
    );
    private static final Set<String> ROLES = Set.of("user", "assistant");
    private final ChatMessageMapper mapper;

    public ChatMessageServiceImpl(ChatMessageMapper mapper) {
        this.mapper = mapper;
    }

    public List<ChatMessage> list(Long userId, String context) {
        validateContext(context);
        return mapper.findByContext(userId, context);
    }

    @Transactional
    public void sync(Long userId, String context, List<ChatSyncRequest.Message> messages) {
        validateContext(context);
        mapper.deleteByContext(userId, context);
        messages.forEach(message -> append(userId, context, message.role(), message.content()));
    }

    public ChatMessage append(Long userId, String context, String role, String content) {
        validateContext(context);
        if (!ROLES.contains(role)) {
            throw new IllegalArgumentException("消息角色只能是 user 或 assistant");
        }
        ChatMessage item = new ChatMessage();
        item.setUserId(userId);
        item.setContext(context);
        item.setRole(role);
        item.setContent(content);
        mapper.insert(item);
        return item;
    }

    public void clear(Long userId, String context) {
        validateContext(context);
        mapper.deleteByContext(userId, context);
    }

    private void validateContext(String context) {
        if (!CONTEXTS.contains(context)) {
            throw new IllegalArgumentException("不支持的聊天场景：" + context);
        }
    }
}
