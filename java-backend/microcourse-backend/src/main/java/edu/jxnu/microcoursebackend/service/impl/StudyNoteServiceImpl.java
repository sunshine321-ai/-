package edu.jxnu.microcoursebackend.service.impl;

import edu.jxnu.microcoursebackend.mapper.StudyNoteMapper;
import edu.jxnu.microcoursebackend.pojo.StudyNote;
import edu.jxnu.microcoursebackend.service.StudyNoteService;
import org.springframework.stereotype.Service;

@Service
public class StudyNoteServiceImpl implements StudyNoteService {
    private final StudyNoteMapper studyNoteMapper;

    public StudyNoteServiceImpl(StudyNoteMapper studyNoteMapper) {
        this.studyNoteMapper = studyNoteMapper;
    }

    @Override
    public StudyNote getByUserId(Long userId) {
        return studyNoteMapper.findByUserId(userId);
    }

    @Override
    public StudyNote save(Long userId, String content) {
        studyNoteMapper.save(userId, content);
        StudyNote saved = studyNoteMapper.findByUserId(userId);
        if (saved == null) {
            throw new IllegalStateException("学习笔记保存失败");
        }
        return saved;
    }
}
