package edu.jxnu.microcoursebackend.pojo;

import jakarta.validation.constraints.NotBlank;

public record WrongQuestionSaveRequest(
        @NotBlank(message = "题目不能为空") String question,
        String optionsJson,
        String correctAnswer,
        String userAnswer,
        String note
) {
}
