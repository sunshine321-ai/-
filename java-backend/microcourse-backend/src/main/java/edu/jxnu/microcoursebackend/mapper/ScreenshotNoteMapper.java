package edu.jxnu.microcoursebackend.mapper;

import edu.jxnu.microcoursebackend.pojo.ScreenshotNote;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

@Mapper
public interface ScreenshotNoteMapper {

    @Insert("""
            insert into screenshot_notes(user_id, image_url, video_time, note, ai_analysis)
            values(#{userId}, #{imageUrl}, #{videoTime}, #{note}, #{aiAnalysis})
            """)
    @Options(useGeneratedKeys = true, keyProperty = "id")
    void insert(ScreenshotNote screenshotNote);

    @Select("""
            select id, user_id, image_url, video_time, note, ai_analysis, created_at
            from screenshot_notes
            where user_id = #{userId}
            order by id desc
            """)
    List<ScreenshotNote> findByUserId(Long userId);

    @Select("select id, user_id, image_url, video_time, note, ai_analysis, created_at from screenshot_notes where id = #{id} and user_id = #{userId}")
    ScreenshotNote findByIdAndUserId(@Param("id") Long id, @Param("userId") Long userId);

    @Update("""
            update screenshot_notes
            set note = #{note}, ai_analysis = #{aiAnalysis}
            where id = #{id} and user_id = #{userId}
            """)
    int updateContent(
            @Param("id") Long id,
            @Param("userId") Long userId,
            @Param("note") String note,
            @Param("aiAnalysis") String aiAnalysis
    );

    @Delete("delete from screenshot_notes where id = #{id} and user_id = #{userId}")
    int deleteByIdAndUserId(@Param("id") Long id, @Param("userId") Long userId);
}
