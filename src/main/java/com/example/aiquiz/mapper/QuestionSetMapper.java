package com.example.aiquiz.mapper;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Select;
import com.example.aiquiz.model.QuestionSet;
import java.util.List;

@Mapper
public interface QuestionSetMapper {
    @Insert("INSERT INTO question_set (title, description, question_count, create_time, update_time) " +
            "VALUES (#{title}, #{description}, #{questionCount}, NOW(), NOW())")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(QuestionSet questionSet);
    
    @Select("SELECT * FROM question_set WHERE id = #{id}")
    QuestionSet findById(Long id);
    
    @Select("SELECT * FROM question_set ORDER BY create_time DESC")
    List<QuestionSet> findAll();
} 