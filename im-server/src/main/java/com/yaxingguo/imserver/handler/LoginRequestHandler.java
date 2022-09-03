package com.yaxingguo.imserver.handler;

import com.yaxingguo.concurrent.CallbackTask;
import com.yaxingguo.concurrent.CallbackTaskScheduler;
import com.yaxingguo.imcommon.bean.Msg.ProtoMsg;
import com.yaxingguo.imserver.processor.LoginProcessor;
import com.yaxingguo.imserver.session.ServerSession;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Slf4j
@ChannelHandler.Sharable
@Service("LoginRequestHandler")
public class LoginRequestHandler extends ChannelInboundHandlerAdapter {
    @Autowired
    LoginProcessor loginProcessor;

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        if (null==msg||!(msg instanceof ProtoMsg.Message)){
            super.channelRead(ctx,msg);
            return;
        }

        ProtoMsg.Message pkg = (ProtoMsg.Message) msg;
        //获得请求类型
        ProtoMsg.HeadType headType = pkg.getType();
        if (!headType.equals(loginProcessor.type())){
            super.channelRead(ctx,msg);
            return;
        }
        ServerSession session = new ServerSession(ctx.channel());
        //异步任务，处理登录的逻辑
        CallbackTaskScheduler.add(new CallbackTask<Boolean>(){
            @Override
            public Boolean execute() throws Exception {
                boolean r = loginProcessor.action(session, pkg);
                return r;
            }
            //异步任务返回
            @Override
            public void onBack(Boolean r) {
                if (r) {

                    ctx.pipeline().addAfter("login", "chat",   chatRedirectHandler);
                    ctx.pipeline().addAfter("login", "heartBeat",new HeartBeatServerHandler());

                    ctx.pipeline().remove("login");
                    log.info("登录成功:" + session.getUser());

                } else {
                    ServerSession.closeSession(ctx);
                    log.info("登录失败:" + session.getUser());

                }

            }
            //异步任务异常

            @Override
            public void onException(Throwable t) {
                ServerSession.closeSession(ctx);
                log.info("登录失败:" + session.getUser());

            }
        });
    }
}
