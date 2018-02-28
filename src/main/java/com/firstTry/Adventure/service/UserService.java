package com.firstTry.Adventure.service;

import org.springframework.security.core.userdetails.UserDetails;

import com.firstTry.Adventure.entity.UserTest;
/**
 * 
 * @author Roger
 *
 */
public interface UserService {
	/**
     * 新增一个用户
     * @param name
     * @param age
     */
    void create(UserTest userTest);
    /**
     * 测试
     */
    void testJdbc();
    /**
     * 根据名称查询用户
     * @param username
     * @return
     */
    UserDetails loadUserByUsername(String username);
}
