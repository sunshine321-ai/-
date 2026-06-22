package edu.jxnu.microcoursebackend.service;

import edu.jxnu.microcoursebackend.pojo.LearningProgress;
import edu.jxnu.microcoursebackend.pojo.LearningProgressSaveRequest;

import java.util.List;

public interface LearningProgressService {
    List<LearningProgress> list(Long userId);
    LearningProgress save(Long userId, LearningProgressSaveRequest request);
    void reset(Long userId);
}
