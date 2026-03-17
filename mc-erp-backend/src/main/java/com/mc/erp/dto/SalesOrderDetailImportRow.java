package com.mc.erp.dto;

import com.alibaba.excel.annotation.ExcelProperty;
import lombok.Data;

/**
 * 销售订单明细 Excel 导入行（按钮2：上传合同实际明细）
 */
@Data
public class SalesOrderDetailImportRow {

    @ExcelProperty("订单号")
    private String orderNo;

    @ExcelProperty("产品规格")
    private String spec;

    @ExcelProperty("产品类型")
    private String productType;

    @ExcelProperty("材质")
    private String material;

    @ExcelProperty("长度(m)")
    private String length;

    @ExcelProperty("公差")
    private String tolerance;

    @ExcelProperty("结算价格")
    private String settlementPrice;

    @ExcelProperty("结算方式：理论重量，过磅重量")
    private String measurementMethod;

    @ExcelProperty("订货数量")
    private String orderedQuantity;

    @ExcelProperty("实际数量")
    private String actualQuantity;

    @ExcelProperty("件数")
    private String bundleCount;

    @ExcelProperty("净重")
    private String netWeight;

    @ExcelProperty("毛重")
    private String grossWeight;

    @ExcelProperty("体积")
    private String volume;

    @ExcelProperty("包装重量")
    private String packagingWeight;

    @ExcelProperty("包装方式")
    private String packaging;

    @ExcelProperty("卷内径(mm)")
    private String coilInnerDiameter;

    @ExcelProperty("加工项")
    private String processingItems;

    @ExcelProperty("备注")
    private String remark;

    @ExcelProperty("货源地")
    private String originPlace;

}
