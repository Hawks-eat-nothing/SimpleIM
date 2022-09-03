package com.yaxingguo.imclient.sender;

import com.yaxingguo.imclient.session.ClientSession;
import com.yaxingguo.imcommon.bean.Msg.ProtoMsg;
import com.yaxingguo.imcommon.bean.User;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

@Data
@Slf4j
public abstract class BaseSender {
    private User user;
    private ClientSession session;

    public boolean isConnected(){
        if (null==session){
            log.info("session is null");
            return false;
        }
        return session.isConnected();
    }

    public boolean isLogin(){
        if (null==session){
            log.info("session is null");
            return false;
        }
        return session.isLogin();
    }
    public void sendMsg(ProtoMsg.Message message){
        if (null==getSession()||!isConnected()){
            log.info("连接还没成功");
            return;
        }
        Channel channel = getSession().getChannel();
        ChannelFuture f = channel.writeAndFlush(message);
        f.addListener(new GenericFutureListener<Future<? super Void>>() {
            @Override
            public void operationComplete(Future<? super Void> future) throws Exception {
                //回调
                if (future.isSuccess()){
                    sendSucceed(message);
                }else {
                    sendFailed(message);
                }
            }
        });
    }

    protected void sendFailed(ProtoMsg.Message message) {
        log.info("发送失败");
    }

    protected void sendSucceed(ProtoMsg.Message message) {
        log.info("发送成功");
    }

}
