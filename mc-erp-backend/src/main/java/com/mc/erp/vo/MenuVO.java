package com.mc.erp.vo;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class MenuVO {
    private Long id;
    private Long parentId;
    private String menuName;
    private String path;
    private String component;
    private String icon;
    private Integer type;
    private String permission;
    private Integer sort;
    private LocalDateTime createTime;
}
