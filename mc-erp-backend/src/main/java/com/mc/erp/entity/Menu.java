package com.mc.erp.entity;

import com.baomidou.mybatisplus.annotation.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("sys_menu")
public class Menu {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long parentId;

    @NotBlank(message = "菜单名称不能为空")
    private String menuName;
    private String path;
    private String component;
    private String icon;

    @NotNull(message = "菜单类型不能为空")
    private Integer type;
    private String permission;

    @NotNull(message = "排序不能为空")
    private Integer sort;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    @TableLogic
    private Integer isDeleted;
}
