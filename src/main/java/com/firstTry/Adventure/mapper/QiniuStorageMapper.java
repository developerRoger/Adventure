package com.firstTry.Adventure.mapper;
import com.firstTry.Adventure.config.BaseDao;
import com.firstTry.Adventure.entity.QiniuStorageEntity;
import org.apache.ibatis.annotations.Mapper;

/**
 * 
 * 
 * @author Roger
 * @email luojie2luojuan@qq.com
 * @date 2018-09-22 15:57:37
 */
@Mapper
public interface QiniuStorageMapper extends BaseDao<QiniuStorageEntity>{
//BaseDao已实现基础方法
		//根据主键查找
		QiniuStorageEntity queryObject(Long id);
}
