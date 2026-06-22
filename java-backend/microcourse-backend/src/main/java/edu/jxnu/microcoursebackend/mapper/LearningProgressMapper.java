package edu.jxnu.microcoursebackend.mapper;

import edu.jxnu.microcoursebackend.pojo.LearningProgress;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface LearningProgressMapper {
    @Select("select * from learning_progress where user_id=#{userId} order by chapter_key")
    List<LearningProgress> findByUserId(Long userId);

    @Select("select * from learning_progress where user_id=#{userId} and chapter_key=#{chapterKey}")
    LearningProgress findOne(@Param("userId") Long userId, @Param("chapterKey") String chapterKey);

    @Insert("""
            insert into learning_progress(user_id, chapter_key, progress, completed, detail_json, duration_seconds)
            values(#{userId}, #{chapterKey}, #{progress}, #{completed}, #{detailJson}, #{durationSeconds})
            on duplicate key update progress=values(progress), completed=values(completed),
                detail_json=values(detail_json), duration_seconds=values(duration_seconds)
            """)
    void upsert(LearningProgress progress);

    @Delete("delete from learning_progress where user_id=#{userId}")
    int deleteAll(Long userId);
}
