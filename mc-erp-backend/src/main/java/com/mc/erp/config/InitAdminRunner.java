package com.mc.erp.config;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.mc.erp.entity.Customer;
import com.mc.erp.entity.Menu;
import com.mc.erp.entity.ProductType;
import com.mc.erp.entity.PurchaseOrderDetail;
import com.mc.erp.entity.Role;
import com.mc.erp.entity.RoleMenu;
import com.mc.erp.entity.Supplier;
import com.mc.erp.entity.User;
import com.mc.erp.mapper.CustomerMapper;
import com.mc.erp.mapper.PurchaseOrderDetailMapper;
import com.mc.erp.mapper.SupplierMapper;
import com.mc.erp.service.CustomerService;
import com.mc.erp.service.MenuService;
import com.mc.erp.service.ProductTypeService;
import com.mc.erp.service.RoleMenuService;
import com.mc.erp.service.RoleService;
import com.mc.erp.service.SupplierService;
import com.mc.erp.entity.SalesOrder;
import com.mc.erp.entity.SalesOrderDetail;
import com.mc.erp.entity.PurchaseOrder;
import com.mc.erp.entity.PurchaseOrderItem;
import com.mc.erp.mapper.SalesOrderMapper;
import com.mc.erp.mapper.SalesOrderDetailMapper;
import com.mc.erp.mapper.PurchaseOrderMapper;
import com.mc.erp.mapper.PurchaseOrderItemMapper;
import com.mc.erp.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Component
public class InitAdminRunner implements ApplicationRunner {

    @Autowired
    private UserService userService;

    @Autowired
    private RoleService roleService;

    @Autowired
    private MenuService menuService;

    @Autowired
    private RoleMenuService roleMenuService;

    @Autowired
    private CustomerService customerService;

    @Autowired
    private CustomerMapper customerMapper;

    @Autowired
    private SupplierService supplierService;

    @Autowired
    private SupplierMapper supplierMapper;

    @Autowired
    private ProductTypeService productTypeService;

    @Autowired
    private SalesOrderMapper salesOrderMapper;

    @Autowired
    private SalesOrderDetailMapper salesOrderDetailMapper;

    @Autowired
    private PurchaseOrderMapper purchaseOrderMapper;

    @Autowired
    private PurchaseOrderItemMapper purchaseOrderItemMapper;

    @Autowired
    private PurchaseOrderDetailMapper purchaseOrderDetailMapper;

    @Override
    public void run(ApplicationArguments args) {
       
        User admin = getOrCreateUser("admin", "123456", "Admin");
        if (admin == null) {
            throw new RuntimeException("初始化 admin 用户失败");
        }

        Role adminRole = getOrCreateRole("admin", "超级管理员", "系统最高权限");
        if (adminRole == null) {
            throw new RuntimeException("初始化 admin 角色失败");
        }

        userService.updateUserRoles(admin.getId(), List.of(adminRole.getId()));
        initPermissions(adminRole.getId());
        initSeedData();
    }

    /**
     * 初始化系统菜单权限定义，并将所有权限绑定给 admin 角色。
     * 每行 = 一个可被角色勾选的权限按钮，permission 字段对应 @PreAuthorize 中的权限字符串。
     *
     * type: 0=目录, 1=菜单页面, 2=按钮/操作权限
     */
    private void initPermissions(Long adminRoleId) {
        // ── 系统管理 目录 ───────────────────────────────────
        Long sysDir = getOrCreateMenu(0L, "系统管理", "/system", null, "Setting", 0, null, 1);

        // 用户管理 页面
        Long userMenu = getOrCreateMenu(sysDir, "用户管理", "/system/user", "system/user/index", "User", 1, null, 1);
        Long pUserList = getOrCreateMenu(userMenu, "查看用户",   null, null, null, 2, "system:user:list", 1);
        Long pUserEdit = getOrCreateMenu(userMenu, "编辑用户",   null, null, null, 2, "system:user:edit", 2);

        // 角色管理 页面
        Long roleMenu = getOrCreateMenu(sysDir, "角色管理", "/system/role", "system/role/index", "UserFilled", 1, null, 2);
        Long pRoleList = getOrCreateMenu(roleMenu, "查看角色",   null, null, null, 2, "system:role:list", 1);
        Long pRoleEdit = getOrCreateMenu(roleMenu, "编辑角色",   null, null, null, 2, "system:role:edit", 2);

        // 菜单管理 页面
        Long menuMgr = getOrCreateMenu(sysDir, "菜单管理", "/system/menu", "system/menu/index", "Menu", 1, null, 3);
        Long pMenuList = getOrCreateMenu(menuMgr, "查看菜单",   null, null, null, 2, "system:menu:list", 1);
        Long pMenuEdit = getOrCreateMenu(menuMgr, "编辑菜单",   null, null, null, 2, "system:menu:edit", 2);

        // ── admin 角色绑定所有权限菜单 ──────────────────────
        // 只追加 admin 尚未拥有的菜单，避免重复添加
        List<Long> existing = roleMenuService.getMenuIdsByRoleId(adminRoleId);
        List<Long> allPermMenus = List.of(
                sysDir, userMenu, pUserList, pUserEdit,
                roleMenu, pRoleList, pRoleEdit,
                menuMgr, pMenuList, pMenuEdit
        );
        allPermMenus.stream()
                .filter(id -> id != null && !existing.contains(id))
                .forEach(menuId -> {
                    RoleMenu rm = new RoleMenu();
                    rm.setRoleId(adminRoleId);
                    rm.setMenuId(menuId);
                    roleMenuService.getBaseMapper().insert(rm);
                });
    }

    /**
     * 按 menuName + parentId 幂等地创建菜单，已存在则返回现有 ID。
     */
    private Long getOrCreateMenu(Long parentId, String menuName, String path, String component,
                                  String icon, int type, String permission, int sort) {
        Menu exists = menuService.getOne(new LambdaQueryWrapper<Menu>()
                .eq(Menu::getParentId, parentId)
                .eq(Menu::getMenuName, menuName));
        if (exists != null) return exists.getId();
        Menu menu = new Menu();
        menu.setParentId(parentId);
        menu.setMenuName(menuName);
        menu.setPath(path);
        menu.setComponent(component);
        menu.setIcon(icon);
        menu.setType(type);
        menu.setPermission(permission);
        menu.setSort(sort);
        menuService.save(menu);
        return menu.getId();
    }

    private void initSeedData() {
        Role salesRole = getOrCreateRole("sales", "业务", "销售业务角色");
        getOrCreateRole("operator", "操作", "操作角色");
        getOrCreateRole("purchaser", "采购", "采购角色");
        User salesA = getOrCreateUser("zach", "123456", "赵宇");

        if (salesRole != null && salesA != null) {
            userService.updateUserRoles(salesA.getId(), List.of(salesRole.getId()));
        }

        // ====== 客户 ======
        Long customerId1 = null;
        Long customerId2 = null;
        if (salesA != null) {
            customerId1 = getOrCreateCustomer("测试客户-华东", "中国", "ASIA", "张三", "李四",
                    "cust-east@example.com", "13800000001", salesA.getId());
            customerId2 = getOrCreateCustomer("测试客户-欧洲", "德国", "EUROPE", "Hans", "Mia",
                    "cust-eu@example.com", "13800000002", salesA.getId());
        }

        // ====== 供应商 ======
        Long supplierId1 = getOrCreateSupplier("SUP_TEST_001", "测试供应商一", "赵六", "13900000001", "上海市浦东新区");
        Long supplierId2 = getOrCreateSupplier("SUP_TEST_002", "测试供应商二", "孙七", "13900000002", "天津市滨海新区");

        // ====== 产品类型 ======
        getOrCreateProductType("H型钢");
        getOrCreateProductType("I型钢");
        getOrCreateProductType("热轧卷");

        // ====== 销售订单及明细 ======
        if (salesA != null && customerId1 != null && customerId2 != null) {
            initSalesOrders(salesA.getId(), customerId1, customerId2);
        }

        // ====== 采购订单及明细 ======
        if (supplierId1 != null && supplierId2 != null) {
            initPurchaseOrders(supplierId1, supplierId2);
        }

        System.out.println("====== Seed Data Initialized ======");
    }

    // ----------------------------------------------------------------
    // 销售订单
    // ----------------------------------------------------------------
    private void initSalesOrders(Long salespersonId, Long customerId1, Long customerId2) {

        // ----- SO-TEST-001：H型钢，华东客户 -----
        SalesOrder so1 = getOrCreateSalesOrder("SO-TEST-001", so -> {
            so.setSalespersonId(salespersonId);
            so.setOperatorId(salespersonId);
            so.setCustomerId(customerId1);
            so.setTradeTerm("FOB");
            so.setPaymentMethod("TT");
            so.setCurrency("USD");
            so.setDepositExchangeRate(new BigDecimal("7.10"));
            so.setFinalExchangeRate(new BigDecimal("7.15"));
            so.setContractAmount(new BigDecimal("120000"));
            so.setActualAmount(new BigDecimal("120000"));
            so.setDepositRate(new BigDecimal("30"));
            so.setReceivedAmount(new BigDecimal("36000"));
            so.setFinalPaymentAmount(new BigDecimal("84000"));
            so.setInsuranceFee(new BigDecimal("0.0015"));
            so.setInsuranceAmount(new BigDecimal("180"));
            so.setExpectedReceiptDays(LocalDate.now().plusDays(30));
            so.setDeliveryDate(LocalDate.now().plusDays(15));
            so.setDestinationPort("Shanghai");
            so.setTransportType("海运");
            so.setSeaFreight(new BigDecimal("3000"));
            so.setPortFee(new BigDecimal("500"));
            so.setVat(new BigDecimal("13"));
            so.setContractTotalQuantity(new BigDecimal("60"));
            so.setStatus(1);
        });
        if (so1 != null) {
            // 明细1：H300×300
            insertSalesDetailIfAbsent(so1.getId(), "H300×300", detail -> {
                detail.setProductType("H型钢");
                detail.setMaterial("Q235B");
                detail.setLength("12");
                detail.setTolerance("±2mm");
                detail.setQuantityTon(new BigDecimal("40"));
                detail.setQuantityPc(120);
                detail.setSettlementPrice(new BigDecimal("2000"));
                detail.setPriceTotal(new BigDecimal("80000"));
                detail.setPackaging("裸装");
                detail.setOriginPlace("宝钢");
                detail.setNetWeight(new BigDecimal("40"));
                detail.setGrossWeight(new BigDecimal("40.5"));
                detail.setBundleCount(6);
                detail.setMeasurementMethod("理重");
                detail.setRemark("主规格");
            });
            // 明细2：H200×200
            insertSalesDetailIfAbsent(so1.getId(), "H200×200", detail -> {
                detail.setProductType("H型钢");
                detail.setMaterial("Q345B");
                detail.setLength("9");
                detail.setTolerance("±1.5mm");
                detail.setQuantityTon(new BigDecimal("20"));
                detail.setQuantityPc(80);
                detail.setSettlementPrice(new BigDecimal("2100"));
                detail.setPriceTotal(new BigDecimal("42000"));
                detail.setPackaging("裸装");
                detail.setOriginPlace("马钢");
                detail.setNetWeight(new BigDecimal("20"));
                detail.setGrossWeight(new BigDecimal("20.3"));
                detail.setBundleCount(4);
                detail.setMeasurementMethod("理重");
                detail.setRemark("补充规格");
            });
        }

        // ----- SO-TEST-002：I型钢，欧洲客户 -----
        SalesOrder so2 = getOrCreateSalesOrder("SO-TEST-002", so -> {
            so.setSalespersonId(salespersonId);
            so.setOperatorId(salespersonId);
            so.setCustomerId(customerId2);
            so.setTradeTerm("CIF");
            so.setPaymentMethod("LC");
            so.setCurrency("EUR");
            so.setDepositExchangeRate(new BigDecimal("7.80"));
            so.setFinalExchangeRate(new BigDecimal("7.85"));
            so.setContractAmount(new BigDecimal("75000"));
            so.setActualAmount(new BigDecimal("75000"));
            so.setDepositRate(new BigDecimal("20"));
            so.setReceivedAmount(new BigDecimal("15000"));
            so.setFinalPaymentAmount(new BigDecimal("60000"));
            so.setInsuranceFee(new BigDecimal("0.002"));
            so.setInsuranceAmount(new BigDecimal("150"));
            so.setExpectedReceiptDays(LocalDate.now().plusDays(45));
            so.setDeliveryDate(LocalDate.now().plusDays(25));
            so.setDestinationPort("Hamburg");
            so.setTransportType("海运");
            so.setSeaFreight(new BigDecimal("5000"));
            so.setPortFee(new BigDecimal("800"));
            so.setVat(new BigDecimal("0"));
            so.setContractTotalQuantity(new BigDecimal("45"));
            so.setStatus(1);
        });
        if (so2 != null) {
            // 明细1：I200×150
            insertSalesDetailIfAbsent(so2.getId(), "I200×150", detail -> {
                detail.setProductType("I型钢");
                detail.setMaterial("SS400");
                detail.setLength("6");
                detail.setTolerance("±1.5mm");
                detail.setQuantityTon(new BigDecimal("25"));
                detail.setQuantityPc(200);
                detail.setSettlementPrice(new BigDecimal("1800"));
                detail.setPriceTotal(new BigDecimal("45000"));
                detail.setPackaging("托盘");
                detail.setOriginPlace("武钢");
                detail.setNetWeight(new BigDecimal("25"));
                detail.setGrossWeight(new BigDecimal("25.8"));
                detail.setBundleCount(5);
                detail.setMeasurementMethod("实重");
                detail.setRemark("欧洲客户标准规格");
            });
            // 明细2：I160×82
            insertSalesDetailIfAbsent(so2.getId(), "I160×82", detail -> {
                detail.setProductType("I型钢");
                detail.setMaterial("SS400");
                detail.setLength("6");
                detail.setTolerance("±1mm");
                detail.setQuantityTon(new BigDecimal("20"));
                detail.setQuantityPc(280);
                detail.setSettlementPrice(new BigDecimal("1600"));
                detail.setPriceTotal(new BigDecimal("32000"));
                detail.setPackaging("托盘");
                detail.setOriginPlace("武钢");
                detail.setNetWeight(new BigDecimal("20"));
                detail.setGrossWeight(new BigDecimal("20.5"));
                detail.setBundleCount(4);
                detail.setMeasurementMethod("实重");
                detail.setRemark("欧洲客户辅助规格");
            });
        }

        // ----- SO-TEST-003：热轧卷，华东客户，小批量空运 -----
        SalesOrder so3 = getOrCreateSalesOrder("SO-TEST-003", so -> {
            so.setSalespersonId(salespersonId);
            so.setOperatorId(salespersonId);
            so.setCustomerId(customerId1);
            so.setTradeTerm("FOB");
            so.setPaymentMethod("TT");
            so.setCurrency("USD");
            so.setDepositExchangeRate(new BigDecimal("7.10"));
            so.setFinalExchangeRate(new BigDecimal("7.15"));
            so.setContractAmount(new BigDecimal("36000"));
            so.setActualAmount(new BigDecimal("36000"));
            so.setDepositRate(new BigDecimal("50"));
            so.setReceivedAmount(new BigDecimal("18000"));
            so.setFinalPaymentAmount(new BigDecimal("18000"));
            so.setExpectedReceiptDays(LocalDate.now().plusDays(20));
            so.setDeliveryDate(LocalDate.now().plusDays(5));
            so.setDestinationPort("Shenzhen");
            so.setTransportType("空运");
            so.setVat(new BigDecimal("13"));
            so.setContractTotalQuantity(new BigDecimal("15"));
            so.setStatus(0);
        });
        if (so3 != null) {
            // 明细1：热轧卷 SPCC 1.5mm
            insertSalesDetailIfAbsent(so3.getId(), "卷材1.5×1250", detail -> {
                detail.setProductType("热轧卷");
                detail.setMaterial("SPCC");
                detail.setLength("");
                detail.setTolerance("±0.1mm");
                detail.setQuantityTon(new BigDecimal("8"));
                detail.setCoilInnerDiameter("508mm");
                detail.setSettlementPrice(new BigDecimal("1400"));
                detail.setPriceTotal(new BigDecimal("11200"));
                detail.setPackaging("卷内");
                detail.setOriginPlace("鞍钢");
                detail.setNetWeight(new BigDecimal("8"));
                detail.setGrossWeight(new BigDecimal("8.2"));
                detail.setMeasurementMethod("实重");
                detail.setRemark("1.5mm厚热轧卷");
            });
            // 明细2：热轧卷 SPCC 2.0mm
            insertSalesDetailIfAbsent(so3.getId(), "卷材2.0×1250", detail -> {
                detail.setProductType("热轧卷");
                detail.setMaterial("SPCC");
                detail.setLength("");
                detail.setTolerance("±0.1mm");
                detail.setQuantityTon(new BigDecimal("7"));
                detail.setCoilInnerDiameter("508mm");
                detail.setSettlementPrice(new BigDecimal("1380"));
                detail.setPriceTotal(new BigDecimal("9660"));
                detail.setPackaging("卷内");
                detail.setOriginPlace("鞍钢");
                detail.setNetWeight(new BigDecimal("7"));
                detail.setGrossWeight(new BigDecimal("7.2"));
                detail.setMeasurementMethod("实重");
                detail.setRemark("2.0mm厚热轧卷");
            });
        }
    }

    // ----------------------------------------------------------------
    // 采购订单
    // ----------------------------------------------------------------
    private void initPurchaseOrders(Long supplierId1, Long supplierId2) {

        // ----- PO-TEST-001：关联 SO-TEST-001，H型钢加工采购 -----
        PurchaseOrder po1 = getOrCreatePurchaseOrder("PO-TEST-001", po -> {
            po.setSupplierId(supplierId1);
            po.setSalesOrderNo("SO-TEST-001");
            po.setTotalAmount(new BigDecimal("90000"));
            po.setActualAmount(new BigDecimal("90000"));
            po.setDepositRate(new BigDecimal("30"));
            po.setDepositAmount(new BigDecimal("27000"));
            po.setOrderDate(LocalDate.now());
            po.setDeliveryDate(LocalDate.now().plusDays(10));
            po.setTransportRemark("送货上门，需提前2天通知");
            po.setTotalFreight(new BigDecimal("1200"));
            po.setStatus(1);
        });
        if (po1 != null) {
            Long poId1 = po1.getId();
            // 加工项
            insertPurchaseItemIfAbsent(poId1, "H型钢 加工", new BigDecimal("70000"));
            insertPurchaseItemIfAbsent(poId1, "切割加工", new BigDecimal("12000"));
            insertPurchaseItemIfAbsent(poId1, "喷漆处理", new BigDecimal("5000"));
            insertPurchaseItemIfAbsent(poId1, "质量检测", new BigDecimal("3000"));
            // 物料明细
            insertPurchaseDetailIfAbsent(poId1, "H300×300", detail -> {
                detail.setProductType("H型钢");
                detail.setMaterial("Q235B");
                detail.setLength("12");
                detail.setTolerance("±2mm");
                detail.setQuantityTon(new BigDecimal("40"));
                detail.setQuantityPc(120);
                detail.setSettlementPrice(new BigDecimal("1800"));
                detail.setPriceTotal(new BigDecimal("72000"));
                detail.setPackaging("裸装");
                detail.setOriginPlace("宝钢");
                detail.setNetWeight(new BigDecimal("40"));
                detail.setGrossWeight(new BigDecimal("40.5"));
                detail.setBundleCount(6);
                detail.setMeasurementMethod("理重");
                detail.setProcessingItems("切割,喷漆");
                detail.setRemark("主规格采购");
            });
            insertPurchaseDetailIfAbsent(poId1, "H200×200", detail -> {
                detail.setProductType("H型钢");
                detail.setMaterial("Q345B");
                detail.setLength("9");
                detail.setTolerance("±1.5mm");
                detail.setQuantityTon(new BigDecimal("20"));
                detail.setQuantityPc(80);
                detail.setSettlementPrice(new BigDecimal("1900"));
                detail.setPriceTotal(new BigDecimal("38000"));
                detail.setPackaging("裸装");
                detail.setOriginPlace("马钢");
                detail.setNetWeight(new BigDecimal("20"));
                detail.setGrossWeight(new BigDecimal("20.3"));
                detail.setBundleCount(4);
                detail.setMeasurementMethod("理重");
                detail.setRemark("补充规格采购");
            });
        }

        // ----- PO-TEST-002：关联 SO-TEST-002，I型钢采购 -----
        PurchaseOrder po2 = getOrCreatePurchaseOrder("PO-TEST-002", po -> {
            po.setSupplierId(supplierId2);
            po.setSalesOrderNo("SO-TEST-002");
            po.setTotalAmount(new BigDecimal("55000"));
            po.setActualAmount(new BigDecimal("55000"));
            po.setDepositRate(new BigDecimal("20"));
            po.setDepositAmount(new BigDecimal("11000"));
            po.setOrderDate(LocalDate.now());
            po.setDeliveryDate(LocalDate.now().plusDays(20));
            po.setTransportRemark("港口自提");
            po.setTotalFreight(new BigDecimal("800"));
            po.setStatus(0);
        });
        if (po2 != null) {
            Long poId2 = po2.getId();
            // 加工项
            insertPurchaseItemIfAbsent(poId2, "I型钢 加工", new BigDecimal("50000"));
            insertPurchaseItemIfAbsent(poId2, "包装加固", new BigDecimal("5000"));
            // 物料明细
            insertPurchaseDetailIfAbsent(poId2, "I200×150", detail -> {
                detail.setProductType("I型钢");
                detail.setMaterial("SS400");
                detail.setLength("6");
                detail.setTolerance("±1.5mm");
                detail.setQuantityTon(new BigDecimal("25"));
                detail.setQuantityPc(200);
                detail.setSettlementPrice(new BigDecimal("1600"));
                detail.setPriceTotal(new BigDecimal("40000"));
                detail.setPackaging("托盘");
                detail.setOriginPlace("武钢");
                detail.setNetWeight(new BigDecimal("25"));
                detail.setGrossWeight(new BigDecimal("25.8"));
                detail.setBundleCount(5);
                detail.setMeasurementMethod("实重");
                detail.setRemark("欧洲出口规格");
            });
            insertPurchaseDetailIfAbsent(poId2, "I160×82", detail -> {
                detail.setProductType("I型钢");
                detail.setMaterial("SS400");
                detail.setLength("6");
                detail.setTolerance("±1mm");
                detail.setQuantityTon(new BigDecimal("20"));
                detail.setQuantityPc(280);
                detail.setSettlementPrice(new BigDecimal("1500"));
                detail.setPriceTotal(new BigDecimal("30000"));
                detail.setPackaging("托盘");
                detail.setOriginPlace("武钢");
                detail.setNetWeight(new BigDecimal("20"));
                detail.setGrossWeight(new BigDecimal("20.5"));
                detail.setBundleCount(4);
                detail.setMeasurementMethod("实重");
                detail.setRemark("欧洲出口辅助规格");
            });
        }
    }

    // ----------------------------------------------------------------
    // 辅助方法
    // ----------------------------------------------------------------

    private SalesOrder getOrCreateSalesOrder(String orderNo, java.util.function.Consumer<SalesOrder> setter) {
        LambdaQueryWrapper<SalesOrder> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SalesOrder::getOrderNo, orderNo);
        SalesOrder exists = salesOrderMapper.selectOne(wrapper);
        if (exists != null) {
            return exists;
        }
        SalesOrder so = new SalesOrder();
        so.setOrderNo(orderNo);
        setter.accept(so);
        salesOrderMapper.insert(so);
        return so;
    }

    private void insertSalesDetailIfAbsent(Long orderId, String spec, java.util.function.Consumer<SalesOrderDetail> setter) {
        LambdaQueryWrapper<SalesOrderDetail> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SalesOrderDetail::getOrderId, orderId)
               .eq(SalesOrderDetail::getSpec, spec);
        Long count = salesOrderDetailMapper.selectCount(wrapper);
        if (count > 0) return;
        SalesOrderDetail detail = new SalesOrderDetail();
        detail.setOrderId(orderId);
        detail.setSpec(spec);
        setter.accept(detail);
        salesOrderDetailMapper.insert(detail);
    }

    private PurchaseOrder getOrCreatePurchaseOrder(String poNo, java.util.function.Consumer<PurchaseOrder> setter) {
        LambdaQueryWrapper<PurchaseOrder> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(PurchaseOrder::getPoNo, poNo);
        PurchaseOrder exists = purchaseOrderMapper.selectOne(wrapper);
        if (exists != null) {
            return exists;
        }
        PurchaseOrder po = new PurchaseOrder();
        po.setPoNo(poNo);
        setter.accept(po);
        purchaseOrderMapper.insert(po);
        return po;
    }

    private void insertPurchaseItemIfAbsent(Long purchaseOrderId, String content, BigDecimal amount) {
        LambdaQueryWrapper<PurchaseOrderItem> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(PurchaseOrderItem::getPurchaseOrderId, purchaseOrderId)
               .eq(PurchaseOrderItem::getContent, content);
        Long count = purchaseOrderItemMapper.selectCount(wrapper);
        if (count > 0) return;
        PurchaseOrderItem item = new PurchaseOrderItem();
        item.setPurchaseOrderId(purchaseOrderId);
        item.setContent(content);
        item.setAmount(amount);
        purchaseOrderItemMapper.insert(item);
    }

    private void insertPurchaseDetailIfAbsent(Long purchaseOrderId, String spec,
                                              java.util.function.Consumer<PurchaseOrderDetail> setter) {
        LambdaQueryWrapper<PurchaseOrderDetail> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(PurchaseOrderDetail::getPurchaseOrderId, purchaseOrderId)
               .eq(PurchaseOrderDetail::getSpec, spec);
        Long count = purchaseOrderDetailMapper.selectCount(wrapper);
        if (count > 0) return;
        PurchaseOrderDetail detail = new PurchaseOrderDetail();
        detail.setPurchaseOrderId(purchaseOrderId);
        detail.setSpec(spec);
        setter.accept(detail);
        purchaseOrderDetailMapper.insert(detail);
    }

    private User getOrCreateUser(String username, String password, String realName) {
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getUsername, username);
        User exists = userService.getOne(wrapper);
        if (exists != null) {
            return exists;
        }
        User user = new User();
        user.setUsername(username);
        user.setPassword(password);
        user.setRealName(realName);
        boolean saved = userService.save(user);
        return saved ? user : null;
    }

    private Role getOrCreateRole(String roleCode, String roleName, String description) {
        LambdaQueryWrapper<Role> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Role::getRoleCode, roleCode);
        Role exists = roleService.getOne(wrapper);
        if (exists != null) {
            return exists;
        }
        Role role = new Role();
        role.setRoleCode(roleCode);
        role.setRoleName(roleName);
        role.setDescription(description);
        boolean saved = roleService.save(role);
        return saved ? role : null;
    }

    /** 返回客户 ID，已存在时直接返回，否则创建后返回 */
    private Long getOrCreateCustomer(String name, String country, String continent, String consignee, String notify,
                                     String email, String phone, Long salesUserId) {
        LambdaQueryWrapper<Customer> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Customer::getName, name)
               .eq(Customer::getSalesUserId, salesUserId);
        Customer exists = customerService.getOne(wrapper);
        if (exists != null) {
            return exists.getId();
        }
        Customer customer = new Customer();
        customer.setName(name);
        customer.setCountry(country);
        customer.setContinent(continent);
        customer.setConsignee(consignee);
        customer.setNotify(notify);
        customer.setEmail(email);
        customer.setPhone(phone);
        customer.setSalesUserId(salesUserId);
        customer.setLevel("NORMAL");
        // 初始化阶段没有登录上下文，直接走 Mapper 避免触发 CustomerService 的权限拦截
        customerMapper.insert(customer);
        return customer.getId();
    }

    /** 返回供应商 ID，已存在时直接返回，否则创建后返回 */
    private Long getOrCreateSupplier(String supplierCode, String name, String contactPerson, String phone, String address) {
        LambdaQueryWrapper<Supplier> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Supplier::getSupplierCode, supplierCode);
        Supplier exists = supplierService.getOne(wrapper);
        if (exists != null) {
            return exists.getId();
        }
        Supplier supplier = new Supplier();
        supplier.setSupplierCode(supplierCode);
        supplier.setName(name);
        supplier.setContactPerson(contactPerson);
        supplier.setPhone(phone);
        supplier.setAddress(address);
        supplierService.save(supplier);
        return supplier.getId();
    }

    private void getOrCreateProductType(String typeName) {
        LambdaQueryWrapper<ProductType> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ProductType::getTypeName, typeName);
        ProductType exists = productTypeService.getOne(wrapper);
        if (exists != null) {
            return;
        }
        productTypeService.createType(typeName);
    }
}
