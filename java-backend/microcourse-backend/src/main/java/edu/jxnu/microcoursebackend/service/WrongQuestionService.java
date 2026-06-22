package edu.jxnu.microcoursebackend.service;

import edu.jxnu.microcoursebackend.pojo.WrongQuestion;
import edu.jxnu.microcoursebackend.pojo.WrongQuestionSaveRequest;

import java.util.List;

public interface WrongQuestionService {
    List<WrongQuestion> list(Long userId);
    WrongQuestion create(Long userId, WrongQuestionSaveRequest request);
    WrongQuestion update(Long userId, Long id, WrongQuestionSaveRequest request);
    void delete(Long userId, Long id);
    void clear(Long userId);
}
