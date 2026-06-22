package edu.jxnu.microcoursebackend.service.impl;

import edu.jxnu.microcoursebackend.mapper.FlashcardProgressMapper;
import edu.jxnu.microcoursebackend.service.FlashcardProgressService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class FlashcardProgressServiceImpl implements FlashcardProgressService {
    private static final int CARD_COUNT = 10;
    private final FlashcardProgressMapper mapper;

    public FlashcardProgressServiceImpl(FlashcardProgressMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    public List<Integer> list(Long userId) {
        return mapper.findMasteredIndexes(userId);
    }

    @Override
    public void markMastered(Long userId, Integer cardIndex) {
        if (cardIndex == null || cardIndex < 0 || cardIndex >= CARD_COUNT) {
            throw new IllegalArgumentException("闪卡序号不正确");
        }
        mapper.markMastered(userId, cardIndex);
    }

    @Override
    public void clear(Long userId) {
        mapper.clear(userId);
    }
}
