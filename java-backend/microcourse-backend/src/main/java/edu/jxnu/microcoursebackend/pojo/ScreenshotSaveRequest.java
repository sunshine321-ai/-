package edu.jxnu.microcoursebackend.pojo;

import jakarta.validation.constraints.NotBlank;

import java.math.BigDecimal;

public record ScreenshotSaveRequest(
        @NotBlank String image,
        BigDecimal videoTime
) {
}
