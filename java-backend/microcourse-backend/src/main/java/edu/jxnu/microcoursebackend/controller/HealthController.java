package edu.jxnu.microcoursebackend.controller;

import edu.jxnu.microcoursebackend.pojo.Result;
import edu.jxnu.microcoursebackend.service.AiService;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/v1")
public class HealthController {
    private final JdbcTemplate jdbcTemplate;
    private final AiService aiService;

    public HealthController(JdbcTemplate jdbcTemplate, AiService aiService) {
        this.jdbcTemplate = jdbcTemplate;
        this.aiService = aiService;
    }

    @GetMapping("/health")
    public Result<Map<String, Object>> health() {
        Integer value = jdbcTemplate.queryForObject("select 1", Integer.class);
        return Result.success(Map.of(
                "status", "ok",
                "databaseReady", value != null && value == 1,
                "apiConfigured", aiService.isConfigured(),
                "implementation", "spring-boot"
        ));
    }
}
