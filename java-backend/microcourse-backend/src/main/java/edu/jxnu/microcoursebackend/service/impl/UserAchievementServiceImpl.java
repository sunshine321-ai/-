package edu.jxnu.microcoursebackend.service.impl;

import edu.jxnu.microcoursebackend.mapper.UserAchievementMapper;
import edu.jxnu.microcoursebackend.pojo.UserAchievement;
import edu.jxnu.microcoursebackend.service.UserAchievementService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;

@Service
public class UserAchievementServiceImpl implements UserAchievementService {
    private static final String MASTER = "master";
    private static final Set<String> REGULAR_KEYS = Set.of(
            "first_viz", "first_ai", "first_design", "all_exercises", "high_score",
            "long_study", "flashcard_done", "retry_wrong", "note_taker", "night_owl", "explorer"
    );
    private final UserAchievementMapper mapper;

    public UserAchievementServiceImpl(UserAchievementMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    public List<UserAchievement> list(Long userId) {
        return mapper.findByUserId(userId);
    }

    @Transactional
    @Override
    public List<UserAchievement> unlock(Long userId, String achievementKey) {
        if (!REGULAR_KEYS.contains(achievementKey)) {
            throw new IllegalArgumentException("成就标识不正确");
        }
        mapper.unlock(userId, achievementKey);
        if (mapper.countRegularAchievements(userId) >= REGULAR_KEYS.size()) {
            mapper.unlock(userId, MASTER);
        }
        return mapper.findByUserId(userId);
    }
}
