package edu.jxnu.microcoursebackend.controller;

import edu.jxnu.microcoursebackend.pojo.Bookmark;
import edu.jxnu.microcoursebackend.pojo.BookmarkSaveRequest;
import edu.jxnu.microcoursebackend.pojo.Result;
import edu.jxnu.microcoursebackend.service.BookmarkService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/bookmarks")
public class BookmarkController {
    private static final Long USER_ID = 1L;
    private final BookmarkService service;

    public BookmarkController(BookmarkService service) { this.service = service; }

    @GetMapping
    public Result<List<Bookmark>> list() { return Result.success(service.list(USER_ID)); }

    @PostMapping
    public Result<Bookmark> create(@Valid @RequestBody BookmarkSaveRequest request) {
        return Result.success(service.create(USER_ID, request));
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) { service.delete(USER_ID, id); return Result.success(); }

    @DeleteMapping
    public Result<Void> clear() { service.clear(USER_ID); return Result.success(); }
}
