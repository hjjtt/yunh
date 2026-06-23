package com.yunh.interaction.controller;

import com.yunh.common.exception.BusinessException;
import com.yunh.common.result.Result;
import com.yunh.interaction.controller.support.UserHeaderSupport;
import com.yunh.interaction.pojo.Note;
import com.yunh.interaction.service.NoteService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 笔记控制器
 */
@Api(tags = "笔记管理")
@RestController
@RequestMapping("/note")
public class NoteController {
    
    @Autowired
    private NoteService noteService;
    
    @ApiOperation("查询用户笔记列表")
    @GetMapping("/user/{userId}")
    public Result<List<Note>> getByUserId(
            @PathVariable Long userId,
            @RequestHeader(value = "X-User-Id", required = false) String userIdHeader,
            @RequestHeader(value = "X-Role", required = false) String role) {
        if (!"ADMIN".equals(role) && !userId.equals(UserHeaderSupport.parseUserId(userIdHeader))) {
            return Result.error("无权查看该用户笔记");
        }
        return Result.success(noteService.getByUserId(userId));
    }
    
    @ApiOperation("查询公开笔记列表")
    @GetMapping("/public")
    public Result<List<Note>> getPublicNotes() {
        return Result.success(noteService.getPublicNotes());
    }
    
    @ApiOperation("创建笔记")
    @PostMapping("/create")
    public Result<Note> create(@RequestHeader(value = "X-User-Id", required = false) String userIdHeader,
                               @RequestBody Note note) {
        if (note == null) {
            throw new BusinessException("笔记内容不能为空");
        }
        if (note.getCourseId() == null) {
            throw new BusinessException("课程ID不能为空");
        }
        if (note.getContent() == null || note.getContent().trim().isEmpty()) {
            throw new BusinessException("笔记内容不能为空");
        }

        note.setUserId(UserHeaderSupport.requireUserId(userIdHeader));
        note.setContent(note.getContent().trim());
        if (note.getTitle() != null) {
            note.setTitle(note.getTitle().trim());
        }
        return Result.success(noteService.create(note));
    }
    
    @ApiOperation("删除笔记")
    @DeleteMapping("/{id}")
    public Result<Boolean> delete(
            @PathVariable Long id,
            @RequestHeader(value = "X-User-Id", required = false) String userIdHeader,
            @RequestHeader(value = "X-Role", required = false) String role) {
        Note note = noteService.getById(id);
        if (note == null) {
            return Result.error("笔记不存在");
        }
        if (!"ADMIN".equals(role) && !note.getUserId().equals(UserHeaderSupport.requireUserId(userIdHeader))) {
            return Result.error("无权删除该笔记");
        }
        return Result.success(noteService.removeById(id));
    }
}
