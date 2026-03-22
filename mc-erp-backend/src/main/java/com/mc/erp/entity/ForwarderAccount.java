package com.mc.erp.entity;

import com.baomidou.mybatisplus.annotation.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("biz_forwarder_account")
public class ForwarderAccount {
    @TableId(type = IdType.AUTO)
    private Long id;

    @NotNull(message = "货代ID不能为空")
    private Long forwarderId;

    @NotBlank(message = "开户银行不能为空")
    private String bankName;

    @NotBlank(message = "开户名称不能为空")
    private String accountName;

    @NotBlank(message = "账号不能为空")
    private String accountNo;
    private String currency;
    private String swiftCode;
    private String remark;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    @TableField(fill = FieldFill.INSERT)
    private Long createId;
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private Long updateId;
    @TableLogic
    private Integer isDeleted;
}
