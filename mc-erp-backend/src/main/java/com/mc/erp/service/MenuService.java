package com.mc.erp.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.mc.erp.entity.Menu;
import com.mc.erp.vo.MenuVO;
import java.util.List;

public interface MenuService extends IService<Menu> {
    List<MenuVO> getTreeList();
}
