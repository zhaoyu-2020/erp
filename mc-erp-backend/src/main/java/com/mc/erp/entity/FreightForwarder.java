package com.mc.erp.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("biz_freight_forwarder")
public class FreightForwarder {
    @TableId(type = IdType.AUTO)
    private Long id;

    @NotBlank(message = "货代编码不能为空")
    private String forwarderCode;

    @NotBlank(message = "货代名称不能为空")
    private String name;

    /**
     * 货代类型: 集装箱/散货
     */
    private String freightType;

    /**
     * 优势市场(多个用英文逗号分隔)
     */
    private String marketAdvantage;

    private String contactPerson;
    private String phone;
    private String address;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;

    @TableField(fill = FieldFill.INSERT)
    private Long createId;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private Long updateId;

    @TableLogic
    private Integer isDeleted;
}
