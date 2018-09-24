package com.firstTry.Adventure.service;

import com.firstTry.Adventure.entity.QiniuStorageEntity;

import java.util.List;
import java.util.Map;

/**
 * 
 * 
 * @author Roger
 * @email luojie2luojuan@qq.com
 * @date 2018-09-22 15:57:37
 */
public interface QiniuStorageService {
	
	QiniuStorageEntity queryObject(Long id);
	
	List<QiniuStorageEntity> query(Map<String, Object> map);
	// 按主键删除
   // int deleteByKey(Map<String, Object> keyMap);
	
	int queryTotal(Map<String, Object> map);
	
	void save(QiniuStorageEntity qiniuStorage);
	
	void update(QiniuStorageEntity qiniuStorage);
	
	void delete(Long id);
	
}
