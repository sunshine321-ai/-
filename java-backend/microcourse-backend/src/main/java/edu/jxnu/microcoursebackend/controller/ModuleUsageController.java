package edu.jxnu.microcoursebackend.controller;

import edu.jxnu.microcoursebackend.pojo.ModuleUsageEventRequest;
import edu.jxnu.microcoursebackend.pojo.Result;
import edu.jxnu.microcoursebackend.service.ModuleUsageService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/usage")
public class ModuleUsageController {
    private static final Long USER_ID = 1L;
    private final ModuleUsageService service;

    public ModuleUsageController(ModuleUsageService service) {
        this.service = service;
    }

    @PostMapping("/events")
    public Result<Void> record(@Valid @RequestBody ModuleUsageEventRequest request) {
        service.record(USER_ID, request);
        return Result.success();
    }

    @GetMapping("/summary")
    public Result<Map<String, List<?>>> summary() {
        return Result.success(service.summary(USER_ID));
    }
}
