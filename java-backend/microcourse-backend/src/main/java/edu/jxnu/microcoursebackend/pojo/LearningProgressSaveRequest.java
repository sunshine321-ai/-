package edu.jxnu.microcoursebackend.pojo;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

import java.math.BigDecimal;

public record LearningProgressSaveRequest(
        @NotBlank(message = "章节标识不能为空") String chapterKey,
        @NotNull(message = "进度不能为空")
        @DecimalMin(value = "0", message = "进度不能小于 0")
        @DecimalMax(value = "100", message = "进度不能大于 100") BigDecimal progress,
        Boolean completed,
        String detailJson,
        @PositiveOrZero(message = "累计学习时长不能小于 0") Long durationSeconds
) {
    public LearningProgressSaveRequest(String chapterKey, BigDecimal progress, Boolean completed) {
        this(chapterKey, progress, completed, null, 0L);
    }
}
