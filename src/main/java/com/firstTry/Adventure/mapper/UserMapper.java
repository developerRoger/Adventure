package com.firstTry.Adventure.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import com.firstTry.Adventure.config.BaseDao;
import com.firstTry.Adventure.entity.UserTest;
/**
 * @category 2018-2-5
 * @author Roger
 *
 */
@Mapper
public interface UserMapper extends BaseDao<UserTest>{
	@Select("SELECT * FROM user WHERE name = #{name}")
	UserTest findByName(@Param("name") String name);
}
