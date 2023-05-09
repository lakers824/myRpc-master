package com.xlfc.remoting.transport.netty.server;

import com.xlfc.remoting.transport.RpcServer;
import com.xlfc.serialization.RpcMessageDecoder;
import com.xlfc.serialization.RpcMessageEncoder;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.timeout.IdleStateHandler;
import io.netty.util.concurrent.DefaultEventExecutorGroup;
import io.netty.util.concurrent.DefaultThreadFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.util.concurrent.TimeUnit;


public class NettyRpcServer implements RpcServer {
    private static Logger log = LoggerFactory.getLogger(NettyRpcServer.class);

    public static final int PORT=3170;
    private Channel channel;

    @Override
    public void stop() {
        this.channel.close();
    }

    @Override
    public void start() throws UnknownHostException {
        String host = InetAddress.getLocalHost().getHostAddress();
        //一个线程加一个selcetor就是一个EventLoop，EventLoop表示循环监听处理事件
        //一个EventLoop可以绑定多个channel，管理多个channel的io操作，即依照pipline顺序调用handler进行处理
        EventLoopGroup bossGroup = new NioEventLoopGroup(1);
        EventLoopGroup workerGroup = new NioEventLoopGroup();

        DefaultEventExecutorGroup serviceHandlerGroup = new DefaultEventExecutorGroup(
                Runtime.getRuntime().availableProcessors() * 2,
                new DefaultThreadFactory("NettyServerWorker", true)
        );
        try {
            //Bootstrap-启动，组装netty组件，启动服务器
            ServerBootstrap b = new ServerBootstrap();
            b.group(bossGroup,workerGroup)
                    //channel实现，支持NIO,BIO，epoll。这里Netty对原生ServerSocketChannel进行封装
                    .channel(NioServerSocketChannel.class)
                    .option(ChannelOption.SO_BACKLOG,128)
                    .handler(new LoggingHandler(LogLevel.INFO))
                    //决定能执行哪些操作，处理EventLoopGroup的事件
                    //Channel代表和客户端进行数据读写的通道
                    //接收到ByteBuf后，由handler进行处理
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel ch) {
                            //pipline,流水线。表示多个handler（处理工序）的合并
                            //pipline负责发布事件，handler对自己感兴趣的事件进行处理
                            //每一道工序是一个handler，对应一个固定的EventLoop(线程)来进行处理，一个EventLoop可以绑定执行多个handler
                            ChannelPipeline p = ch.pipeline();
                            p.addLast(new IdleStateHandler(30, 0, 0, TimeUnit.SECONDS));
                            //Encoder将数据转换为ByteBuf，Decoder相反
                            p.addLast(new RpcMessageEncoder());
                            p.addLast(new RpcMessageDecoder());
                            p.addLast(serviceHandlerGroup, new NettyServerHandler());
                        }
                    });

            // 绑定端口，同步等待绑定成功,不成功无法向下执行
            ChannelFuture f = b.bind(getBindAddress(host)).sync();
            log.info("服务端已启动");
            channel=f.channel();
            //sync同步等待channel关闭，
            f.channel().closeFuture().sync();

        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
            serviceHandlerGroup.shutdownGracefully();
        }


    }

    public static InetSocketAddress getBindAddress(String host) {
        return new InetSocketAddress(host, PORT);
    }

}
