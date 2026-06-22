package edu.jxnu.microcoursebackend.pojo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ModuleUsageSummary {
    private String moduleKey;
    private Long visitCount;
    private Long durationSeconds;
    private LocalDateTime lastVisitedAt;
}
