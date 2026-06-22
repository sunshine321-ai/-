package edu.jxnu.microcoursebackend.pojo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class WrongQuestion {
    private Long id;
    private Long userId;
    private String question;
    private String optionsJson;
    private String correctAnswer;
    private String userAnswer;
    private String note;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
