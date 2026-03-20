package com.mc.erp.vo;

import lombok.Data;
import lombok.AllArgsConstructor;

/**
 * 订单状态报表视图对象
 */
@Data
@AllArgsConstructor
public class OrderStatusReportVO {
    private Integer statusCode;     // 状态码
    private String statusLabel;     // 状态名称
    private Integer orderCount;     // 订单数量
    private Boolean isCompleted;    // 是否已完成状态
}
