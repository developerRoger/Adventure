package com.firstTry.Adventure;

import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.boot.web.support.SpringBootServletInitializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.web.client.RestTemplate;

import springfox.documentation.swagger2.annotations.EnableSwagger2;
/**
 * 扫描不到手动配置进行扫描
 * @author 	Roger	
 *
 */
@EnableAsync//线程异步使用注解
@EnableTransactionManagement
@EnableScheduling//定时任务发现注解
@ComponentScan(value={"com.firstTry.Adventure.*"})
@MapperScan("com.firstTry.adventure.mapper")
@EnableSwagger2
/*//此方法为关闭数据库连接或为禁止数据库链接
@EnableAutoConfiguration(exclude={DataSourceAutoConfiguration.class})//在测试的时候不连接数据库
*/@SpringBootApplication
public class AdventureApplication extends SpringBootServletInitializer{
	 @Autowired
	    private RestTemplateBuilder builder;
	    @Bean
	    public RestTemplate restTemplate() {
	        return builder.build();
	    }
	
	/**
	 * 线程池配置
	 * @return
	 */
	@Bean
	public Executor defaultThreadPool() {
		ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
		executor.setCorePoolSize(20);	//基本线程数量
		executor.setMaxPoolSize(20);	//最大线程数量
		executor.setQueueCapacity(200);  //队列最大长度
		executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
		executor.setThreadNamePrefix("AdventureExecutor-");//打印线程数量
		executor.setKeepAliveSeconds(30); //允许空闲时间
		executor.initialize();
		return executor;
	}
	   /**
	    * 密码采用了BCryptPasswordEncoder进行加密
	    * @return
	    */
	    @Bean  
	    public BCryptPasswordEncoder bCryptPasswordEncoder() {  
	        return new BCryptPasswordEncoder();  
	    }  
	/**
	 * 启动方法
	 * @param args
	 */
	public static void main(String[] args) {
		 new SpringApplicationBuilder(AdventureApplication.class).web(true).run(args);
	}
	
	 @Override
	    protected SpringApplicationBuilder configure(SpringApplicationBuilder application) {
	        return application.sources(AdventureApplication.class);
	    }
}
