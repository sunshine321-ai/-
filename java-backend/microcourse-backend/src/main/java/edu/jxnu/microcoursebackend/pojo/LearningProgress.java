package edu.jxnu.microcoursebackend.pojo;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class LearningProgress {
    private Long id;
    private Long userId;
    private String chapterKey;
    private BigDecimal progress;
    private Boolean completed;
    private String detailJson;
    private Long durationSeconds;
    private LocalDateTime updatedAt;
}
