package edu.jxnu.microcoursebackend.pojo;

import jakarta.validation.constraints.NotNull;

public record StudyNoteSaveRequest(@NotNull String content) {
}
