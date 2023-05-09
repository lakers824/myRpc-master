package com.xlfc.provider;


import com.xlfc.common.annotion.RpcComponentScan;
import com.xlfc.remoting.transport.netty.server.NettyRpcServer;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;


public class AnnotationProvider {
    public static void main(String[] args) throws IOException {
        //指定配置类，配置bean
        new AnnotationConfigApplicationContext(ProviderComponentScan.class);
        //启动netty
        NettyRpcServer nettyRpcServer=new NettyRpcServer();
        nettyRpcServer.start();
    }

    //配置类，声明扫描的包
    @Configuration
    @RpcComponentScan(basePackages = {"com.xlfc.provider.impl"})
    static public class ProviderComponentScan{

    }
}
