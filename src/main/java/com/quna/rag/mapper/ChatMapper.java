package com.quna.rag.mapper;

import com.quna.rag.entity.ChatHistory;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Select;
import java.util.List;

public interface ChatMapper {
    @Insert("INSERT INTO chat_history(user_id,question,answer) VALUES(#{userId},#{question},#{answer})")
    int insert(ChatHistory history);

    @Select("SELECT * FROM chat_history WHERE user_id=#{userId} ORDER BY id DESC")
    List<ChatHistory> selectByUserId(Long userId);
}