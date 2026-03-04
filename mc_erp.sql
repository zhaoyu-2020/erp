-- 1. 产品主数据表
CREATE TABLE IF NOT EXISTS `biz_product` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `spu_code` varchar(50) NOT NULL COMMENT '产品编码(内部)',
  `hs_code` varchar(20) DEFAULT NULL COMMENT '海关编码',
  `name_cn` varchar(200) NOT NULL COMMENT '中文品名',
  `name_en` varchar(200) DEFAULT NULL COMMENT '英文品名',
  `tax_refund_rate` decimal(5,4) DEFAULT '0.0000' COMMENT '退税率',
  `unit` varchar(20) DEFAULT 'PCS' COMMENT '计量单位',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `is_deleted` tinyint(1) DEFAULT '0' COMMENT '逻辑删除:0否 1是',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_spu_code` (`spu_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='外贸产品主数据表';

-- 2. 销售订单主表
CREATE TABLE IF NOT EXISTS `biz_sales_order` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `order_no` varchar(50) NOT NULL COMMENT '外销订单号(如 SO20231018001)',
  `customer_id` bigint(20) NOT NULL COMMENT '客户ID',
  `salesperson_id` bigint(20) NOT NULL COMMENT '业务员ID(归属人)',
  `trade_term` varchar(20) NOT NULL COMMENT '贸易条款(FOB, CIF, EXW 等)',
  `currency` varchar(10) NOT NULL DEFAULT 'USD' COMMENT '结算币种',
  `exchange_rate` decimal(10,4) NOT NULL COMMENT '立单时汇率(快照)',
  `total_amount` decimal(15,2) NOT NULL DEFAULT '0.00' COMMENT '订单总金额',
  `status` tinyint(4) NOT NULL DEFAULT '1' COMMENT '状态:1待审核 2已审核待采购 3生产中 4已发货 5已完结',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `is_deleted` tinyint(1) DEFAULT '0',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_order_no` (`order_no`),
  KEY `idx_customer` (`customer_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='销售订单表';

-- 3. 销售订单明细表
CREATE TABLE IF NOT EXISTS `biz_sales_order_item` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `order_id` bigint(20) NOT NULL COMMENT '销售订单主表ID',
  `product_id` bigint(20) NOT NULL COMMENT '产品ID',
  `quantity` int(11) NOT NULL COMMENT '下单数量',
  `unit_price` decimal(10,2) NOT NULL COMMENT '外币单价',
  `total_price` decimal(15,2) NOT NULL COMMENT '单品总价(外币)',
  `remark_en` varchar(500) DEFAULT NULL COMMENT '英文特殊要求/唛头等',
  PRIMARY KEY (`id`),
  KEY `idx_order_id` (`order_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='销售订单明细表';

-- 4. 客户档案表
CREATE TABLE IF NOT EXISTS `biz_customer` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `customer_code` varchar(50) NOT NULL COMMENT '客户编号',
  `name` varchar(200) NOT NULL COMMENT '客户名称',
  `country` varchar(100) DEFAULT NULL COMMENT '国家/地区',
  `contact_person` varchar(100) DEFAULT NULL COMMENT '联系人',
  `email` varchar(100) DEFAULT NULL COMMENT '邮箱',
  `phone` varchar(50) DEFAULT NULL COMMENT '电话',
  `level` varchar(20) DEFAULT 'NORMAL' COMMENT '级别(VIP/NORMAL)',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `is_deleted` tinyint(1) DEFAULT '0',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_customer_code` (`customer_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='客户档案表';

-- 5. 供应商档案表
CREATE TABLE IF NOT EXISTS `biz_supplier` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `supplier_code` varchar(50) NOT NULL COMMENT '供应商编号',
  `name` varchar(200) NOT NULL COMMENT '供应商名称',
  `contact_person` varchar(100) DEFAULT NULL COMMENT '联系人',
  `phone` varchar(50) DEFAULT NULL COMMENT '电话',
  `address` varchar(500) DEFAULT NULL COMMENT '地址',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `is_deleted` tinyint(1) DEFAULT '0',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_supplier_code` (`supplier_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='供应商档案表';

-- 6. 采购订单表
CREATE TABLE IF NOT EXISTS `biz_purchase_order` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `po_no` varchar(50) NOT NULL COMMENT '采购单号',
  `sales_order_id` bigint(20) DEFAULT NULL COMMENT '关联销售单ID',
  `supplier_id` bigint(20) NOT NULL COMMENT '供应商ID',
  `total_amount` decimal(15,2) NOT NULL DEFAULT '0.00' COMMENT '采购总金额(RMB)',
  `status` tinyint(4) NOT NULL DEFAULT '1' COMMENT '状态:1待审核 2生产中 3已入库',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `is_deleted` tinyint(1) DEFAULT '0',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_po_no` (`po_no`),
  KEY `idx_supplier` (`supplier_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='采购订单表';

-- 7. 用户表
CREATE TABLE IF NOT EXISTS `sys_user` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `username` varchar(50) NOT NULL COMMENT '用户名',
  `password` varchar(100) NOT NULL COMMENT '密码',
  `real_name` varchar(50) DEFAULT NULL COMMENT '真实姓名',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `is_deleted` tinyint(1) DEFAULT '0',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_username` (`username`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='系统用户表';

-- 插入一条默认测试用户
INSERT INTO `sys_user` (`username`, `password`, `real_name`) VALUES ('admin', '123456', 'Administrator');

-- 8. 库存表 (WMS)
CREATE TABLE IF NOT EXISTS `biz_stock` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `product_id` bigint(20) NOT NULL COMMENT '产品ID',
  `current_qty` int(11) NOT NULL DEFAULT '0' COMMENT '当前实际库存',
  `available_qty` int(11) NOT NULL DEFAULT '0' COMMENT '可用库存',
  `locked_qty` int(11) NOT NULL DEFAULT '0' COMMENT '锁定库存',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `is_deleted` tinyint(1) DEFAULT '0',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_product_id` (`product_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='库存表';

-- 9. 报关单证表 (Documents)
CREATE TABLE IF NOT EXISTS `biz_customs_doc` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `doc_no` varchar(50) NOT NULL COMMENT '报关单号',
  `sales_order_id` bigint(20) NOT NULL COMMENT '关联销售单ID',
  `declare_date` date DEFAULT NULL COMMENT '申报日期',
  `status` tinyint(4) NOT NULL DEFAULT '1' COMMENT '状态:1未申报 2申报中 3已放行',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `is_deleted` tinyint(1) DEFAULT '0',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_doc_no` (`doc_no`),
  KEY `idx_sales_order` (`sales_order_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='报关单证表';

-- 10. 财务收款登记表 (Finance)
CREATE TABLE IF NOT EXISTS `biz_finance_receipt` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `receipt_no` varchar(50) NOT NULL COMMENT '水单号',
  `customer_id` bigint(20) NOT NULL COMMENT '付款客户ID',
  `amount` decimal(15,2) NOT NULL COMMENT '收款金额',
  `currency` varchar(10) NOT NULL DEFAULT 'USD' COMMENT '币种',
  `status` tinyint(4) NOT NULL DEFAULT '1' COMMENT '状态:1未认领 2已认领',
  `claim_time` datetime DEFAULT NULL COMMENT '认领时间',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `is_deleted` tinyint(1) DEFAULT '0',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_receipt_no` (`receipt_no`),
  KEY `idx_customer` (`customer_id`)
-- 11. 角色表
CREATE TABLE IF NOT EXISTS `sys_role` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `role_code` varchar(50) NOT NULL COMMENT '角色编码',
  `role_name` varchar(100) NOT NULL COMMENT '角色名称',
  `description` varchar(200) DEFAULT NULL COMMENT '描述',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `is_deleted` tinyint(1) DEFAULT '0',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_role_code` (`role_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='系统角色表';

-- 12. 菜单权限表
CREATE TABLE IF NOT EXISTS `sys_menu` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `parent_id` bigint(20) NOT NULL DEFAULT '0' COMMENT '父菜单ID',
  `menu_name` varchar(100) NOT NULL COMMENT '菜单名称',
  `path` varchar(200) DEFAULT NULL COMMENT '路由路径',
  `component` varchar(200) DEFAULT NULL COMMENT '组件路径',
  `icon` varchar(100) DEFAULT NULL COMMENT '图标',
  `type` tinyint(4) NOT NULL DEFAULT '1' COMMENT '菜单类型(1目录 2菜单 3按钮)',
  `permission` varchar(100) DEFAULT NULL COMMENT '权限标识',
  `sort` int(11) DEFAULT '0' COMMENT '排序',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `is_deleted` tinyint(1) DEFAULT '0',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='系统菜单权限表';

-- 13. 用户-角色关联表
CREATE TABLE IF NOT EXISTS `sys_user_role` (
  `user_id` bigint(20) NOT NULL,
  `role_id` bigint(20) NOT NULL,
  PRIMARY KEY (`user_id`,`role_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户角色关联表';

-- 14. 角色-菜单关联表
CREATE TABLE IF NOT EXISTS `sys_role_menu` (
  `role_id` bigint(20) NOT NULL,
  `menu_id` bigint(20) NOT NULL,
  PRIMARY KEY (`role_id`,`menu_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='角色菜单关联表';

-- 插入默认角色和权限数据
INSERT INTO `sys_role` (`role_code`, `role_name`, `description`) VALUES ('admin', '超级管理员', '系统最高权限');
INSERT INTO `sys_user_role` (`user_id`, `role_id`) VALUES (1, 1);

