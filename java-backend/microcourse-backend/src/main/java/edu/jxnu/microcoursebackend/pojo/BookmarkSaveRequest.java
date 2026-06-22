package edu.jxnu.microcoursebackend.pojo;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record BookmarkSaveRequest(
        @NotNull(message = "视频时间不能为空")
        @DecimalMin(value = "0", message = "视频时间不能小于 0") BigDecimal videoTime,
        @NotBlank(message = "书签名称不能为空") String label
) {
}
