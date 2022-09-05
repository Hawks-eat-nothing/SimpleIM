package com.yaxingguo.imserver.handler;

import com.yaxingguo.concurrent.FutureTaskScheduler;
import com.yaxingguo.imcommon.bean.Msg.ProtoMsg;
import com.yaxingguo.imserver.session.ServerSession;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 主要工作如下：
 * 1. 对消息类型进行判断，：判断是否为聊天请求Protobuf数据包。如果不是，
 *    通过super.channelRead(ctx,msg)将消息交给流水线的下一站
 * 2. 对用户登录进行判断，如果没有登录则不能发送消息
 * 3. 开启异步的消息转发，由负责转发的chatRedirectProcessor实例完成消息转发
 *
 * @author Yaxing_Guo
 */
@Slf4j
@ChannelHandler.Sharable
@Service("ChatRedirectHandler")
public class ChatRedirectHandler extends ChannelInboundHandlerAdapter {
    @Autowired
    ChatRedirectProcessor chatRedirectProcessor;

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        //判断消息实例
        if (null == msg || !(msg instanceof ProtoMsg.Message)) {
            super.channelRead(ctx,msg);
            return;
        }
        //判断消息类型
        ProtoMsg.Message pkg = (ProtoMsg.Message) msg;
        ProtoMsg.HeadType headType = pkg.getType();
        if (!headType.equals(chatRedirectProcessor.type())){
            super.channelRead(ctx,msg);
            return;
        }
        //判断是否登录
        ServerSession session = ServerSession.getSession(ctx);
        if (null==session||!session.isLogin()){
            log.error("用户尚未登录，不能发送消息");
            return;
        }
        //异步处理IM消息转发的逻辑
        FutureTaskScheduler.add(()->{
            chatRedirectProcessor.action(session,pkg);
        });
    }
}
