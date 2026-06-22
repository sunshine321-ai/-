package edu.jxnu.microcoursebackend.controller;

import edu.jxnu.microcoursebackend.pojo.Result;
import edu.jxnu.microcoursebackend.service.FlashcardProgressService;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/flashcards")
public class FlashcardProgressController {
    private static final Long USER_ID = 1L;
    private final FlashcardProgressService service;

    public FlashcardProgressController(FlashcardProgressService service) {
        this.service = service;
    }

    @GetMapping
    public Result<List<Integer>> list() {
        return Result.success(service.list(USER_ID));
    }

    @PutMapping("/{cardIndex}")
    public Result<Void> markMastered(@PathVariable Integer cardIndex) {
        service.markMastered(USER_ID, cardIndex);
        return Result.success();
    }

    @DeleteMapping
    public Result<Void> clear() {
        service.clear(USER_ID);
        return Result.success();
    }
}
