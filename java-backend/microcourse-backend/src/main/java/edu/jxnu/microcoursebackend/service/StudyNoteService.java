package edu.jxnu.microcoursebackend.service;

import edu.jxnu.microcoursebackend.pojo.StudyNote;

public interface StudyNoteService {
    StudyNote getByUserId(Long userId);

    StudyNote save(Long userId, String content);
}
