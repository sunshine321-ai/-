package edu.jxnu.microcoursebackend.pojo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ChatMessage {
    private Long id;
    private Long userId;
    private String context;
    private String role;
    private String content;
    private LocalDateTime createdAt;
}
