package com.yaxingguo.imclient.handler;

import com.yaxingguo.imclient.protoConverter.HeartBeatMsgConverter;
import com.yaxingguo.imclient.session.ClientSession;
import com.yaxingguo.imcommon.bean.Msg.ProtoMsg;
import com.yaxingguo.imcommon.bean.User;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Slf4j
@ChannelHandler.Sharable
@Service("HeartBeatClientHandler")
public class HeartBeatClientHandler extends ChannelInboundHandlerAdapter {
    //心跳的时间间隔，单位为秒
    private static final int HEARTBEAT_INTERVAL = 50;

    //在Handler被加入到pipeline时，开始发送心跳


    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        ClientSession session = ClientSession.getSession(ctx);
        User user = session.getUser();
        HeartBeatMsgConverter builder = new HeartBeatMsgConverter(user, session);
        ProtoMsg.Message message = builder.build();
        //发送心跳
        heartBeat(ctx,message);
    }
    //使用定时器，发送心跳报文
    private void heartBeat(ChannelHandlerContext ctx, ProtoMsg.Message message) {
        ctx.executor().schedule(()->{
            if (ctx.channel().isActive()){
                log.info("发送HEART_BEAT消息给 server");
                ctx.writeAndFlush(message);
                heartBeat(ctx,message);
            }
        }, HEARTBEAT_INTERVAL, TimeUnit.SECONDS);
    }
    /**
     * 接收到服务器的心跳回写
     */
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        //判断消息实例
        if (null==msg||!(msg instanceof ProtoMsg.Message)){
            super.channelRead(ctx,msg);
            return;
        }
        //判断消息类型
        ProtoMsg.Message pkg = (ProtoMsg.Message) msg;
        ProtoMsg.HeadType headType = pkg.getType();
        if (headType.equals(ProtoMsg.HeadType.HEART_BEAT)){
            log.info("收到回写的HEART_BEAT消息 from server");
            return;
        }else {
            super.channelRead(ctx,msg);
        }
    }
}
