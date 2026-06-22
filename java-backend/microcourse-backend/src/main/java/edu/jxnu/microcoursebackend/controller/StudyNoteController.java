package edu.jxnu.microcoursebackend.controller;

import edu.jxnu.microcoursebackend.pojo.Result;
import edu.jxnu.microcoursebackend.pojo.StudyNote;
import edu.jxnu.microcoursebackend.pojo.StudyNoteSaveRequest;
import edu.jxnu.microcoursebackend.service.StudyNoteService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/notes")
public class StudyNoteController {
    private static final Long DEFAULT_USER_ID = 1L;
    private final StudyNoteService studyNoteService;

    public StudyNoteController(StudyNoteService studyNoteService) {
        this.studyNoteService = studyNoteService;
    }

    @GetMapping
    public Result<StudyNote> get() {
        return Result.success(studyNoteService.getByUserId(DEFAULT_USER_ID));
    }

    @PutMapping
    public Result<StudyNote> save(@Valid @RequestBody StudyNoteSaveRequest request) {
        return Result.success(studyNoteService.save(DEFAULT_USER_ID, request.content()));
    }
}
