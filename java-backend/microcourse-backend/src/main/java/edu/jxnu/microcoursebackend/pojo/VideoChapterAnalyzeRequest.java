package edu.jxnu.microcoursebackend.pojo;

import java.util.List;

public record VideoChapterAnalyzeRequest(
        String videoTitle,
        Long durationSeconds,
        List<VideoChapterFrame> frames
) {
}
