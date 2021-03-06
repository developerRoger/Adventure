package com.firstTry.Adventure;


import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.firstTry.Adventure.config.IdProcessor;
import com.firstTry.Adventure.entity.UserTest;
import com.firstTry.Adventure.mapper.SysGeneratorMapper;
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
	private IdProcessor<Long> idProcessor;
	
	@Autowired
	private UserMapper userMapper;
	
	@Autowired
	private SysGeneratorMapper sysGeneratorService;
	
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
		user.setName("cahihaea");
		user.setMobie("1008611");
		user.setAdders("广州天河");
		user.setPassword("544");
		user.setRemark("ppop");
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
	 * @throws Exception 
	 */
	@Test
	public void testRedis(){
		//
		redisTemplate.opsForValue().set("111", "redis");
		System.out.println(redisTemplate.opsForValue().get("111")+"????");
		redisTemplate.delete("111");
		System.out.println(redisTemplate.opsForValue().get("111")+"????");
	}
	
	/**
	 * 测试id自动生成(雪花算法)
	 */
//	@Test
	public void testIdNext(){
		System.out.println(idProcessor.nextId());
	}
	
	/**
	 * 跟mybatis整合
	 * 查询测试
	 */
//	@Test
	public void testMybatis(){
		UserTest userTest=userMapper.findByName("2");
		System.out.println(userTest.getAdders()+"======");
	}
	
//	@Test
	public void TestService(){
		List<Map<String,Object>> listMap=sysGeneratorService.queryList(new HashMap<>());
		System.out.println(listMap);
	}
	
//	@Test
	public void TestFor(){
		System.out.println(TestForIndex());
	}
	
	private String TestForIndex(){
		
		for(int a=0 ;a<10;a++){
			if(a==5){
				return "---"+a;
			}
			System.out.println(a);
		}
		return "0";
	}
	
//	@Test
	public void TestBoolean(){
		boolean i=false;
		if(!i){
			System.out.println("true");
		}else{
			System.out.println("false");
		}
	}
}
