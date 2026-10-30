package com.firstTry.Adventure.service;

/**
 * IP黑名单
 *
 * @author Roger
 */
public interface IpBlacklistService {

	/**
	 * 判断指定IP是否被限制加入房间
	 *
	 * @param ip IP字符串
	 * @return true表示被限制
	 */
	boolean isBlocked(String ip);
}