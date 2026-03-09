package com.mc.erp.config;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.mc.erp.entity.Customer;
import com.mc.erp.entity.ProductType;
import com.mc.erp.entity.Role;
import com.mc.erp.entity.Supplier;
import com.mc.erp.entity.User;
import com.mc.erp.mapper.CustomerMapper;
import com.mc.erp.service.CustomerService;
import com.mc.erp.service.ProductTypeService;
import com.mc.erp.service.RoleService;
import com.mc.erp.service.SupplierService;
import com.mc.erp.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class InitAdminRunner implements ApplicationRunner {

    @Autowired
    private UserService userService;

    @Autowired
    private RoleService roleService;

    @Autowired
    private CustomerService customerService;

    @Autowired
    private CustomerMapper customerMapper;

    @Autowired
    private SupplierService supplierService;

    @Autowired
    private ProductTypeService productTypeService;

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
        initSeedData();
    }

    private void initSeedData() {
        Role salesRole = getOrCreateRole("sales", "业务员", "销售业务角色");
        User salesA = getOrCreateUser("zach", "123456", "赵宇");

        if (salesRole != null) {
            if (salesA != null) {
                userService.updateUserRoles(salesA.getId(), List.of(salesRole.getId()));
            }
           
        }

        if (salesA != null) {
            getOrCreateCustomer("测试客户-华东", "中国", "ASIA", "张三", "李四", "cust-east@example.com", "13800000001", salesA.getId());
            getOrCreateCustomer("测试客户-欧洲", "德国", "EUROPE", "Hans", "Mia", "cust-eu@example.com", "13800000002", salesA.getId());
        }
       

        getOrCreateSupplier("SUP_TEST_001", "测试供应商一", "赵六", "13900000001", "上海市浦东新区");
        getOrCreateSupplier("SUP_TEST_002", "测试供应商二", "孙七", "13900000002", "天津市滨海新区");

        getOrCreateProductType("H型钢");
        getOrCreateProductType("I型钢");
        getOrCreateProductType("热轧卷");

        System.out.println("====== Seed Data Initialized ======");
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

    private void getOrCreateCustomer(String name, String country, String continent, String consignee, String notify,
                                     String email, String phone, Long salesUserId) {
        LambdaQueryWrapper<Customer> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Customer::getName, name);
        wrapper.eq(Customer::getSalesUserId, salesUserId);
        Customer exists = customerService.getOne(wrapper);
        if (exists != null) {
            return;
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
    }

    private void getOrCreateSupplier(String supplierCode, String name, String contactPerson, String phone, String address) {
        LambdaQueryWrapper<Supplier> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Supplier::getSupplierCode, supplierCode);
        Supplier exists = supplierService.getOne(wrapper);
        if (exists != null) {
            return;
        }

        Supplier supplier = new Supplier();
        supplier.setSupplierCode(supplierCode);
        supplier.setName(name);
        supplier.setContactPerson(contactPerson);
        supplier.setPhone(phone);
        supplier.setAddress(address);
        supplierService.save(supplier);
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
