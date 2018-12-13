package com.firstTry.Adventure.middleware;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import javax.websocket.server.ServerEndpointConfig;
/**
 * 解决websoket无法@Autowired注入的问题
 * @author Roger
 *这个类的核心就是getEndpointInstance(Class clazz)这个方法。 
 * 定义了获取类实例是通过ApplicationContext获取。
 * @date 2018/11/9
 */
public class SpringConfigurator  extends ServerEndpointConfig.Configurator implements ApplicationContextAware {

	 private static volatile BeanFactory context;
	 
	    @Override
	    public <T> T getEndpointInstance(Class<T> clazz) throws InstantiationException
	    {
	         return context.getBean(clazz);
	    }
	 
	    @Override
	    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException
	    {
	        System.out.println("auto load"+this.hashCode());
	        SpringConfigurator.context = applicationContext;
	    }


}
