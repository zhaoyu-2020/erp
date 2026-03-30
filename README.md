# MC ERP — 外贸钢材企业资源管理系统

面向外贸钢材出口企业的全流程 ERP 系统，覆盖销售、采购、物流、财务、仓储等核心业务模块。

## 技术栈

### 后端 (`mc-erp-backend`)

| 技术 | 版本 | 用途 |
|------|------|------|
| Java | 17 | 运行时 |
| Spring Boot | 3.1.5 | 应用框架 |
| Spring Security + JWT | — | 认证与授权 |
| MyBatis-Plus | 3.5.4 | ORM / 数据访问层 |
| MySQL | — | 关系型数据库 |
| EasyExcel | 3.3.2 | Excel 导入导出 |
| Maven | — | 构建工具 |

### 前端 (`mc-erp-frontend`)

| 技术 | 版本 | 用途 |
|------|------|------|
| Vue 3 | ^3.5 | UI 框架（Composition API） |
| TypeScript | ~5.9 | 类型安全 |
| Vite | ^7.3 | 构建工具 |
| Element Plus | ^2.13 | UI 组件库 |
| Pinia | ^3.0 | 状态管理 |
| Vue Router | ^5.0 | 客户端路由 |
| Axios | ^1.13 | HTTP 客户端 |
| ECharts | ^6.0 | 数据可视化 |

## 快速启动

### 环境要求

- JDK 17+
- Node.js 18+
- MySQL 8.0+

### 后端

```bash
# 1. 创建数据库
mysql -u root -p -e "CREATE DATABASE mc_erp CHARACTER SET utf8mb4;"

# 2. 修改数据库连接配置
# 编辑 mc-erp-backend/src/main/resources/application.yml

# 3. 启动（首次启动会自动执行 schema.sql 建表并插入测试数据）
cd mc-erp-backend
mvn spring-boot:run
```

服务启动后监听 `http://localhost:8080`。

### 前端

```bash
cd mc-erp-frontend
npm install
npm run dev
```

前端开发服务器启动于 `http://localhost:3000`，`/api` 请求自动代理至后端。

### 默认账号

| 用户名 | 密码 | 角色 |
|--------|------|------|
| admin | 123456 | 超级管理员 |
| zach | 123456 | 业务员 |

## 业务流程

```
创建销售订单 → 录入订单明细 → 财务收款/绑定定金
       ↓
  创建采购订单 → 录入采购明细 → 采购跟进（生产中 → 待发货）
       ↓
  创建海运订单 → 登记海运费/地面费用
       ↓
  报关出运 → 财务收尾款 → 管理员归档结算
```

## 功能模块

### 销售订单

状态流转：`新建 → 已收定金 → 已采购 → 待发运 → 已发运 → 已收款 → 已完成`

- 完整的 CRUD 与分页查询
- 字段：订单号、业务员、客户、贸易条款（FOB/CIF/EXW）、币种、定金/尾款汇率、合同金额、定金比例、运输类型、海运费、港杂费、增值税、利润/亏损
- 状态变更为"已完成"时自动计算利润/亏损
- 支持 Excel 批量导入（合同头部 + 明细两套模板）

### 采购订单

状态流转：`新建 → 生产中 → 待发货 → 已完成`

- 关联销售订单号（以订单号为外键）
- 字段：供应商、合同金额、定金比例/金额、交货期、总运费、加工项（一对多）
- 附件上传：照片、材质单、发票、付款水单
- 支持 Excel 批量导入（合同头部 + 明细两套模板）

### 海运订单

状态流转：`草稿 → 已提交 → 已结算`（任意阶段可作废）

- 订单编号自动生成：`HD + YYYYMMDD + 6位序号`
- 运输方式：集装箱（柜型/柜量/柜号/拼柜）或散货（重量/体积/舱位/船名航次）
- 费用明细：海运费 + 地面费用（多行条目）
- 可选货物保险：保额、保费、币种
- 完整操作审计日志

### 财务收款

- 收款单：流水号、金额、币种、收款日期、收款银行、状态（新建/认领中/完成）
- 收款明细：一笔收款可拆分绑定至多个销售订单（定金或尾款）

### 客户管理

- 字段：名称、国家、大洲（七大洲）、Consignee、Notify Party、邮箱、电话、负责业务员、客户等级（VIP/普通）

### 供应商管理

- 支持多个银行账户（银行名称、账户名、账号、币种、SWIFT 代码）

### 产品管理

- 字段：中/英文名称、产品类型、规格、材质、长度、米重、公差、申报要素、HS 编码、退税率、计量单位

### 报关单证

- 状态：未申报 / 申报中 / 已放行
- 关联销售订单

### 仓储管理 (WMS)

- 库存记录：当前库存量、可用量、锁定量

### 报表与驾驶舱

- 业务驾驶舱：ECharts 数据可视化（可按日期范围筛选）
- 销售报表、财务报表

### 系统管理

- 用户、角色、菜单（目录/菜单/按钮三级权限树）
- 基于 JWT 的无状态认证，BCrypt 密码加密

## 数据库结构

数据库名：`mc_erp`。所有业务表均支持软删除（`is_deleted` 字段）。

| 表名前缀 | 说明 |
|----------|------|
| `biz_*` | 业务数据（产品、客户、供应商、销售/采购订单、财务、仓储） |
| `sys_*` | 系统数据（用户、角色、菜单及其关联关系） |
| `erp_*` | 海运订单及费用明细、操作日志 |

完整建表 DDL 见：`mc-erp-backend/src/main/resources/schema.sql`

## API 说明

所有接口前缀为 `/api/v1/`，除 `/api/v1/auth/**` 和 `/uploads/**` 外均需携带 JWT Token。

主要路由组：

| 路由组 | 说明 |
|--------|------|
| `/auth` | 登录认证 |
| `/sales-orders` | 销售订单（含 Excel 导入） |
| `/sales-order-details` | 销售订单明细 |
| `/purchase-orders` | 采购订单（含 Excel 导入） |
| `/purchase-order-details` | 采购订单明细 |
| `/freight-orders` | 海运订单（含费用明细、审计日志） |
| `/finance-receipts` | 财务收款单 |
| `/customers` | 客户管理 |
| `/suppliers` | 供应商管理 |
| `/products` | 产品管理 |
| `/stocks` | 仓储库存 |
| `/customs-docs` | 报关单证 |
| `/reports` | 报表（驾驶舱/销售/财务） |
| `/files/upload` | 文件上传 |
| `/users` `/roles` `/menus` | 系统管理 |

## 配置说明

核心配置文件：`mc-erp-backend/src/main/resources/application.yml`

```yaml
server:
  port: 8080

spring:
  datasource:
    url: jdbc:mysql://127.0.0.1:3306/mc_erp
    username: root
    password: root

file:
  upload-dir: uploads
  base-url: http://localhost:8080/uploads
```

文件上传限制：单文件 50MB，单次请求 200MB。


# todo
所有路由，初始化