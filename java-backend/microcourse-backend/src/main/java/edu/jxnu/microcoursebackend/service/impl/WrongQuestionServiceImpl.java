package edu.jxnu.microcoursebackend.service.impl;

import edu.jxnu.microcoursebackend.mapper.WrongQuestionMapper;
import edu.jxnu.microcoursebackend.pojo.WrongQuestion;
import edu.jxnu.microcoursebackend.pojo.WrongQuestionSaveRequest;
import edu.jxnu.microcoursebackend.service.WrongQuestionService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class WrongQuestionServiceImpl implements WrongQuestionService {
    private final WrongQuestionMapper mapper;

    public WrongQuestionServiceImpl(WrongQuestionMapper mapper) {
        this.mapper = mapper;
    }

    public List<WrongQuestion> list(Long userId) {
        return mapper.findByUserId(userId);
    }

    public WrongQuestion create(Long userId, WrongQuestionSaveRequest request) {
        WrongQuestion item = fromRequest(userId, null, request);
        mapper.insert(item);
        return mapper.findByIdAndUserId(item.getId(), userId);
    }

    public WrongQuestion update(Long userId, Long id, WrongQuestionSaveRequest request) {
        if (mapper.findByIdAndUserId(id, userId) == null) {
            throw new IllegalArgumentException("错题不存在");
        }
        WrongQuestion item = fromRequest(userId, id, request);
        mapper.update(item);
        return mapper.findByIdAndUserId(id, userId);
    }

    public void delete(Long userId, Long id) {
        mapper.delete(id, userId);
    }

    public void clear(Long userId) {
        mapper.deleteAll(userId);
    }

    private WrongQuestion fromRequest(Long userId, Long id, WrongQuestionSaveRequest request) {
        WrongQuestion item = new WrongQuestion();
        item.setId(id);
        item.setUserId(userId);
        item.setQuestion(request.question());
        item.setOptionsJson(request.optionsJson());
        item.setCorrectAnswer(request.correctAnswer());
        item.setUserAnswer(request.userAnswer());
        item.setNote(request.note());
        return item;
    }
}
