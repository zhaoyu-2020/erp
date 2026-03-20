package com.mc.erp.vo;

import lombok.Data;
import lombok.AllArgsConstructor;

/**
 * 未完订单状态行视图对象（用于状态进度表）
 */
@Data
@AllArgsConstructor
public class IncompleteOrderStatusVO {
    private String orderNo;     // 订单号
    private Integer statusCode; // 当前状态码
    private String statusLabel; // 当前状态名称
}
