
-- 1. 产品主数据表
CREATE TABLE IF NOT EXISTS `biz_product` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
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
  `create_id` bigint(20) DEFAULT NULL COMMENT '创建人ID',
  `update_id` bigint(20) DEFAULT NULL COMMENT '更新人ID',
  `is_deleted` tinyint(1) DEFAULT '0' COMMENT '逻辑删除:0否 1是',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_id` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='外贸产品主数据表';

-- 1-1. 产品类型表
CREATE TABLE IF NOT EXISTS `biz_product_type` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `type_name` varchar(100) NOT NULL COMMENT '产品类型名称',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `create_id` bigint(20) DEFAULT NULL COMMENT '创建人ID',
  `update_id` bigint(20) DEFAULT NULL COMMENT '更新人ID',
  `is_deleted` tinyint(1) DEFAULT '0',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_type_name` (`type_name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='产品类型字典表';

-- 2. 销售订单主表
CREATE TABLE IF NOT EXISTS `biz_sales_order` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `order_no` varchar(50) NOT NULL COMMENT '外销订单号(如 SO20231018001)',
  `customer_id` bigint(20) NOT NULL COMMENT '客户ID',
  `salesperson_id` bigint(20) NOT NULL COMMENT '业务员ID(归属人)',
  `operator_id` bigint(20) DEFAULT NULL COMMENT '操作员ID',
  `create_id` bigint(20) DEFAULT NULL COMMENT '创建人ID',
  `trade_term` varchar(20) NOT NULL COMMENT '贸易条款(FOB, CIF, EXW 等)',
  `payment_method` varchar(20) DEFAULT NULL COMMENT '付款方式',
  `currency` varchar(10) NOT NULL DEFAULT 'USD' COMMENT '结算币种',
  `deposit_exchange_rate` decimal(10,4) NOT NULL DEFAULT '0.0000' COMMENT '定金汇率',
  `final_exchange_rate` decimal(10,4) NOT NULL DEFAULT '0.0000' COMMENT '尾款汇率',
  `contract_amount` decimal(15,2) NOT NULL DEFAULT '0.00' COMMENT '合同金额',
  `actual_amount` decimal(15,2) NOT NULL DEFAULT '0.00' COMMENT '实际金额',
  `deposit_rate` decimal(5,2) NOT NULL DEFAULT '0.00' COMMENT '定金比例(%)',
  `received_amount` decimal(15,2) NOT NULL DEFAULT '0.00' COMMENT '已定金收款金额',
  `final_payment_amount` decimal(15,2) DEFAULT '0.00' COMMENT '尾款金额',
  `insurance_fee` decimal(15,2) DEFAULT '0.00' COMMENT '保险费用',
  `insurance_amount` decimal(15,2) DEFAULT '0.00' COMMENT '保险金额',
  `expected_receipt_days` date DEFAULT NULL COMMENT '预计收尾款日期',
  `delivery_date` date DEFAULT NULL COMMENT '交货期',
  `destination_port` varchar(100) NOT NULL DEFAULT '' COMMENT '目的港',
  `transport_type` varchar(50) DEFAULT NULL COMMENT '运输类型(集装箱/散货/铁路/汽运)',
  `sea_freight` decimal(15,2) DEFAULT '0.00' COMMENT '海运费(USD)',
  `port_fee` decimal(15,2) DEFAULT '0.00' COMMENT '港杂费',
  `vat` decimal(15,2) DEFAULT '0.00' COMMENT '增值税',
  `profit` decimal(15,2) DEFAULT '0.00' COMMENT '利润',
  `loss` decimal(15,2) DEFAULT NULL COMMENT '损耗(定金+尾款-明细价格汇总之和)',
  `contract_total_quantity` decimal(15,3) DEFAULT NULL COMMENT '合同总数量',
  `settlement_total_quantity` decimal(15,3) DEFAULT NULL COMMENT '结算总数量',
  `status` tinyint(4) NOT NULL DEFAULT '1' COMMENT '状态:1新建 2已收定金 3已采购 4待发运 5已发运 6已收款 7已完成',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `update_id` bigint(20) DEFAULT NULL COMMENT '更新人ID',
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
  `create_id` bigint(20) DEFAULT NULL COMMENT '创建人ID',
  `update_id` bigint(20) DEFAULT NULL COMMENT '更新人ID',
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
  `notify` varchar(200) DEFAULT NULL COMMENT 'notify',
  `email` varchar(100) DEFAULT NULL COMMENT '邮箱',
  `phone` varchar(50) DEFAULT NULL COMMENT '电话',
  `sales_user_id` bigint(20) DEFAULT NULL COMMENT '业务员用户ID',
  `level` varchar(20) DEFAULT 'NORMAL' COMMENT '级别(VIP/NORMAL)',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `create_id` bigint(20) DEFAULT NULL COMMENT '创建人ID',
  `update_id` bigint(20) DEFAULT NULL COMMENT '更新人ID',
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
  `product_type` varchar(100) DEFAULT NULL COMMENT '产品类型(多个用英文逗号分隔)',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `create_id` bigint(20) DEFAULT NULL COMMENT '创建人ID',
  `update_id` bigint(20) DEFAULT NULL COMMENT '更新人ID',
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
  `create_id` bigint(20) DEFAULT NULL COMMENT '创建人ID',
  `update_id` bigint(20) DEFAULT NULL COMMENT '更新人ID',
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
  `salesperson_id` bigint(20) DEFAULT NULL COMMENT '业务员ID',
  `create_id` bigint(20) DEFAULT NULL COMMENT '创建人ID',
  `total_amount` decimal(15,2) NOT NULL DEFAULT '0.00' COMMENT '订单金额',
  `actual_amount` decimal(15,2) NOT NULL DEFAULT '0.00' COMMENT '实际金额',
  `deposit_rate` decimal(5,2) NOT NULL DEFAULT '0.00' COMMENT '定金比例(%)',
  `deposit_amount` decimal(15,2) NOT NULL DEFAULT '0.00' COMMENT '定金金额',
  `order_date` date DEFAULT NULL COMMENT '订单日期',
  `delivery_date` date DEFAULT NULL COMMENT '交货期',
  `transport_remark` varchar(500) DEFAULT NULL COMMENT '运输备注(车辆信息)',
  `total_freight` decimal(15,2) DEFAULT '0.00' COMMENT '总运费',
  `photos` varchar(1000) DEFAULT NULL COMMENT '照片(多张逗号分隔)',
  `material_sheet` varchar(500) DEFAULT NULL COMMENT '材质单',
  `invoice` varchar(500) DEFAULT NULL COMMENT '发票',
  `deposit_slip` varchar(1000) DEFAULT NULL COMMENT '定金水单',
  `final_payment_slip` varchar(1000) DEFAULT NULL COMMENT '尾款水单',
  `freight_slip` varchar(1000) DEFAULT NULL COMMENT '运费水单',
  `status` tinyint(4) NOT NULL DEFAULT '1' COMMENT '状态:1新建 2生产中 3待发货 4已完成',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `update_id` bigint(20) DEFAULT NULL COMMENT '更新人ID',
  `is_deleted` tinyint(1) DEFAULT '0',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_po_no` (`po_no`),
  KEY `idx_supplier` (`supplier_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='采购订单表';

CREATE TABLE IF NOT EXISTS `biz_purchase_order_item` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `purchase_order_id` bigint(20) NOT NULL COMMENT '采购订单ID',
  `content` varchar(500) NOT NULL COMMENT '加工内容',
  `amount` decimal(15,2) NOT NULL DEFAULT '0.00' COMMENT '金额',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `create_id` bigint(20) DEFAULT NULL COMMENT '创建人ID',
  `update_id` bigint(20) DEFAULT NULL COMMENT '更新人ID',
  PRIMARY KEY (`id`),
  KEY `idx_purchase_order` (`purchase_order_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='采购订单加工项表';

-- 6-2. 采购订单明细表（参考销售订单明细）
CREATE TABLE IF NOT EXISTS `biz_purchase_order_detail` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `purchase_order_id` bigint(20) NOT NULL COMMENT '采购订单ID',
  `product_id` bigint(20) DEFAULT NULL COMMENT '产品ID',
  `spec` varchar(100) DEFAULT NULL COMMENT '产品规格',
  `product_type` varchar(100) DEFAULT NULL COMMENT '产品类型',
  `material` varchar(100) DEFAULT NULL COMMENT '材质',
  `length` varchar(50) DEFAULT NULL COMMENT '长度',
  `tolerance` varchar(50) DEFAULT NULL COMMENT '公差',
  `quantity_ton` decimal(15,3) DEFAULT NULL COMMENT '数量(吨)',
  `quantity_pc` int(11) DEFAULT NULL COMMENT '数量(pc)',
  `quantity_meter` decimal(15,3) DEFAULT NULL COMMENT '数量(米)',
  `settlement_price` decimal(15,2) DEFAULT NULL COMMENT '结算价格',
  `measurement_method` varchar(50) DEFAULT NULL COMMENT '计量方式',
  `price_total` decimal(15,2) DEFAULT NULL COMMENT '价格汇总',
  `packaging_weight` decimal(15,3) DEFAULT NULL COMMENT '包装重量',
  `packaging` varchar(200) DEFAULT NULL COMMENT '包装',
  `coil_inner_diameter` varchar(50) DEFAULT NULL COMMENT '卷内径',
  `processing_items` varchar(500) DEFAULT NULL COMMENT '加工项',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注',
  `ordered_quantity` decimal(15,3) DEFAULT NULL COMMENT '订货数量',
  `actual_quantity` decimal(15,3) DEFAULT NULL COMMENT '实际数量',
  `bundle_count` int(11) DEFAULT NULL COMMENT '捆数',
  `net_weight` decimal(15,3) DEFAULT NULL COMMENT '净重',
  `gross_weight` decimal(15,3) DEFAULT NULL COMMENT '毛重',
  `volume` decimal(15,3) DEFAULT NULL COMMENT '体积',
  `origin_place` varchar(200) DEFAULT NULL COMMENT '货源地',
  `actual_theoretical_weight` decimal(15,3) DEFAULT NULL COMMENT '实际理论重量',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `create_id` bigint(20) DEFAULT NULL COMMENT '创建人ID',
  `update_id` bigint(20) DEFAULT NULL COMMENT '更新人ID',
  `is_deleted` tinyint(1) DEFAULT '0',
  PRIMARY KEY (`id`),
  KEY `idx_purchase_order_detail_order` (`purchase_order_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='采购订单明细表';

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
  `create_id` bigint(20) DEFAULT NULL COMMENT '创建人ID',
  `update_id` bigint(20) DEFAULT NULL COMMENT '更新人ID',
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
  `create_id` bigint(20) DEFAULT NULL COMMENT '创建人ID',
  `update_id` bigint(20) DEFAULT NULL COMMENT '更新人ID',
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
  `create_id` bigint(20) DEFAULT NULL COMMENT '创建人ID',
  `update_id` bigint(20) DEFAULT NULL COMMENT '更新人ID',
  `is_deleted` tinyint(1) DEFAULT '0',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_doc_no` (`doc_no`),
  KEY `idx_sales_order` (`sales_order_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='报关单证表';

-- 10. 财务收款单表 (Finance Receipt)
CREATE TABLE IF NOT EXISTS `biz_finance_receipt` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `serial_no` varchar(100) DEFAULT NULL COMMENT '银行流水号',
  `amount` decimal(15,2) NOT NULL COMMENT '收款金额',
  `currency` varchar(10) NOT NULL DEFAULT 'USD' COMMENT '币种',
  `receipt_date` date NOT NULL COMMENT '收款日期',
  `receiving_bank` varchar(200) DEFAULT NULL COMMENT '收款银行',
  `status` tinyint(4) NOT NULL DEFAULT '1' COMMENT '状态:1新建 2认领中 3完成',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `create_id` bigint(20) DEFAULT NULL COMMENT '创建人ID',
  `update_id` bigint(20) DEFAULT NULL COMMENT '更新人ID',
  `is_deleted` tinyint(1) DEFAULT '0',
  PRIMARY KEY (`id`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='财务收款单表';

-- 10b. 财务收款明细表 (Finance Receipt Detail)
CREATE TABLE IF NOT EXISTS `biz_finance_receipt_detail` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `receipt_id` bigint(20) NOT NULL COMMENT '收款单ID',
  `sales_order_id` bigint(20) NOT NULL COMMENT '销售订单ID',
  `sales_order_no` varchar(50) NOT NULL COMMENT '销售订单号',
  `bound_amount` decimal(15,2) NOT NULL COMMENT '绑定金额',
  `bind_type` tinyint(2) DEFAULT NULL COMMENT '绑定类型:1定金 2尾款',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `create_id` bigint(20) DEFAULT NULL COMMENT '创建人ID',
  `update_id` bigint(20) DEFAULT NULL COMMENT '更新人ID',
  `is_deleted` tinyint(1) DEFAULT '0',
  PRIMARY KEY (`id`),
  KEY `idx_receipt_id` (`receipt_id`),
  KEY `idx_sales_order_id` (`sales_order_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='财务收款明细表';

-- 11. 角色表
CREATE TABLE IF NOT EXISTS `sys_role` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `role_code` varchar(50) NOT NULL COMMENT '角色编码',
  `role_name` varchar(100) NOT NULL COMMENT '角色名称',
  `description` varchar(200) DEFAULT NULL COMMENT '描述',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `create_id` bigint(20) DEFAULT NULL COMMENT '创建人ID',
  `update_id` bigint(20) DEFAULT NULL COMMENT '更新人ID',
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
  `create_id` bigint(20) DEFAULT NULL COMMENT '创建人ID',
  `update_id` bigint(20) DEFAULT NULL COMMENT '更新人ID',
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

-- 15. 销售订单详情表
CREATE TABLE IF NOT EXISTS `biz_sales_order_detail` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `order_id` bigint(20) NOT NULL COMMENT '销售订单ID',
  `product_id` bigint(20) DEFAULT NULL COMMENT '关联产品ID',
  `spec` varchar(100) DEFAULT NULL COMMENT '产品规格',
  `product_type` varchar(100) DEFAULT NULL COMMENT '产品类型',
  `material` varchar(100) DEFAULT NULL COMMENT '材质',
  `length` varchar(50) DEFAULT NULL COMMENT '长度(米)',
  `tolerance` varchar(50) DEFAULT NULL COMMENT '公差',
  `quantity_ton` decimal(15,3) DEFAULT NULL COMMENT '数量(t/吨)',
  `quantity_pc` int(11) DEFAULT NULL COMMENT '数量(pc/片)',
  `quantity_meter` decimal(15,2) DEFAULT NULL COMMENT '数量(m/米)',
  `settlement_price` decimal(15,2) DEFAULT NULL COMMENT '结算价格',
  `price_total` decimal(15,2) DEFAULT NULL COMMENT '价格汇总(结算价格*吨数)',
  `packaging_weight` decimal(15,3) DEFAULT NULL COMMENT '包装重量',
  `packaging` varchar(200) DEFAULT NULL COMMENT '包装',
  `coil_inner_diameter` varchar(50) DEFAULT NULL COMMENT '卷内径(mm)',
  `processing_items` varchar(500) DEFAULT NULL COMMENT '加工项',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注',

  `ordered_quantity` decimal(15,3) DEFAULT NULL COMMENT '订货数量',
  `actual_quantity` decimal(15,3) DEFAULT NULL COMMENT '实际结算重量',
  `actual_theoretical_weight` decimal(15,3) DEFAULT NULL COMMENT '实际理论重量',
  `bundle_count` int(11) DEFAULT NULL COMMENT '捆数',
  `net_weight` decimal(15,3) DEFAULT NULL COMMENT '净重',
  `gross_weight` decimal(15,3) DEFAULT NULL COMMENT '毛重',
  `volume` decimal(15,4) DEFAULT NULL COMMENT '体积',
  `origin_place` varchar(200) DEFAULT NULL COMMENT '货源地',
  `measurement_method` varchar(50) DEFAULT NULL COMMENT '计量方式(理论重量/过磅重量/个/支/捆/套)',
  
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `create_id` bigint(20) DEFAULT NULL COMMENT '创建人ID',
  `update_id` bigint(20) DEFAULT NULL COMMENT '更新人ID',
  `is_deleted` tinyint(1) DEFAULT '0',
  PRIMARY KEY (`id`),
  KEY `idx_order_id` (`order_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='销售订单详情表';
