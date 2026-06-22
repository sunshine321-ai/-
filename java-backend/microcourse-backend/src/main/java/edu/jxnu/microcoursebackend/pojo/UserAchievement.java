package edu.jxnu.microcoursebackend.pojo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class UserAchievement {
    private Long id;
    private Long userId;
    private String achievementKey;
    private LocalDateTime unlockedAt;
}
