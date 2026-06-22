package edu.jxnu.microcoursebackend.controller;

import edu.jxnu.microcoursebackend.pojo.LearningProgress;
import edu.jxnu.microcoursebackend.pojo.LearningProgressSaveRequest;
import edu.jxnu.microcoursebackend.pojo.Result;
import edu.jxnu.microcoursebackend.service.LearningProgressService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/progress")
public class LearningProgressController {
    private static final Long USER_ID = 1L;
    private final LearningProgressService service;

    public LearningProgressController(LearningProgressService service) { this.service = service; }

    @GetMapping
    public Result<List<LearningProgress>> list() { return Result.success(service.list(USER_ID)); }

    @PutMapping
    public Result<LearningProgress> save(@Valid @RequestBody LearningProgressSaveRequest request) {
        return Result.success(service.save(USER_ID, request));
    }

    @DeleteMapping
    public Result<Void> reset() { service.reset(USER_ID); return Result.success(); }
}
