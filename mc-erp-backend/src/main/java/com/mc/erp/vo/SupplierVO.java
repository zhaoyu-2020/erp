package com.mc.erp.vo;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class SupplierVO {
    private Long id;
    private String supplierCode;
    private String name;
    private String contactPerson;
    private String phone;
    private String address;
    private LocalDateTime createTime;
}
