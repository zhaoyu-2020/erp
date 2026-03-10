package com.mc.erp.entity;

import com.baomidou.mybatisplus.annotation.*;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("biz_supplier")
public class Supplier {
    @TableId(type = IdType.AUTO)
    private Long id;

    @NotBlank(message = "供应商编码不能为空")
    private String supplierCode;

    @NotBlank(message = "供应商名称不能为空")
    private String name;

    @NotBlank(message = "联系人不能为空")
    private String contactPerson;

    @NotBlank(message = "联系电话不能为空")
    private String phone;
    private String address;
    /**
     * 产品类型(多个用英文逗号分隔)
     */
    private String productType;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    @TableLogic
    private Integer isDeleted;
}
