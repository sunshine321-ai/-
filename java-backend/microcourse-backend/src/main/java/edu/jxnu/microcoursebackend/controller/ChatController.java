package edu.jxnu.microcoursebackend.controller;

import edu.jxnu.microcoursebackend.pojo.ChatMessage;
import edu.jxnu.microcoursebackend.pojo.ChatSyncRequest;
import edu.jxnu.microcoursebackend.pojo.Result;
import edu.jxnu.microcoursebackend.service.ChatMessageService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/chats")
public class ChatController {
    private static final Long USER_ID = 1L;
    private final ChatMessageService service;

    public ChatController(ChatMessageService service) { this.service = service; }

    @GetMapping("/{context}")
    public Result<List<ChatMessage>> list(@PathVariable String context) {
        return Result.success(service.list(USER_ID, context));
    }

    @PutMapping
    public Result<Void> sync(@Valid @RequestBody ChatSyncRequest request) {
        service.sync(USER_ID, request.context(), request.messages());
        return Result.success();
    }

    @DeleteMapping("/{context}")
    public Result<Void> clear(@PathVariable String context) {
        service.clear(USER_ID, context);
        return Result.success();
    }
}
