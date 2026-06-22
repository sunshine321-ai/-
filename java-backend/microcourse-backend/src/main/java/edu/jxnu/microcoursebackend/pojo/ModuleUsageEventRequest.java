package edu.jxnu.microcoursebackend.pojo;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PositiveOrZero;

public record ModuleUsageEventRequest(
        @NotBlank(message = "模块标识不能为空") String moduleKey,
        @NotBlank(message = "事件类型不能为空") String eventType,
        @PositiveOrZero(message = "使用时长不能小于 0") Integer durationSeconds
) {
}
