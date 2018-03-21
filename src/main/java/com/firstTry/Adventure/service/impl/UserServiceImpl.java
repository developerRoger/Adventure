package com.firstTry.Adventure.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import com.firstTry.Adventure.entity.UserTest;
import com.firstTry.Adventure.mapper.UserMapper;
import com.firstTry.Adventure.service.UserService;
/**
 * 
 * @author Roger
 *
 */
@Service
public class UserServiceImpl implements UserService{
    @Autowired
    private JdbcTemplate jdbcTemplate;
	
    @Autowired
    private UserMapper userMapper;
    
	@Override
	public void create(UserTest userTest) {
		jdbcTemplate.update("insert into user(id, name,mobie,adders,password,remark) values(?,?,?,?,?,?)", userTest.getId(), userTest.getName(),
				userTest.getMobie(),userTest.getAdders(),userTest.getPassword(),userTest.getRemark());
		
	}
	@Override
	public void testJdbc() {
	}
	/**
	 * 
	 */
	@Override
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
		UserTest user=userMapper.findByName(username);
		//手动加密
		user.setPassword(new BCryptPasswordEncoder().encode(user.getPassword()));
		return user;
	}

}
