package com.yunh.interaction.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.yunh.interaction.pojo.Note;

import java.util.List;

/**
 * 笔记服务接口
 */
public interface NoteService extends IService<Note> {
    
    List<Note> getByUserId(Long userId);
    
    List<Note> getPublicNotes();
    
    Note create(Note note);
}
