package com.yunh.user.vo;

import com.yunh.user.pojo.User;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 用户安全视图对象
 * 只保留前端展示和跨服务协作需要的非敏感字段。
 */
@Data
public class UserSafeVO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;

    private String username;

    private String nickname;

    private String avatar;

    private Integer status;

    private String role;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;

    public static UserSafeVO from(User user) {
        if (user == null) {
            return null;
        }

        UserSafeVO safeVO = new UserSafeVO();
        safeVO.setId(user.getId());
        safeVO.setUsername(user.getUsername());
        safeVO.setNickname(user.getNickname());
        safeVO.setAvatar(user.getAvatar());
        safeVO.setStatus(user.getStatus());
        safeVO.setRole(user.getRole());
        safeVO.setCreateTime(user.getCreateTime());
        safeVO.setUpdateTime(user.getUpdateTime());
        return safeVO;
    }
}
