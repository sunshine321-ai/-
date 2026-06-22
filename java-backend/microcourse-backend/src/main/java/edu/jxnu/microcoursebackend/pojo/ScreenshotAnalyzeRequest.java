package edu.jxnu.microcoursebackend.pojo;

import jakarta.validation.constraints.NotBlank;

public record ScreenshotAnalyzeRequest(
        @NotBlank(message = "图片路径不能为空") String imagePath
) {
}
