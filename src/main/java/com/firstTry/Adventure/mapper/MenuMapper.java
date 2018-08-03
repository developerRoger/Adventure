package com.firstTry.Adventure.mapper;

import com.firstTry.Adventure.config.BaseDao;
import com.firstTry.Adventure.entity.MenuEntity;
import org.apache.ibatis.annotations.Mapper;

/**
 * 
 * 
 * @author Roger
 * @email luojie2luojuan@qq.com
 * @date 2018-08-02 16:33:50
 */
@Mapper
public interface MenuMapper extends BaseDao<MenuEntity>{
//BaseDao已实现基础方法
		//根据主键查找
		MenuEntity queryObject(Long id);
}
