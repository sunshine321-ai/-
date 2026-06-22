package edu.jxnu.microcoursebackend.mapper;

import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface FlashcardProgressMapper {
    @Select("select card_index from flashcard_progress where user_id=#{userId} order by card_index")
    List<Integer> findMasteredIndexes(Long userId);

    @Insert("insert ignore into flashcard_progress(user_id, card_index) values(#{userId}, #{cardIndex})")
    void markMastered(@Param("userId") Long userId, @Param("cardIndex") Integer cardIndex);

    @Delete("delete from flashcard_progress where user_id=#{userId}")
    void clear(Long userId);
}
