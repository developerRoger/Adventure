package com.firstTry.Adventure.config;

import java.util.List;
import java.util.Map;



/**
 * @version 1.0
 * @author Roger
 * @category 2018.7.31
 * 该类为所有所有Dao的基类方法
 * 里面实现一些简单的增删改查
 */
public interface BaseDao<T> {
	//根据主键查询
	T findById(Long id);
    // 按主键删除
    int deleteByKey(Map<String, Object> keyMap);
    // 单条保存
	int save(T t);
	// 按主键更新全部字段
	int update(T t);
	// 通用条件查询
	List<T> query(Map<String, Object> queryMap);
	//
	void delete(String code);
	//
	void delete(Long id);
	// 查找所有行数
	int queryTotal(Map<String, Object> map);
}
