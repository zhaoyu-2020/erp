package com.mc.erp.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("biz_customs_doc")
public class CustomsDoc {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String docNo;
    private Long salesOrderId;
    private LocalDate declareDate;
    private Integer status;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    @TableLogic
    private Integer isDeleted;
}
