package com.yaxingguo.imserver.server;

import com.yaxingguo.imcommon.codec.ProtobufDecoder;
import com.yaxingguo.imcommon.codec.ProtobufEncoder;
import com.yaxingguo.imserver.handler.LoginRequestHandler;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.InetSocketAddress;

@Slf4j
@Data
@Service("ChatServer")
public class ChatServer {
    @Value("${server.port}")
    private int port;
    //通过NIO的方式接收连接和处理连接
    private EventLoopGroup bg;
    private EventLoopGroup wg;

    private ServerBootstrap b = new ServerBootstrap();
    @Autowired
    private LoginRequestHandler loginRequestHandler;

    @Autowired
    private ServerExceptionHandler serverExceptionHandler;

    @Autowired
    private ChatRedirectHandler chatRedirectHandler;

    public void run(){
        try {
            b.group(bg,wg)
                    .channel(NioServerSocketChannel.class)
                    .localAddress(new InetSocketAddress(port))
                    .option(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT)
                    .childOption(ChannelOption.ALLOCATOR,PooledByteBufAllocator.DEFAULT)
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        //有连接到达时会创建一个channel
                        @Override
                        protected void initChannel(SocketChannel socketChannel) throws Exception {
                            //管理pipeline里的handler
                            socketChannel.pipeline().addLast(new ProtobufDecoder())
                                    .addLast(new ProtobufEncoder())
                                    .addLast("login",loginRequestHandler)
                                    .addLast(serverExceptionHandler);

                        }
                    });
            //绑定server
            ChannelFuture channelFuture = b.bind().sync();
            log.info("SimpleIM服务启动，端口:"+channelFuture.channel().localAddress());
            //监听通道关闭事件
            ChannelFuture closeFuture = channelFuture.channel().closeFuture();
            closeFuture.sync();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }finally {
            wg.shutdownGracefully();
            bg.shutdownGracefully();
        }
    }
}
