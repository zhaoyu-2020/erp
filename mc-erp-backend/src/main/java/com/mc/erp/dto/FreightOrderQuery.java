package com.mc.erp.dto;

import lombok.Data;

@Data
public class FreightOrderQuery {
    private Integer pageNum = 1;
    private Integer pageSize = 10;
    private String orderCode;
    private String saleOrderCode;
    private Long forwarderId;
    private String forwarderName;
    private Integer transportType;
    private String containerType;
    private Integer isLcl;
    private Integer needInsurance;
    private Integer orderStatus;
    private String createUser;
    private String createTimeStart;
    private String createTimeEnd;
    private String keyword;
}
