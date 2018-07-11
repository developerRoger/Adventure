package com.firstTry.Adventure.utils;

import java.util.HashMap;
import java.util.Map;
import org.apache.commons.beanutils.BeanMap;
/**
 * 
 * @author Roger
 * @email luojie2luojuan@qq.com
 * @date 2018-3-19
 *
 */
public class ObjectToMapUtil {
	/**
	 * 将对象转为Map
	 * @param obj
	 * @return Map
	 */
	public static Map<String, Object> convertObjectToMap(Object obj) {
		Map<String, Object> map = new HashMap<>();
		BeanMap beanMap = new BeanMap(obj);
		for (Object key : beanMap.keySet()) {
			if (key instanceof String && !"class".equals(key)) {
				map.put((String) key, beanMap.get(key));
			}
		}
		return map;
	}

}
