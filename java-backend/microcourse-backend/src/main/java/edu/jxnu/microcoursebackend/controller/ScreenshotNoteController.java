package edu.jxnu.microcoursebackend.controller;

import edu.jxnu.microcoursebackend.pojo.Result;
import edu.jxnu.microcoursebackend.pojo.ScreenshotNote;
import edu.jxnu.microcoursebackend.pojo.ScreenshotSaveRequest;
import edu.jxnu.microcoursebackend.pojo.ScreenshotNoteUpdateRequest;
import edu.jxnu.microcoursebackend.pojo.ScreenshotAnalyzeRequest;
import edu.jxnu.microcoursebackend.service.AiService;
import edu.jxnu.microcoursebackend.service.ScreenshotNoteService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/screenshots")
public class ScreenshotNoteController {
    private static final Long DEFAULT_USER_ID = 1L;
    private final ScreenshotNoteService screenshotNoteService;
    private final AiService aiService;

    public ScreenshotNoteController(ScreenshotNoteService screenshotNoteService, AiService aiService) {
        this.screenshotNoteService = screenshotNoteService;
        this.aiService = aiService;
    }

    @GetMapping
    public Result<List<ScreenshotNote>> list() {
        return Result.success(screenshotNoteService.listByUserId(DEFAULT_USER_ID));
    }

    @PostMapping
    public Result<ScreenshotNote> save(@Valid @RequestBody ScreenshotSaveRequest request) {
        return Result.success(screenshotNoteService.save(DEFAULT_USER_ID, request.image(), request.videoTime()));
    }

    @PostMapping("/analyze")
    public Result<String> analyze(@Valid @RequestBody ScreenshotAnalyzeRequest request) {
        return Result.success(aiService.analyzeScreenshot(request.imagePath()));
    }

    @PutMapping("/{id}")
    public Result<ScreenshotNote> update(
            @PathVariable Long id,
            @RequestBody ScreenshotNoteUpdateRequest request
    ) {
        return Result.success(screenshotNoteService.update(
                DEFAULT_USER_ID,
                id,
                request.note(),
                request.aiAnalysis()
        ));
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        screenshotNoteService.delete(DEFAULT_USER_ID, id);
        return Result.success();
    }
}
