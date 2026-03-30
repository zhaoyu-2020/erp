package com.mc.erp.dto;

import com.alibaba.excel.annotation.ExcelProperty;
import lombok.Data;

/**
 * 销售订单 Excel 導入行（按钮1：上传合同基本信息）
 */
@Data
public class SalesOrderImportRow {

    @ExcelProperty("订单号*")
    private String orderNo;

    @ExcelProperty("业务员姓名*")
    private String salespersonName;

    @ExcelProperty("操作员姓名*")
    private String operatorName;

    @ExcelProperty("客户名称*")
    private String customerName;

    @ExcelProperty("贸易条款*")
    private String tradeTerm;

    @ExcelProperty("付款方式*")
    private String paymentMethod;

    @ExcelProperty("币种*")
    private String currency;

    @ExcelProperty("定金比例(%)*")
    private String depositRate;

    /** 格式 YYYY-MM-DD */
    @ExcelProperty("预计收尾款日期*")
    private String expectedReceiptDays;

    /** 格式 YYYY-MM-DD */
    @ExcelProperty("交货期*")
    private String deliveryDate;


    @ExcelProperty("目的港*")
    private String destinationPort;

    @ExcelProperty("运输方式*")
    private String transportType;


    @ExcelProperty("定金汇率")
    private String depositExchangeRate;

    @ExcelProperty("尾款汇率")
    private String finalExchangeRate;

    @ExcelProperty("合同金额")
    private String contractAmount;


    @ExcelProperty("收款金额")
    private String receivedAmount;

    @ExcelProperty("海运费")
    private String seaFreight;

    @ExcelProperty("港杂费")
    private String portFee;
}
