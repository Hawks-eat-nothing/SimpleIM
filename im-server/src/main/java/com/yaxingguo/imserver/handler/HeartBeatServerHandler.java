package com.yaxingguo.imserver.handler;

import com.yaxingguo.concurrent.FutureTaskScheduler;
import com.yaxingguo.imcommon.bean.Msg.ProtoMsg;
import com.yaxingguo.imserver.session.ServerSession;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.handler.timeout.IdleStateHandler;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.TimeUnit;
@Slf4j
public class HeartBeatServerHandler extends IdleStateHandler {
    private static final int READ_IDLE_GAP = 150;


    public HeartBeatServerHandler() {
        /**
         * 四个参数的含义：
         * 1. 表示入站空闲检测时长，指的是一段时间内如果没有数据入站，就判定连接假死
         * 2. 表示出站空闲检测时长，指的是一段时间内没有数据出站，就判定连接假死
         * 3. 表示出/入站检测时长，表示在一段时间内如果没有出站或者入站，就判定假死
         * 4. 表示时间单位
         */

        super(READ_IDLE_GAP,0,0, TimeUnit.SECONDS);
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        //判断消息实例
        if (null==msg||!(msg instanceof ProtoMsg.Message)){
            super.channelRead(ctx,msg);
            return;
        }

        ProtoMsg.Message pkg = (ProtoMsg.Message) msg;
        //判断消息类型
        ProtoMsg.HeadType headType = pkg.getType();
        if (headType.equals(ProtoMsg.HeadType.HEART_BEAT)){
            //异步处理，将心跳包直接回复给客户端
            FutureTaskScheduler.add(()->{
                if (ctx.channel().isActive()){
                    ctx.writeAndFlush(msg);
                }
            });
        }
        super.channelRead(ctx,msg);
    }

    @Override
    protected void channelIdle(ChannelHandlerContext ctx, IdleStateEvent evt) throws Exception {
        log.info(READ_IDLE_GAP+"秒内未读到数据，关闭连接");
        ServerSession.closeSession(ctx);
    }
}
