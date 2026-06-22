package edu.jxnu.microcoursebackend.mapper;

import edu.jxnu.microcoursebackend.pojo.WrongQuestion;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface WrongQuestionMapper {
    @Select("select * from wrong_questions where user_id = #{userId} order by id desc")
    List<WrongQuestion> findByUserId(Long userId);

    @Select("select * from wrong_questions where id = #{id} and user_id = #{userId}")
    WrongQuestion findByIdAndUserId(@Param("id") Long id, @Param("userId") Long userId);

    @Insert("""
            insert into wrong_questions(user_id, question, options_json, correct_answer, user_answer, note)
            values(#{userId}, #{question}, #{optionsJson}, #{correctAnswer}, #{userAnswer}, #{note})
            """)
    @Options(useGeneratedKeys = true, keyProperty = "id")
    void insert(WrongQuestion question);

    @Update("""
            update wrong_questions
            set question=#{question}, options_json=#{optionsJson}, correct_answer=#{correctAnswer},
                user_answer=#{userAnswer}, note=#{note}
            where id=#{id} and user_id=#{userId}
            """)
    int update(WrongQuestion question);

    @Delete("delete from wrong_questions where id=#{id} and user_id=#{userId}")
    int delete(@Param("id") Long id, @Param("userId") Long userId);

    @Delete("delete from wrong_questions where user_id=#{userId}")
    int deleteAll(Long userId);
}
