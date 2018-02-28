package com.firstTry.Adventure;


import org.json.JSONObject;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.firstTry.Adventure.entity.UserTest;
import com.firstTry.Adventure.mapper.UserMapper;
import com.firstTry.Adventure.service.UserService;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest
public class AdventureApplicationTests {
	
	@Autowired
	private UserService userSerivce;
	
	@Autowired
	private RedisTemplate<String, Object> redisTemplate;//redis实现方法
	
	@Autowired
	private UserMapper userMapper;
	 /**
     * 数据源1
     */
//	@Autowired
//	@Qualifier("primaryJdbcTemplate")
//	protected JdbcTemplate jdbcTemplate1;
	 /**
     * 数据源2
     */
//	@Autowired
//	@Qualifier("secondaryJdbcTemplate")
//	protected JdbcTemplate jdbcTemplate2;
	
	
/*	@Test
	public void contextLoads() {
	}*/
	
//	@Test
	public void test() throws Exception {
		UserTest user=new UserTest();
		user.setId(1l);
		user.setName("测试");
		user.setMobie("1008611");
		user.setAdders("广州天河");
		user.setPassword("123456");
		user.setRemark("备注");
		// 插入个用户
		userSerivce.create(user);
	}
	/**
	 * JDBC多数据源配置测试
	 */
/*	@Test
	public void testJdbc() {
		*//**
		 * 测试
		 *//*
		jdbcTemplate1.update("insert into jdbcTest(id, name) values(1,'2')");
		*//**
		 * 测试
		 *//*
		jdbcTemplate2.update("insert into user(id, name,mobie,adders,password,remark) values(1,'2','3','4','5','6')");
	}*/
	
	/**
	 * 测试redis
	 */
//	@Test
	public void testRedis(){
		//
		redisTemplate.opsForValue().set("111", "redis");
		System.out.println(redisTemplate.opsForValue().get("111")+"????");
	}
	
	/**
	 * 跟mybatis整合
	 * 查询测试
	 */
	@Test
	public void testMybatis(){
		UserTest userTest=userMapper.findByName("2");
		System.out.println(userTest.getAdders()+"======");
	}
}
