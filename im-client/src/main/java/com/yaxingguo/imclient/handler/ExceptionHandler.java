package com.yaxingguo.imclient.handler;

import com.yaxingguo.imclient.client.CommandController;
import com.yaxingguo.imclient.session.ClientSession;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Slf4j
@ChannelHandler.Sharable
@Service("ExceptionHandler")
public class ExceptionHandler extends ChannelInboundHandlerAdapter {

    @Autowired
    private CommandController commandController;
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        if (cause instanceof InvalidFrameException){
            log.error(cause.getMessage());
            ClientSession.getSession(ctx).close();
        }else {
            //捕捉异常信息
            log.error(cause.getMessage());
            ctx.close();
            if (commandController==null){
                return;
            }
            //开始重连
            commandController.setConnectFlag(false);
            commandController.startConnectServer();
        }
    }
    public void channelReadComplete(ChannelHandlerContext ctx)throws Exception{
        ctx.flush();
    }
}
