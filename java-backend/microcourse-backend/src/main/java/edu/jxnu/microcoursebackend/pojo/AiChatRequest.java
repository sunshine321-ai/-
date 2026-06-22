package edu.jxnu.microcoursebackend.pojo;

public record AiChatRequest(
        String message,
        String question,
        String context,
        String systemPrompt,
        String system_prompt
) {
    public String effectiveSystemPrompt() {
        return systemPrompt != null ? systemPrompt : system_prompt;
    }
}
