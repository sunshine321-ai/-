package edu.jxnu.microcoursebackend.service;

import edu.jxnu.microcoursebackend.pojo.UserAchievement;

import java.util.List;

public interface UserAchievementService {
    List<UserAchievement> list(Long userId);

    List<UserAchievement> unlock(Long userId, String achievementKey);
}
