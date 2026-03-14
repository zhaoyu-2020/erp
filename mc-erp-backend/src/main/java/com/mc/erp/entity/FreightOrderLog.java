package com.mc.erp.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("erp_freight_order_log")
public class FreightOrderLog {
    @TableId(value = "log_id", type = IdType.AUTO)
    private Long logId;

    private Long orderId;
    private String orderCode;
    private String operator;
    private Long operatorId;
    private LocalDateTime operateTime;
    private String operateType;
    private String beforeContent;
    private String afterContent;
    private String operateRemark;
}
