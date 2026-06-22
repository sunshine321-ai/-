package edu.jxnu.microcoursebackend.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ScreenshotNote {
    private Long id;
    private Long userId;
    private String imageUrl;
    private BigDecimal videoTime;
    private String note;
    private String aiAnalysis;
    private LocalDateTime createdAt;
}
