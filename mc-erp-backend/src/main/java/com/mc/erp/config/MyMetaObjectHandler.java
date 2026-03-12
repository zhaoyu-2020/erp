package com.mc.erp.config;

import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import com.mc.erp.util.SecurityUtil;
import org.apache.ibatis.reflection.MetaObject;
import org.springframework.stereotype.Component;

/**
 * MyBatis Plus 自动填充处理器：自动设置 create_id 和 update_id
 */
@Component
public class MyMetaObjectHandler implements MetaObjectHandler {

    @Override
    public void insertFill(MetaObject metaObject) {
        Long userId = SecurityUtil.getCurrentUserId();
        this.strictInsertFill(metaObject, "createId", Long.class, userId);
        this.strictInsertFill(metaObject, "updateId", Long.class, userId);
    }

    @Override
    public void updateFill(MetaObject metaObject) {
        Long userId = SecurityUtil.getCurrentUserId();
        this.strictUpdateFill(metaObject, "updateId", Long.class, userId);
    }
}
