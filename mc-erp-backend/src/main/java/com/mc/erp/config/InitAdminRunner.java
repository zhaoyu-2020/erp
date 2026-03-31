package com.mc.erp.config;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.mc.erp.dto.ProductTypeCreateDTO;
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
import com.mc.erp.entity.FreightForwarder;
import com.mc.erp.mapper.FreightForwarderMapper;
import com.mc.erp.service.FreightForwarderService;
import com.mc.erp.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
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
    private FreightForwarderService freightForwarderService;

    @Autowired
    private FreightForwarderMapper freightForwarderMapper;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private PurchaseOrderDetailMapper purchaseOrderDetailMapper;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public void run(ApplicationArguments args) {
       
        User admin = getOrCreateUser("admin", "123456", "管理员");
        if (admin == null) {
            throw new RuntimeException("初始化 admin 用户失败");
        }

        Role adminRole = getOrCreateRole("admin", "管理员", "系统最高权限");
        if (adminRole == null) {
            throw new RuntimeException("初始化 admin 角色失败");
        }

        userService.updateUserRoles(admin.getId(), List.of(adminRole.getId()));
        initPermissions(adminRole.getId());
        initSeedData(adminRole);
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

    private void initSeedData(Role adminRole) {
        Role salesRole = getOrCreateRole("sales", "业务员", "销售业务角色");
        Role operatorRole = getOrCreateRole("operator", "操作员", "操作角色");
        getOrCreateRole("purchaser", "采购员", "采购角色");
        User salesA = getOrCreateUser("zach", "123456", "赵宇");

        if (salesRole != null && salesA != null) {
            userService.updateUserRoles(salesA.getId(), List.of(salesRole.getId()));
        }

        // ====== 额外用户 ======
        // 管理员
        User cathy = getOrCreateUser("Cathy", "123456", "赵彩霞");
        if (cathy != null && adminRole != null) {
            userService.updateUserRoles(cathy.getId(), List.of(adminRole.getId()));
        }
        User ma = getOrCreateUser("Ma", "123456", "马世军");
        if (ma != null && adminRole != null) {
            userService.updateUserRoles(ma.getId(), List.of(adminRole.getId()));
        }
        // 业务员
        User lisa = getOrCreateUser("lisa", "123456", "姚琪");
        if (lisa != null && salesRole != null) {
            userService.updateUserRoles(lisa.getId(), List.of(salesRole.getId()));
        }
        User nancy = getOrCreateUser("nancy", "123456", "张欣欣");
        if (nancy != null && salesRole != null) {
            userService.updateUserRoles(nancy.getId(), List.of(salesRole.getId()));
        }
        User sophia = getOrCreateUser("Sophia", "123456", "马新月");
        if (sophia != null && salesRole != null) {
            userService.updateUserRoles(sophia.getId(), List.of(salesRole.getId()));
        }
        // 操作员
        User kelly = getOrCreateUser("Kelly", "123456", "乔羽童");
        if (kelly != null && operatorRole != null) {
            userService.updateUserRoles(kelly.getId(), List.of(operatorRole.getId()));
        }
        User shirley = getOrCreateUser("Shirley", "123456", "李羽萱");
        if (shirley != null && operatorRole != null) {
            userService.updateUserRoles(shirley.getId(), List.of(operatorRole.getId()));
        }

        // ====== 客户 ======
    
        if (salesA != null) {
            getOrCreateCustomer("Talla", "塞内加尔", "AFRICA", "talla", "talla",
                    "talla@example.com", "13800000001", salesA.getId());
                    
            getOrCreateCustomer("yann", "加拿大", "NORTH_AMERICA", "yann", "yann",
                    "yann@example.com", "13800000002", salesA.getId());
        }

        if (lisa!=null) {
            getOrCreateCustomer("shive", "黎巴嫩", "AFRICA", "shive", "shive",
                    "sino@example.com", "13800000002", lisa.getId());
        }

        if(sophia!=null) {
            getOrCreateCustomer("Oscar", "圭亚那", "SOUTH_AMERICA", "Oscar", "Oscar",
                    "sino@example.com", "13800000003", sophia.getId());
        }

        // ====== 供应商 ======
        Long supplierId1 = getOrCreateSupplier("TSYY", "月羿", "安呈华", "13900000001", "唐山市路南区");

        // ====== 货代 ======
        getOrCreateFreightForwarder("RJRD", "荣进睿达", "散货", "非洲,欧洲", "郭颖", "13800000100", "上海市浦东新区");

        // ====== 产品类型 ======
        getOrCreateProductType("热轧卷");
        getOrCreateProductType("冷轧卷");
        getOrCreateProductType("角钢");
        getOrCreateProductType("扁钢");
        getOrCreateProductType("槽钢");
        getOrCreateProductType("工字钢");
        getOrCreateProductType("H型钢");
        getOrCreateProductType("I型钢");
        getOrCreateProductType("镀锌卷");
        getOrCreateProductType("彩涂卷");


        System.out.println("====== Seed Data Initialized ======");  
    }

    

    // ----------------------------------------------------------------
    // 辅助方法
    // ----------------------------------------------------------------

    private User getOrCreateUser(String username, String password, String realName) {
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getUsername, username);
        User exists = userService.getOne(wrapper);
        if (exists != null) {
            // 同步更新 realName，确保重启后展示名称保持最新
            if (!realName.equals(exists.getRealName())) {
                exists.setRealName(realName);
                userService.updateById(exists);
            }
            return exists;
        }
        // 物理删除可能残留的同名软删除记录
        jdbcTemplate.update("DELETE FROM sys_user WHERE username = ? AND is_deleted = 1", username);
        User user = new User();
        user.setUsername(username);
        user.setPassword(passwordEncoder.encode(password));
        user.setRealName(realName);
        boolean saved = userService.save(user);
        return saved ? user : null;
    }

    private Role getOrCreateRole(String roleCode, String roleName, String description) {
        LambdaQueryWrapper<Role> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Role::getRoleCode, roleCode);
        Role exists = roleService.getOne(wrapper);
        if (exists != null) {
            // 同步更新名称，确保重启后名称保持最新
            if (!roleName.equals(exists.getRoleName())) {
                exists.setRoleName(roleName);
                exists.setDescription(description);
                roleService.updateById(exists);
            }
            return exists;
        }
        // 物理删除可能残留的同编码软删除记录
        jdbcTemplate.update("DELETE FROM sys_role WHERE role_code = ? AND is_deleted = 1", roleCode);
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
        // 物理删除可能残留的同编码软删除记录
        jdbcTemplate.update("DELETE FROM biz_supplier WHERE supplier_code = ? AND is_deleted = 1", supplierCode);
        Supplier supplier = new Supplier();
        supplier.setSupplierCode(supplierCode);
        supplier.setName(name);
        supplier.setContactPerson(contactPerson);
        supplier.setPhone(phone);
        supplier.setAddress(address);
        supplierService.save(supplier);
        return supplier.getId();
    }

    private void getOrCreateFreightForwarder(String forwarderCode, String name, String freightType,
                                              String marketAdvantage, String contactPerson,
                                              String phone, String address) {
        LambdaQueryWrapper<FreightForwarder> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(FreightForwarder::getForwarderCode, forwarderCode);
        FreightForwarder exists = freightForwarderService.getOne(wrapper);
        if (exists != null) {
            return;
        }
        // 物理删除可能残留的同编码软删除记录
        jdbcTemplate.update("DELETE FROM biz_freight_forwarder WHERE forwarder_code = ? AND is_deleted = 1", forwarderCode);
        FreightForwarder ff = new FreightForwarder();
        ff.setForwarderCode(forwarderCode);
        ff.setName(name);
        ff.setFreightType(freightType);
        ff.setMarketAdvantage(marketAdvantage);
        ff.setContactPerson(contactPerson);
        ff.setPhone(phone);
        ff.setAddress(address);
        freightForwarderMapper.insert(ff);
    }

    private void getOrCreateProductType(String typeName) {
        LambdaQueryWrapper<ProductType> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ProductType::getTypeName, typeName);
        ProductType exists = productTypeService.getOne(wrapper);
        if (exists != null) {
            return;
        }
        ProductTypeCreateDTO dto = new ProductTypeCreateDTO();
        dto.setTypeName(typeName);
        productTypeService.createType(dto);
    }
}
