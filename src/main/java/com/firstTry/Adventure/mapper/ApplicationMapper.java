package com.firstTry.Adventure.mapper;

import org.apache.ibatis.annotations.Mapper;
import com.firstTry.Adventure.config.BaseDao;
import com.firstTry.Adventure.entity.ApplicationEntity;
/**
 * @category 2018-7-31
 * @author Roger
 *
 */
@Mapper
public interface ApplicationMapper extends BaseDao<ApplicationEntity>{
	ApplicationEntity queryObject(String code);
}
