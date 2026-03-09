
-- 1. 产品主数据表
CREATE TABLE IF NOT EXISTS `biz_product` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `spu_code` varchar(50) NOT NULL COMMENT '产品编码(内部)',
  `hs_code` varchar(20) DEFAULT NULL COMMENT '海关编码',
  `type` varchar(50) DEFAULT NULL COMMENT '产品类型',
  `spec` varchar(100) DEFAULT NULL COMMENT '规格',
  `material` varchar(100) DEFAULT NULL COMMENT '材质',
  `length` varchar(50) DEFAULT NULL COMMENT '长度',
  `meter_weight` varchar(50) DEFAULT NULL COMMENT '米重',
  `tolerance` varchar(50) DEFAULT NULL COMMENT '公差',
  `declaration` varchar(255) DEFAULT NULL COMMENT '申报要素',
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
  `deposit_exchange_rate` decimal(10,4) NOT NULL DEFAULT '0.0000' COMMENT '定金汇率',
  `final_exchange_rate` decimal(10,4) NOT NULL DEFAULT '0.0000' COMMENT '尾款汇率',
  `contract_amount` decimal(15,2) NOT NULL DEFAULT '0.00' COMMENT '合同金额',
  `actual_amount` decimal(15,2) NOT NULL DEFAULT '0.00' COMMENT '实际金额',
  `deposit_rate` decimal(5,2) NOT NULL DEFAULT '0.00' COMMENT '定金比例(%)',
  `received_amount` decimal(15,2) NOT NULL DEFAULT '0.00' COMMENT '已收款金额',
  `expected_receipt_days` int(11) NOT NULL DEFAULT '0' COMMENT '预计收尾款天数',
  `transport_type` varchar(50) DEFAULT NULL COMMENT '运输类型(集装箱/散货/铁路/汽运)',
  `sea_freight` decimal(15,2) DEFAULT '0.00' COMMENT '海运费',
  `port_fee` decimal(15,2) DEFAULT '0.00' COMMENT '港杂费',
  `vat` decimal(15,2) DEFAULT '0.00' COMMENT '增值税',
  `profit` decimal(15,2) DEFAULT '0.00' COMMENT '利润',
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
  `product_id` bigint(20) DEFAULT NULL COMMENT '产品ID',
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
  `name` varchar(200) NOT NULL COMMENT '客户名称',
  `country` varchar(100) DEFAULT NULL COMMENT '国家/地区',
  `continent` varchar(20) DEFAULT NULL COMMENT '洲别(ASIA/EUROPE/NORTH_AMERICA/SOUTH_AMERICA/AFRICA/OCEANIA/ANTARCTICA)',
  `consignee` varchar(200) DEFAULT NULL COMMENT '收货人',
  `notify` varchar(200) DEFAULT NULL COMMENT '通知人',
  `email` varchar(100) DEFAULT NULL COMMENT '邮箱',
  `phone` varchar(50) DEFAULT NULL COMMENT '电话',
  `sales_user_id` bigint(20) DEFAULT NULL COMMENT '业务员用户ID',
  `level` varchar(20) DEFAULT 'NORMAL' COMMENT '级别(VIP/NORMAL)',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `is_deleted` tinyint(1) DEFAULT '0',
  PRIMARY KEY (`id`)
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

-- 5-1. 供应商账户信息表
CREATE TABLE IF NOT EXISTS `biz_supplier_account` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `supplier_id` bigint(20) NOT NULL COMMENT '供应商ID',
  `bank_name` varchar(100) NOT NULL COMMENT '开户银行',
  `account_name` varchar(100) NOT NULL COMMENT '账户名称',
  `account_no` varchar(100) NOT NULL COMMENT '银行账号',
  `currency` varchar(20) DEFAULT 'CNY' COMMENT '币种',
  `swift_code` varchar(50) DEFAULT NULL COMMENT 'SWIFT码',
  `remark` varchar(255) DEFAULT NULL COMMENT '备注',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `is_deleted` tinyint(1) DEFAULT '0',
  PRIMARY KEY (`id`),
  KEY `idx_supplier_id` (`supplier_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='供应商账户信息表';

-- 6. 采购订单表
CREATE TABLE IF NOT EXISTS `biz_purchase_order` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `po_no` varchar(50) NOT NULL COMMENT '采购单号',
  `supplier_id` bigint(20) NOT NULL COMMENT '供应商ID',
  `sales_order_no` varchar(50) DEFAULT NULL COMMENT '关联销售订单号',
  `total_amount` decimal(15,2) NOT NULL DEFAULT '0.00' COMMENT '订单金额',
  `actual_amount` decimal(15,2) NOT NULL DEFAULT '0.00' COMMENT '实际金额',
  `deposit_rate` decimal(5,2) NOT NULL DEFAULT '0.00' COMMENT '定金比例(%)',
  `deposit_amount` decimal(15,2) NOT NULL DEFAULT '0.00' COMMENT '定金金额',
  `delivery_date` date DEFAULT NULL COMMENT '交货期',
  `transport_remark` varchar(500) DEFAULT NULL COMMENT '运输备注(车辆信息)',
  `total_freight` decimal(15,2) DEFAULT '0.00' COMMENT '总运费',
  `photos` varchar(1000) DEFAULT NULL COMMENT '照片(多张逗号分隔)',
  `material_sheet` varchar(500) DEFAULT NULL COMMENT '材质单',
  `invoice` varchar(500) DEFAULT NULL COMMENT '发票',
  `status` tinyint(4) NOT NULL DEFAULT '1' COMMENT '状态:1已签合同 2已付定金 3生产完成 4待付尾款 5已发货 6已完结',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `is_deleted` tinyint(1) DEFAULT '0',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_po_no` (`po_no`),
  KEY `idx_supplier` (`supplier_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='采购订单表';

-- 6-1. 采购订单加工项表
CREATE TABLE IF NOT EXISTS `biz_purchase_order_item` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `purchase_order_id` bigint(20) NOT NULL COMMENT '采购订单ID',
  `content` varchar(500) NOT NULL COMMENT '加工内容',
  `amount` decimal(15,2) NOT NULL DEFAULT '0.00' COMMENT '金额',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_purchase_order` (`purchase_order_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='采购订单加工项表';

-- 7. 用户表
CREATE TABLE IF NOT EXISTS `sys_user` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `username` varchar(50) NOT NULL COMMENT '用户名',
  `password` varchar(100) NOT NULL COMMENT '密码',
  `real_name` varchar(50) DEFAULT NULL COMMENT '真实姓名',
  `phone` varchar(20) DEFAULT NULL COMMENT '手机号',
  `email` varchar(100) DEFAULT NULL COMMENT '邮箱',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `is_deleted` tinyint(1) DEFAULT '0',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_username` (`username`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='系统用户表';

-- 插入一条默认测试用户


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
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='财务水单表';

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
