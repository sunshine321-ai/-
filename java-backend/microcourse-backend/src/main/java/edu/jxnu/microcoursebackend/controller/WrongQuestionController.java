package edu.jxnu.microcoursebackend.controller;

import edu.jxnu.microcoursebackend.pojo.Result;
import edu.jxnu.microcoursebackend.pojo.WrongQuestion;
import edu.jxnu.microcoursebackend.pojo.WrongQuestionSaveRequest;
import edu.jxnu.microcoursebackend.service.WrongQuestionService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/wrong-questions")
public class WrongQuestionController {
    private static final Long USER_ID = 1L;
    private final WrongQuestionService service;

    public WrongQuestionController(WrongQuestionService service) {
        this.service = service;
    }

    @GetMapping
    public Result<List<WrongQuestion>> list() { return Result.success(service.list(USER_ID)); }

    @PostMapping
    public Result<WrongQuestion> create(@Valid @RequestBody WrongQuestionSaveRequest request) {
        return Result.success(service.create(USER_ID, request));
    }

    @PutMapping("/{id}")
    public Result<WrongQuestion> update(@PathVariable Long id, @Valid @RequestBody WrongQuestionSaveRequest request) {
        return Result.success(service.update(USER_ID, id, request));
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) { service.delete(USER_ID, id); return Result.success(); }

    @DeleteMapping
    public Result<Void> clear() { service.clear(USER_ID); return Result.success(); }
}
