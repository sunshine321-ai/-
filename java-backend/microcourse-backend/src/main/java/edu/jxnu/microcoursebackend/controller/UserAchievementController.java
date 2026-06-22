package edu.jxnu.microcoursebackend.controller;

import edu.jxnu.microcoursebackend.pojo.Result;
import edu.jxnu.microcoursebackend.pojo.UserAchievement;
import edu.jxnu.microcoursebackend.service.UserAchievementService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/achievements")
public class UserAchievementController {
    private static final Long USER_ID = 1L;
    private final UserAchievementService service;

    public UserAchievementController(UserAchievementService service) {
        this.service = service;
    }

    @GetMapping
    public Result<List<UserAchievement>> list() {
        return Result.success(service.list(USER_ID));
    }

    @PutMapping("/{achievementKey}")
    public Result<List<UserAchievement>> unlock(@PathVariable String achievementKey) {
        return Result.success(service.unlock(USER_ID, achievementKey));
    }
}
