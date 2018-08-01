package com.firstTry.Adventure.service;

import com.firstTry.Adventure.entity.ApplicationEntity;

import java.util.List;
import java.util.Map;

/**
 * 
 * 
 * @author Roger
 * @email luojie2luojuan@qq.com
 * @date 2018-08-01 14:29:11
 */
public interface ApplicationService {
	
	ApplicationEntity queryObject(String code);
	
	List<ApplicationEntity> query(Map<String, Object> map);
	// 按主键删除
   // int deleteByKey(Map<String, Object> keyMap);
	
	int queryTotal(Map<String, Object> map);
	
	void save(ApplicationEntity application);
	
	void update(ApplicationEntity application);
	
	void delete(String code);
}
