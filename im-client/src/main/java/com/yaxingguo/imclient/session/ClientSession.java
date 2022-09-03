package com.yaxingguo.imclient.session;

import ch.qos.logback.core.net.server.Client;
import com.yaxingguo.imcommon.bean.Msg.ProtoMsg;
import com.yaxingguo.imcommon.bean.User;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.util.AttributeKey;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Data
public class ClientSession {
    public static final AttributeKey<ClientSession> SESSION_KEY = AttributeKey.valueOf("SESSION_KEY");
    /**
     * 用户实现客户端会话管理的核心
     */
    private Channel channel;
    private User user;
    /**
     * 保存登录后的服务端sessionID
     */
    private String sessionId;
    private boolean isConnected = false;
    private boolean isLogin = false;

    //绑定通道
    public ClientSession(Channel channel){
        this.channel = channel;
        this.sessionId = String.valueOf(-1);
        channel.attr(ClientSession.SESSION_KEY).set(this);
    }
    //登录成功后设置sessionID
    public static void loginSuccess(ChannelHandlerContext ctx, ProtoMsg.Message pkg){
        Channel channel = ctx.channel();
        ClientSession session = channel.attr(ClientSession.SESSION_KEY).get();
        session.setSessionId(pkg.getSessionId());
       session.setLogin(true);
        log.info("登录成功");
    }
    //获取通道
    public static ClientSession getSession(ChannelHandlerContext ctx){
        Channel channel = ctx.channel();
        ClientSession session = channel.attr(ClientSession.SESSION_KEY).get();
        return session;
    }

    public String getRemoteAddress(){
        return channel.remoteAddress().toString();
    }

    //把protobuf数据包写入通道
    public ChannelFuture writeAndFlush(Object pkg){
        ChannelFuture fu = channel.writeAndFlush(pkg);
        return fu;

    }
    public void writeAndClose(Object pkg){
        ChannelFuture fu = channel.writeAndFlush(pkg);
        fu.addListener(ChannelFutureListener.CLOSE);
    }
    //关闭通道
    public void close(){
        isConnected = false;
        ChannelFuture future = channel.close();
        future.addListener(new ChannelFutureListener() {
            @Override
            public void operationComplete(ChannelFuture channelFuture) throws Exception {
                if (channelFuture.isSuccess()){
                    log.info("顺利断开连接");
                }
            }
        });
    }

}
