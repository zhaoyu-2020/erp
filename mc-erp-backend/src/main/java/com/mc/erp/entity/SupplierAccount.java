package com.mc.erp.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("biz_supplier_account")
public class SupplierAccount {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long supplierId;
    private String bankName;
    private String accountName;
    private String accountNo;
    private String currency;
    private String swiftCode;
    private String remark;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    @TableLogic
    private Integer isDeleted;
}
