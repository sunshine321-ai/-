package edu.jxnu.microcoursebackend.pojo;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class Bookmark {
    private Long id;
    private Long userId;
    private BigDecimal videoTime;
    private String label;
    private LocalDateTime createdAt;
}
