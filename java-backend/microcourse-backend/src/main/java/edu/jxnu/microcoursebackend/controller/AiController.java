package edu.jxnu.microcoursebackend.controller;

import edu.jxnu.microcoursebackend.pojo.AiChatRequest;
import edu.jxnu.microcoursebackend.pojo.Result;
import edu.jxnu.microcoursebackend.pojo.VideoChapterAnalyzeRequest;
import edu.jxnu.microcoursebackend.service.AiService;
import edu.jxnu.microcoursebackend.service.ChatMessageService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/ai")
public class AiController {
    private static final Long USER_ID = 1L;
    private final AiService aiService;
    private final ChatMessageService chatMessageService;

    public AiController(AiService aiService, ChatMessageService chatMessageService) {
        this.aiService = aiService;
        this.chatMessageService = chatMessageService;
    }

    @PostMapping("/chat")
    public Result<Map<String, String>> chat(@RequestBody AiChatRequest request) {
        String message = request.message() != null ? request.message() : request.question();
        if (message == null || message.isBlank()) {
            throw new IllegalArgumentException("消息不能为空");
        }
        String context = request.context() == null || request.context().isBlank() ? "study" : request.context();
        String answer = aiService.chat(message.trim(), request.effectiveSystemPrompt());
        chatMessageService.append(USER_ID, context, "user", message.trim());
        chatMessageService.append(USER_ID, context, "assistant", answer);
        return Result.success(Map.of("message", answer));
    }

    @PostMapping("/video-chapters")
    public Result<Map<String, String>> analyzeVideoChapters(@RequestBody VideoChapterAnalyzeRequest request) {
        String chapters = aiService.analyzeVideoChapters(
                request.videoTitle(),
                request.durationSeconds(),
                request.frames()
        );
        return Result.success(Map.of("chapters", chapters));
    }
}
