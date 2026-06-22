package edu.jxnu.microcoursebackend.service.impl;

import edu.jxnu.microcoursebackend.service.AiService;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.Base64;
import java.util.List;
import java.util.Map;

@Service
public class AiServiceImpl implements AiService {
    private static final String DEFAULT_PROMPT = "你是一位耐心的计算机视觉助教，专门讲解卷积核相关知识。请使用简体中文，先给结论，再清晰解释。";
    private final ObjectMapper objectMapper;
    private final HttpClient httpClient = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(10)).build();
    private final String apiKey;
    private final String baseUrl;
    private final String textModel;
    private final String visionModel;
    private final Path screenshotDirectory;

    public AiServiceImpl(
            ObjectMapper objectMapper,
            @Value("${app.ai.api-key:}") String apiKey,
            @Value("${app.ai.base-url:https://dashscope.aliyuncs.com/compatible-mode/v1/chat/completions}") String baseUrl,
            @Value("${app.ai.text-model:qwen-plus}") String textModel,
            @Value("${app.ai.vision-model:qwen-vl-plus}") String visionModel,
            @Value("${app.storage.root:storage}") String storageRoot
    ) {
        this.objectMapper = objectMapper;
        this.apiKey = apiKey;
        this.baseUrl = baseUrl;
        this.textModel = textModel;
        this.visionModel = visionModel;
        this.screenshotDirectory = Path.of(storageRoot).toAbsolutePath().normalize().resolve("screenshots");
    }

    @Override
    public boolean isConfigured() {
        return apiKey != null && !apiKey.isBlank();
    }

    @Override
    public String chat(String message, String systemPrompt) {
        requireApiKey();
        Map<String, Object> body = Map.of(
                "model", textModel,
                "messages", List.of(
                        Map.of("role", "system", "content", systemPrompt == null || systemPrompt.isBlank() ? DEFAULT_PROMPT : systemPrompt),
                        Map.of("role", "user", "content", message)
                ),
                "temperature", 0.7
        );
        return send(body);
    }

    @Override
    public String analyzeScreenshot(String imageUrl) {
        requireApiKey();
        Path imagePath = screenshotDirectory.resolve(Path.of(imageUrl).getFileName()).normalize();
        if (!imagePath.startsWith(screenshotDirectory) || !Files.isRegularFile(imagePath)) {
            throw new IllegalArgumentException("截图文件不存在");
        }
        try {
            String mime = Files.probeContentType(imagePath);
            if (mime == null) mime = "image/png";
            String dataUrl = "data:" + mime + ";base64," + Base64.getEncoder().encodeToString(Files.readAllBytes(imagePath));
            Map<String, Object> body = Map.of(
                    "model", visionModel,
                    "messages", List.of(Map.of(
                            "role", "user",
                            "content", List.of(
                                    Map.of("type", "image_url", "image_url", Map.of("url", dataUrl)),
                                    Map.of("type", "text", "text", "请分析这张卷积核微课截图，概括知识点并给出学习建议。")
                            )
                    ))
            );
            return send(body);
        } catch (IOException exception) {
            throw new IllegalStateException("读取截图文件失败", exception);
        }
    }

    private String send(Map<String, Object> body) {
        try {
            HttpRequest request = HttpRequest.newBuilder(URI.create(baseUrl))
                    .timeout(Duration.ofSeconds(60))
                    .header("Authorization", "Bearer " + apiKey)
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(objectMapper.writeValueAsString(body)))
                    .build();
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() < 200 || response.statusCode() >= 300) {
                throw new IllegalStateException("AI 服务请求失败，状态码：" + response.statusCode());
            }
            JsonNode root = objectMapper.readTree(response.body());
            JsonNode content = root.at("/choices/0/message/content");
            if (!content.isTextual()) {
                throw new IllegalStateException("AI 服务返回的数据格式无法识别");
            }
            return content.asText();
        } catch (IOException exception) {
            throw new IllegalStateException("AI 服务连接失败", exception);
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("AI 服务请求被中断", exception);
        }
    }

    private void requireApiKey() {
        if (!isConfigured()) {
            throw new IllegalStateException("未配置 DASHSCOPE_API_KEY");
        }
    }
}
