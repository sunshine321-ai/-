package edu.jxnu.microcoursebackend.service;

public interface AiService {
    boolean isConfigured();

    String chat(String message, String systemPrompt);

    String analyzeScreenshot(String imageUrl);
}
