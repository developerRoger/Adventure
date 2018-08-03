package com.firstTry.Adventure.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

import com.firstTry.Adventure.mapper.MenuMapper;
import com.firstTry.Adventure.entity.MenuEntity;
import com.firstTry.Adventure.service.MenuService;



@Service("menuService")
public class MenuServiceImpl implements MenuService {
	@Autowired
	private MenuMapper menuDao;
	
	@Override
	public MenuEntity queryObject(Long id){
		return menuDao.queryObject(id);
	}
	
	@Override
	public List<MenuEntity> query(Map<String, Object> map){
		return menuDao.query(map);
	}
	
	@Override
	public int queryTotal(Map<String, Object> map){
		return menuDao.queryTotal(map);
	}
	
	@Override
	public void save(MenuEntity menu){
		menuDao.save(menu);
	}
	
	@Override
	public void update(MenuEntity menu){
		menuDao.update(menu);
	}
	
	@Override
	public void delete(Long id){
		menuDao.delete(id);
	}
	
	
}
