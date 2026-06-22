package edu.jxnu.microcoursebackend.pojo;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

public record ChatSyncRequest(
        @NotBlank(message = "聊天场景不能为空") String context,
        @NotEmpty(message = "聊天消息不能为空") List<@Valid Message> messages
) {
    public record Message(
            @NotBlank(message = "消息角色不能为空") String role,
            @NotBlank(message = "消息内容不能为空") String content
    ) {
    }
}
