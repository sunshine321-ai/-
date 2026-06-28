package edu.jxnu.microcoursebackend.service.impl;

import edu.jxnu.microcoursebackend.pojo.VideoChapterFrame;
import edu.jxnu.microcoursebackend.service.AiService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class AiServiceImpl implements AiService {
    private static final String DEFAULT_PROMPT = """
            你是一名耐心的计算机视觉微课助教，专门讲解卷积核、图像处理、
            边缘检测和 CNN 基础知识。请使用简体中文，先给结论，再清晰解释。
            """;

    private final ObjectMapper objectMapper;
    private final HttpClient httpClient;
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
        this.apiKey = apiKey == null ? "" : apiKey.trim();
        this.baseUrl = baseUrl;
        this.textModel = textModel;
        this.visionModel = visionModel;
        this.screenshotDirectory = Path.of(storageRoot).toAbsolutePath().normalize().resolve("screenshots");
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(15))
                .build();
    }

    @Override
    public boolean isConfigured() {
        return !apiKey.isBlank();
    }

    @Override
    public String chat(String message, String systemPrompt) {
        if (!isConfigured()) {
            return "AI 服务还没有配置 API Key，请先设置 DASHSCOPE_API_KEY。";
        }
        String prompt = systemPrompt == null || systemPrompt.isBlank() ? DEFAULT_PROMPT : systemPrompt;
        List<Map<String, Object>> messages = List.of(
                Map.of("role", "system", "content", prompt),
                Map.of("role", "user", "content", message)
        );
        return requestCompletion(textModel, messages);
    }

    @Override
    public String analyzeScreenshot(String imageUrl) {
        if (!isConfigured()) {
            return "AI 服务还没有配置 API Key，请先设置 DASHSCOPE_API_KEY。";
        }
        String imageData = resolveImageForVision(imageUrl);
        List<Map<String, Object>> messages = List.of(
                Map.of("role", "system", "content", DEFAULT_PROMPT),
                Map.of("role", "user", "content", List.of(
                        Map.of("type", "text", "text", "请分析这张微课截图，说明其中涉及的卷积核或图像处理知识点，并给出适合学生复习的简短解释。"),
                        Map.of("type", "image_url", "image_url", Map.of("url", imageData))
                ))
        );
        return requestCompletion(visionModel, messages);
    }

    @Override
    public String analyzeVideoChapters(String videoTitle, Long durationSeconds, List<VideoChapterFrame> frames) {
        if (!isConfigured()) {
            return fallbackChapters(durationSeconds);
        }
        List<Object> content = new ArrayList<>();
        content.add(Map.of(
                "type", "text",
                "text", buildVideoChapterPrompt(videoTitle, durationSeconds)
        ));
        if (frames != null) {
            for (VideoChapterFrame frame : frames) {
                if (frame == null || frame.image() == null || frame.image().isBlank()) {
                    continue;
                }
                content.add(Map.of("type", "text", "text", "时间点：" + safeTime(frame.time()) + " 秒"));
                content.add(Map.of("type", "image_url", "image_url", Map.of("url", frame.image())));
            }
        }
        List<Map<String, Object>> messages = List.of(
                Map.of("role", "system", "content", "你是视频课程章节生成器。只返回严格 JSON，不要返回 Markdown。"),
                Map.of("role", "user", "content", content)
        );
        return requestCompletion(visionModel, messages);
    }

    private String buildVideoChapterPrompt(String videoTitle, Long durationSeconds) {
        String title = videoTitle == null || videoTitle.isBlank() ? "卷积核微课" : videoTitle;
        long duration = durationSeconds == null ? 0 : Math.max(0, durationSeconds);
        return """
                请根据这些视频关键帧，为微课自动生成章节目录。
                视频标题：%s
                视频时长：%d 秒
                要求：
                1. 只返回 JSON，不要解释，不要 Markdown。
                2. JSON 格式为：{"chapters":[{"time":0,"label":"章节标题","icon":"📌"}]}
                3. time 必须是秒数整数，label 使用简体中文。
                4. 章节数量控制在 5 到 8 个，章节标题要能概括对应时间段的知识点。
                5. 如果画面中出现卷积核、灰度图、边缘检测、CNN 等内容，要优先识别这些主题。
                """.formatted(title, duration);
    }

    private String requestCompletion(String model, List<Map<String, Object>> messages) {
        try {
            Map<String, Object> payload = new LinkedHashMap<>();
            payload.put("model", model);
            payload.put("messages", messages);
            payload.put("temperature", 0.3);

            String body = objectMapper.writeValueAsString(payload);
            HttpRequest request = HttpRequest.newBuilder(URI.create(baseUrl))
                    .timeout(Duration.ofSeconds(60))
                    .header("Authorization", "Bearer " + apiKey)
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(body))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() < 200 || response.statusCode() >= 300) {
                throw new IllegalStateException("AI 请求失败：HTTP " + response.statusCode());
            }
            return extractContent(response.body());
        } catch (JacksonException exception) {
            throw new IllegalStateException("AI 请求参数无法转换为 JSON", exception);
        } catch (IOException exception) {
            throw new IllegalStateException("AI 请求失败", exception);
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("AI 请求被中断", exception);
        }
    }

    private String extractContent(String body) {
        try {
            JsonNode root = objectMapper.readTree(body);
            JsonNode content = root.at("/choices/0/message/content");
            if (!content.isMissingNode() && !content.isNull()) {
                return content.asText();
            }
            JsonNode text = root.at("/output/text");
            if (!text.isMissingNode() && !text.isNull()) {
                return text.asText();
            }
        } catch (JacksonException exception) {
            throw new IllegalStateException("AI 返回内容不是合法 JSON", exception);
        }
        throw new IllegalStateException("AI 返回内容中没有 message content");
    }

    private String resolveImageForVision(String imageUrl) {
        if (imageUrl == null || imageUrl.isBlank()) {
            throw new IllegalArgumentException("图片路径不能为空");
        }
        if (imageUrl.startsWith("data:image/") || imageUrl.startsWith("http://") || imageUrl.startsWith("https://")) {
            return imageUrl;
        }
        String filename = Path.of(imageUrl).getFileName().toString();
        Path imagePath = screenshotDirectory.resolve(filename).normalize();
        if (!imagePath.startsWith(screenshotDirectory)) {
            throw new IllegalArgumentException("图片路径不合法");
        }
        if (!Files.exists(imagePath)) {
            throw new IllegalArgumentException("截图文件不存在");
        }
        try {
            String contentType = Files.probeContentType(imagePath);
            if (contentType == null || !contentType.startsWith("image/")) {
                contentType = "image/png";
            }
            return "data:" + contentType + ";base64," + Base64.getEncoder().encodeToString(Files.readAllBytes(imagePath));
        } catch (IOException exception) {
            throw new IllegalStateException("读取截图文件失败", exception);
        }
    }

    private String fallbackChapters(Long durationSeconds) {
        long duration = durationSeconds == null || durationSeconds <= 0 ? 480 : durationSeconds;
        long step = Math.max(60, duration / 5);
        return """
                {"chapters":[
                  {"time":0,"label":"课程导入：卷积核是什么","icon":"🎬"},
                  {"time":%d,"label":"像素、灰度与图像矩阵","icon":"🔢"},
                  {"time":%d,"label":"卷积核滑动计算过程","icon":"🧮"},
                  {"time":%d,"label":"边缘检测与滤波效果","icon":"↔️"},
                  {"time":%d,"label":"卷积核在 CNN 中的作用","icon":"🧠"}
                ]}
                """.formatted(step, step * 2, step * 3, Math.min(duration - 1, step * 4));
    }

    private Long safeTime(Long value) {
        return value == null ? 0L : Math.max(0L, value);
    }
}
