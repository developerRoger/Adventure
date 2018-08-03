package com.firstTry.Adventure.service;

import com.firstTry.Adventure.entity.MenuEntity;

import java.util.List;
import java.util.Map;

/**
 * 
 * 
 * @author Roger
 * @email luojie2luojuan@qq.com
 * @date 2018-08-02 16:33:50
 */
public interface MenuService {
	
	MenuEntity queryObject(Long id);
	
	List<MenuEntity> query(Map<String, Object> map);
	// 按主键删除
   // int deleteByKey(Map<String, Object> keyMap);
	
	int queryTotal(Map<String, Object> map);
	
	void save(MenuEntity menu);
	
	void update(MenuEntity menu);
	
	void delete(Long id);
	
}
