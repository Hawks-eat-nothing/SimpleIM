package com.yaxingguo.imclient.client;

import com.yaxingguo.imclient.handler.ChatMsgHandler;
import com.yaxingguo.imclient.handler.ExceptionHandler;
import com.yaxingguo.imclient.handler.LoginResponseHandler;
import com.yaxingguo.imcommon.codec.ProtobufDecoder;
import com.yaxingguo.imcommon.codec.ProtobufEncoder;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.util.concurrent.GenericFutureListener;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Slf4j
@Data
@Service("chatNettyClient")
public class ChatNettyClient {
    @Value("${chat.server.ip}")
    private String host;
    @Value("${chat.server.port}")
    private int port;
    private Bootstrap bootstrap;
    private EventLoopGroup workerGroup;

    @Autowired
    private LoginResponseHandler loginResponseHandler;

    private GenericFutureListener<ChannelFuture> connectedListener;

    @Autowired
    private ChatMsgHandler chatMsgHandler;


    public ChatNettyClient() {
        /**
         * 通过nio方式来接收连接和处理连接
         */
        workerGroup = new NioEventLoopGroup(1);
    }

    /**
     * 重连
     */
    public void doConnect(){
        try {
            bootstrap = new Bootstrap();
            bootstrap.group(workerGroup)
                    .channel(NioSocketChannel.class)
                    .option(ChannelOption.SO_KEEPALIVE,true)
                    .option(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT)
                    .remoteAddress(host,port)
                    .handler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel socketChannel) throws Exception {
                            socketChannel.pipeline().addLast("decoder",new ProtobufDecoder())
                                    .addLast("encoder",new ProtobufEncoder())
                                    .addLast(loginResponseHandler)
                                    .addLast(chatMsgHandler)
                                    .addLast("exception",new ExceptionHandler());
                        }
                    });
            log.info("客户端开始连接[SimpleIM]");
            ChannelFuture future = bootstrap.connect();
            future.addListener(connectedListener);
        }catch (Exception e){
            e.printStackTrace();
        }
    }
    public void close(){
        workerGroup.shutdownGracefully();
    }
}
