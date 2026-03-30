package com.mc.erp.dto;

import com.alibaba.excel.annotation.ExcelProperty;
import lombok.Data;

/**
 * 采购订单 Excel 导入行（按钮3：上传合同基本信息）
 */
@Data
public class PurchaseOrderImportRow {

    @ExcelProperty("采购单号")
    private String poNo;

    @ExcelProperty("供应商名称")
    private String supplierName;

    @ExcelProperty("关联销售订单号")
    private String salesOrderNo;

    @ExcelProperty("业务员姓名")
    private String salespersonName;

    @ExcelProperty("币种")
    private String currency;

    @ExcelProperty("订单金额(RMB)")
    private String totalAmount;

    @ExcelProperty("结算总金额(RMB)")
    private String settlementTotalAmount;

    @ExcelProperty("定金比例(%)")
    private String depositRate;

    @ExcelProperty("定金金额(RMB)")
    private String depositAmount;

    /** 格式 YYYY-MM-DD */
    @ExcelProperty("订单日期")
    private String orderDate;

    /** 格式 YYYY-MM-DD */
    @ExcelProperty("交货日期")
    private String deliveryDate;

    @ExcelProperty("运输备注")
    private String transportRemark;

    @ExcelProperty("总运费")
    private String totalFreight;
}
