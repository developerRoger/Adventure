package com.firstTry.Adventure.service;
import java.util.List;
import java.util.Map;

/**
 * 代码生成器
 * 
 * @author Roger
 * @email luojie2luojuan@qq.com
 * @date 2018-3-19
 */
public interface SysGeneratorService {

	public List<Map<String, Object>> queryList(Map<String, Object> map);

	public int queryTotal(Map<String, Object> map);
	
	public Map<String, String> queryTable(String tableName);

	public List<Map<String, String>> queryColumns(String tableName);

	public byte[] generatorCode(String[] tableNames);
}
