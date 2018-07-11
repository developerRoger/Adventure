package com.firstTry.Adventure.utils;
import java.util.Map;


public class PageConverter {
	/**
	 * 将Page对象转换为Map
	 * 
	 * @param page
	 * @return Map
	 */
	public static Map<String, Object> createQueryParamMap(Page<?> page) {
		Map<String, Object> result = ObjectToMapUtil.convertObjectToMap(page.getQueryObj());
		result.put("pageNum", page.getPageSize() * (page.getPageNumber() - 1));
		result.put("pageSize", page.getPageSize());
		return result;
	}
	
	public static void setPageInfo(Map<String, Object> map) {
		Object pageSizeObj = map.get("pageSizeObj");
		Object pageNumberObj = map.get("pageNumberObj");
		if (pageSizeObj != null && pageNumberObj != null) {
			int pageSize = 0;
			int page = 0;
			if (pageSizeObj instanceof String) {
				pageSize = Integer.valueOf((String) pageSizeObj);
			} else if (pageSizeObj instanceof Number) {
				pageSize = ((Number) pageSizeObj).intValue();
			}
			if (pageNumberObj instanceof String) {
				page = Integer.valueOf((String) pageNumberObj);
			} else if (pageNumberObj instanceof Number) {
				page = ((Number) pageNumberObj).intValue();
			}
			map.put("pageNum", pageSize * (page - 1));
			map.put("pageSize", pageSize);
		}
	}
	/**
	 * 初始map分页
	 * @param map
	 */
	public static void setDefaultPageInfo(Map<String, Object> map) {
		map.put("pageNum", 0);
		map.put("pagesSize", 10000);
	}
	/**
	 * 对象转换map分页
	 * @param obj
	 * @return
	 */
	public static Map<String, Object> createDefaultQueryParamMap(Object obj) {
		Map<String, Object> result = ObjectToMapUtil.convertObjectToMap(obj);
		result.put("pageNum", 0);
		result.put("pagesSize", 10000);
		return result;
	}
}
