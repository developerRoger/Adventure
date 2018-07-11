package com.firstTry.Adventure.mapper;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Mapper;

/**
 * 代码生成器
 * 
 * @author Roger
 * @email luojie2luojuan@qq.com
 * @date 2018-3-19
 */
@Mapper
public interface SysGeneratorMapper {
	
	List<Map<String, Object>> queryList(Map<String, Object> map);
	
	int queryTotal(Map<String, Object> map);
	
	Map<String, String> queryTable(String tableName);
	
	List<Map<String, String>> queryColumns(String tableName);
}
