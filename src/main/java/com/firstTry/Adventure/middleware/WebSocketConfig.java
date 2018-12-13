package com.firstTry.Adventure.middleware;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.server.standard.ServerEndpointExporter;

/**
 * 开启WebSocket支持
 * @author Roger
 * @date 2018/11/9
 */
@Configuration  
public class WebSocketConfig {  
	
    @Bean  
    public ServerEndpointExporter serverEndpointExporter() {  
        return new ServerEndpointExporter();  
    }  
  
    /**
     * 这个实例化是为了能够在websocket的时候能够spring进行注入
     * @return
     */
    @Bean
    public SpringConfigurator newConfigure()
    {
        return new SpringConfigurator();
    }

} 

