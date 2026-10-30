package com.firstTry.Adventure.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.firstTry.Adventure.mapper.IpBlacklistMapper;
import com.firstTry.Adventure.service.IpBlacklistService;

/**
 * IP黑名单
 *
 * @author Roger
 */
@Service("ipBlacklistService")
public class IpBlacklistServiceImpl implements IpBlacklistService {

	@Autowired
	private IpBlacklistMapper ipBlacklistMapper;

	@Override
	public boolean isBlocked(String ip) {
		if (ip == null || ip.trim().length() == 0) {
			return false;
		}
		return ipBlacklistMapper.existsByIp(ip.trim());
	}
}