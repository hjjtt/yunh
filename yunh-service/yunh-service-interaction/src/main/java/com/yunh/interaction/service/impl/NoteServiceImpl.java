package com.yunh.interaction.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yunh.interaction.mapper.NoteMapper;
import com.yunh.interaction.pojo.Note;
import com.yunh.interaction.service.NoteService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
public class NoteServiceImpl extends ServiceImpl<NoteMapper, Note> implements NoteService {
    
    @Override
    public List<Note> getByUserId(Long userId) {
        return list(new QueryWrapper<Note>()
                .eq("user_id", userId)
                .orderByDesc("create_time"));
    }
    
    @Override
    public List<Note> getPublicNotes() {
        return list(new QueryWrapper<Note>()
                .eq("is_public", 1)
                .orderByDesc("create_time"));
    }
    
    @Override
    public Note create(Note note) {
        note.setLikeCount(0);
        note.setStatus(1);
        note.setCreateTime(LocalDateTime.now());
        note.setUpdateTime(LocalDateTime.now());
        save(note);
        log.info("笔记创建成功，用户ID：{}，课程ID：{}", note.getUserId(), note.getCourseId());
        return note;
    }
}
