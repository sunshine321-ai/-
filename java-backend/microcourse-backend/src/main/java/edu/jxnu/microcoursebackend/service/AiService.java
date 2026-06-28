package edu.jxnu.microcoursebackend.service;

import edu.jxnu.microcoursebackend.pojo.VideoChapterFrame;

import java.util.List;

public interface AiService {
    boolean isConfigured();

    String chat(String message, String systemPrompt);

    String analyzeScreenshot(String imageUrl);

    String analyzeVideoChapters(String videoTitle, Long durationSeconds, List<VideoChapterFrame> frames);
}
