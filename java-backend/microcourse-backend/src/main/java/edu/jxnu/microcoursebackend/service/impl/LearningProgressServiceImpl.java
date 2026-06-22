package edu.jxnu.microcoursebackend.service.impl;

import edu.jxnu.microcoursebackend.mapper.LearningProgressMapper;
import edu.jxnu.microcoursebackend.pojo.LearningProgress;
import edu.jxnu.microcoursebackend.pojo.LearningProgressSaveRequest;
import edu.jxnu.microcoursebackend.service.LearningProgressService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class LearningProgressServiceImpl implements LearningProgressService {
    private final LearningProgressMapper mapper;

    public LearningProgressServiceImpl(LearningProgressMapper mapper) {
        this.mapper = mapper;
    }

    public List<LearningProgress> list(Long userId) {
        return mapper.findByUserId(userId);
    }

    public LearningProgress save(Long userId, LearningProgressSaveRequest request) {
        LearningProgress item = new LearningProgress();
        item.setUserId(userId);
        item.setChapterKey(request.chapterKey());
        item.setProgress(request.progress());
        item.setCompleted(request.completed() != null ? request.completed() : request.progress().intValue() >= 100);
        item.setDetailJson(request.detailJson());
        item.setDurationSeconds(request.durationSeconds() == null ? 0L : request.durationSeconds());
        mapper.upsert(item);
        return mapper.findOne(userId, request.chapterKey());
    }

    public void reset(Long userId) {
        mapper.deleteAll(userId);
    }
}
