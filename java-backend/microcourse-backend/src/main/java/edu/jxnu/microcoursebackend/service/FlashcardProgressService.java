package edu.jxnu.microcoursebackend.service;

import java.util.List;

public interface FlashcardProgressService {
    List<Integer> list(Long userId);

    void markMastered(Long userId, Integer cardIndex);

    void clear(Long userId);
}
