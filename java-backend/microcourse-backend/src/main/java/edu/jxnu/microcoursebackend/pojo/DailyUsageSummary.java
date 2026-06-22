package edu.jxnu.microcoursebackend.pojo;

import lombok.Data;

import java.time.LocalDate;

@Data
public class DailyUsageSummary {
    private LocalDate usageDate;
    private String moduleKey;
    private Long visitCount;
    private Long durationSeconds;
}
