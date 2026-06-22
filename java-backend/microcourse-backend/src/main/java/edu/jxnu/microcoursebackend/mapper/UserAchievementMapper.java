package edu.jxnu.microcoursebackend.mapper;

import edu.jxnu.microcoursebackend.pojo.UserAchievement;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface UserAchievementMapper {
    @Select("select * from user_achievements where user_id=#{userId} order by unlocked_at, id")
    List<UserAchievement> findByUserId(Long userId);

    @Insert("insert ignore into user_achievements(user_id, achievement_key) values(#{userId}, #{achievementKey})")
    int unlock(@Param("userId") Long userId, @Param("achievementKey") String achievementKey);

    @Select("select count(*) from user_achievements where user_id=#{userId} and achievement_key <> 'master'")
    int countRegularAchievements(Long userId);
}
