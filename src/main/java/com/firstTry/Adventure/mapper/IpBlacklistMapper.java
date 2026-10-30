package com.firstTry.Adventure.mapper;

import org.apache.ibatis.annotations.Mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.firstTry.Adventure.entity.IpBlacklistEntity;

/**
 * IP黑名单
 *
 * 使用MyBatis-Plus的BaseMapper + Wrapper链式写法，无需XML
 *
 * @author Roger
 */
@Mapper
public interface IpBlacklistMapper extends BaseMapper<IpBlacklistEntity> {

	/**
	 * 判断指定IP是否已在黑名单中
	 *
	 * @param ip IP字符串
	 * @return true表示被限制
	 */
	default boolean existsByIp(String ip) {
		return selectCount(Wrappers.<IpBlacklistEntity>lambdaQuery()
				.eq(IpBlacklistEntity::getIp, ip)) > 0;
	}
}