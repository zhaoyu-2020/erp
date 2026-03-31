package com.mc.erp.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.mc.erp.common.PageResult;
import com.mc.erp.dto.CustomerQuery;
import com.mc.erp.entity.Customer;
import com.mc.erp.entity.SalesOrder;
import com.mc.erp.entity.User;
import com.mc.erp.mapper.CustomerMapper;
import com.mc.erp.service.CustomerService;
import com.mc.erp.service.SalesOrderService;
import com.mc.erp.service.UserService;
import com.mc.erp.util.SecurityUtil;
import com.mc.erp.vo.CustomerVO;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.io.Serializable;
import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class CustomerServiceImpl extends ServiceImpl<CustomerMapper, Customer> implements CustomerService {

    @Autowired
    private UserService userService;

    @Autowired
    private SalesOrderService salesOrderService;

    @Override
    public PageResult<CustomerVO> getPage(CustomerQuery query) {
        Long currentUserId = SecurityUtil.getCurrentUserId();
        if (currentUserId == null) {
            throw new AccessDeniedException("无操作权限");
        }

        Page<Customer> page = new Page<>(query.getPageNum(), query.getPageSize());
        LambdaQueryWrapper<Customer> wrapper = new LambdaQueryWrapper<>();

        // 管理员和操作员可以查看所有客户；其他用户仅查看归属自己的客户
        if (!SecurityUtil.isAdmin() && !SecurityUtil.hasRole("operator")) {
            wrapper.eq(Customer::getSalesUserId, currentUserId);
        }

        wrapper.like(StringUtils.hasText(query.getName()), Customer::getName, query.getName());
        wrapper.eq(StringUtils.hasText(query.getContinent()), Customer::getContinent, query.getContinent());
        wrapper.eq(query.getSalesUserId() != null, Customer::getSalesUserId, query.getSalesUserId());
        wrapper.orderByDesc(Customer::getCreateTime);

        Page<Customer> resultPage = this.page(page, wrapper);

        Set<Long> userIds = resultPage.getRecords().stream().map(Customer::getSalesUserId).filter(Objects::nonNull).collect(Collectors.toSet());
        Map<Long, String> userNameMap = userIds.isEmpty()
                ? Collections.emptyMap()
                : userService.listByIds(userIds).stream().collect(Collectors.toMap(User::getId, User::getUsername));

        var voList = resultPage.getRecords().stream().map(cust -> {
            CustomerVO vo = new CustomerVO();
            BeanUtils.copyProperties(cust, vo);
            vo.setSalesUserName(userNameMap.get(cust.getSalesUserId()));
            return vo;
        }).collect(Collectors.toList());

        return new PageResult<>(resultPage.getTotal(), voList);
    }

    @Override
    public boolean save(Customer entity) {
        normalizeCustomerPayload(entity);
        Long currentUserId = SecurityUtil.getCurrentUserId();
        if (currentUserId == null) {
            throw new AccessDeniedException("无操作权限");
        }
        if (!SecurityUtil.isAdmin()) {
            // 普通用户只能创建属于自己的客户
            entity.setSalesUserId(currentUserId);
        }
        return super.save(entity);
    }

    @Override
    public boolean updateById(Customer entity) {
        normalizeCustomerPayload(entity);
        Long currentUserId = SecurityUtil.getCurrentUserId();
        if (currentUserId == null) {
            throw new AccessDeniedException("无操作权限");
        }
        Customer exist = this.getById(entity.getId());
        if (exist == null) {
            return false;
        }
        if (!SecurityUtil.isAdmin() && !currentUserId.equals(exist.getSalesUserId())) {
            throw new AccessDeniedException("无操作权限");
        }
        // 普通用户只能修改属于自己的客户
        if (!SecurityUtil.isAdmin()) {
            entity.setSalesUserId(currentUserId);
        }
        return super.updateById(entity);
    }

    @Override
    public boolean removeById(Serializable id) {
        Long currentUserId = SecurityUtil.getCurrentUserId();
        if (currentUserId == null) {
            throw new AccessDeniedException("无操作权限");
        }
        Customer exist = this.getById(id);
        if (exist == null) {
            return false;
        }
        if (!SecurityUtil.isAdmin() && !currentUserId.equals(exist.getSalesUserId())) {
            throw new AccessDeniedException("无操作权限");
        }
        // 校验是否有关联的销售订单
        long refCount = salesOrderService.count(new LambdaQueryWrapper<SalesOrder>()
                .eq(SalesOrder::getCustomerId, id));
        if (refCount > 0) {
            throw new RuntimeException("该客户下还有 " + refCount + " 个销售订单，无法删除");
        }
        return super.removeById(id);
    }

    @Override
    public CustomerVO getByIdWithSalesUser(Long id) {
        Long currentUserId = SecurityUtil.getCurrentUserId();
        if (currentUserId == null) {
            throw new AccessDeniedException("无操作权限");
        }

        Customer cust = this.getById(id);
        if (cust == null) {
            return null;
        }

        if (!SecurityUtil.isAdmin() && !SecurityUtil.hasRole("operator") && !currentUserId.equals(cust.getSalesUserId())) {
            throw new AccessDeniedException("无操作权限");
        }

        CustomerVO vo = new CustomerVO();
        BeanUtils.copyProperties(cust, vo);
        if (cust.getSalesUserId() != null) {
            User user = userService.getById(cust.getSalesUserId());
            if (user != null) {
                vo.setSalesUserName(user.getUsername());
            }
        }
        return vo;
    }

    /**
     * 前端空字符串会触发后端 @Email 校验失败，这里统一标准化为空值。
     */
    private void normalizeCustomerPayload(Customer entity) {
        if (entity == null) {
            return;
        }
        if (!StringUtils.hasText(entity.getEmail())) {
            entity.setEmail(null);
        } else {
            entity.setEmail(entity.getEmail().trim());
        }
    }
}
