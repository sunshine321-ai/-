package edu.jxnu.microcoursebackend.mapper;

import edu.jxnu.microcoursebackend.pojo.ChatMessage;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface ChatMessageMapper {
    @Select("select * from chat_messages where user_id=#{userId} and context=#{context} order by id")
    List<ChatMessage> findByContext(@Param("userId") Long userId, @Param("context") String context);

    @Insert("insert into chat_messages(user_id, context, role, content) values(#{userId}, #{context}, #{role}, #{content})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    void insert(ChatMessage message);

    @Delete("delete from chat_messages where user_id=#{userId} and context=#{context}")
    int deleteByContext(@Param("userId") Long userId, @Param("context") String context);
}
