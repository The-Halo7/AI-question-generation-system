package com.example.aiquiz.mapper;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import com.example.aiquiz.model.Question;
import java.util.List;
import org.apache.ibatis.annotations.Options;

@Mapper
public interface QuestionMapper {
    @Insert("INSERT INTO question (set_id, question_number, type, content, answer, analysis, create_time) " +
            "VALUES (#{setId}, #{questionNumber}, #{type}, #{content}, #{answer}, #{analysis}, NOW())")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(Question question);
    
    @Select("SELECT * FROM question WHERE set_id = #{setId} ORDER BY question_number")
    List<Question> findBySetId(Long setId);
} 