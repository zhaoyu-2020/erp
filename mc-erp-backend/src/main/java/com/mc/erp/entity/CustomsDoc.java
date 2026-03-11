package com.mc.erp.entity;

import com.baomidou.mybatisplus.annotation.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("biz_customs_doc")
public class CustomsDoc {
    @TableId(type = IdType.AUTO)
    private Long id;

    @NotBlank(message = "报关单号不能为空")
    private String docNo;

    @NotNull(message = "关联销售订单ID不能为空")
    private Long salesOrderId;

    @NotNull(message = "报关日期不能为空")
    private LocalDate declareDate;
    private Integer status;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    @TableField(fill = FieldFill.INSERT)
    private Long createId;
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private Long updateId;
    @TableLogic
    private Integer isDeleted;
}
