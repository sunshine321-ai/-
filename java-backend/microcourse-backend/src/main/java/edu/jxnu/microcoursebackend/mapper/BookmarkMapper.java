package edu.jxnu.microcoursebackend.mapper;

import edu.jxnu.microcoursebackend.pojo.Bookmark;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface BookmarkMapper {
    @Select("select * from bookmarks where user_id=#{userId} order by video_time")
    List<Bookmark> findByUserId(Long userId);

    @Select("select * from bookmarks where id=#{id} and user_id=#{userId}")
    Bookmark findOne(@Param("id") Long id, @Param("userId") Long userId);

    @Insert("insert into bookmarks(user_id, video_time, label) values(#{userId}, #{videoTime}, #{label})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    void insert(Bookmark bookmark);

    @Delete("delete from bookmarks where id=#{id} and user_id=#{userId}")
    int delete(@Param("id") Long id, @Param("userId") Long userId);

    @Delete("delete from bookmarks where user_id=#{userId}")
    int deleteAll(Long userId);
}
