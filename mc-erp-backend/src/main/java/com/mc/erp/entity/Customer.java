package com.mc.erp.entity;

import com.baomidou.mybatisplus.annotation.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("biz_customer")
public class Customer {
    @TableId(type = IdType.AUTO)
    private Long id;

    @NotBlank(message = "客户名称不能为空")
    private String name;
    private String country;
    private String continent;

    @NotBlank(message = "收货人不能为空")
    private String consignee;

    @NotBlank(message = "notify不能为空")
    private String notify;

    @Email(message = "邮箱格式不正确")
    private String email;

    private String phone;
    /** 关联业务员用户ID */
    @TableField("sales_user_id")
    private Long salesUserId;
    private String level;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    @TableField(fill = FieldFill.INSERT)
    private Long createId;
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private Long updateId;
    @TableLogic
    private Integer isDeleted;
}
