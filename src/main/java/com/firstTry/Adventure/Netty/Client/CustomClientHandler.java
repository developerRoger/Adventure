package com.firstTry.Adventure.Netty.Client;

import com.firstTry.Adventure.Netty.Agreement.CustomMsg;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

public class CustomClientHandler extends ChannelInboundHandlerAdapter {  
    
    @Override  
    public void channelActive(ChannelHandlerContext ctx) throws Exception {  
        CustomMsg customMsg = new CustomMsg((byte)0xAB, (byte)0xCD, "Hello,Netty".length(), "Hello,Netty");  
        ctx.writeAndFlush(customMsg);  
    }  
}