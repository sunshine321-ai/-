package edu.jxnu.microcoursebackend.mapper;

import edu.jxnu.microcoursebackend.pojo.StudyNote;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface StudyNoteMapper {

    @Select("select id, user_id, content, created_at, updated_at from study_notes where user_id = #{userId}")
    StudyNote findByUserId(Long userId);

    @Insert("""
            insert into study_notes(user_id, content)
            values(#{userId}, #{content})
            on duplicate key update content = values(content), updated_at = current_timestamp
            """)
    void save(@Param("userId") Long userId, @Param("content") String content);
}
