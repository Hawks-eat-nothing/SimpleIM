package com.yaxingguo.imclient.handler;

import com.yaxingguo.imclient.session.ClientSession;
import com.yaxingguo.imcommon.ProtoInstant;
import com.yaxingguo.imcommon.bean.Msg.ProtoMsg;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelPipeline;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@ChannelHandler.Sharable
@Service("LoginResponseHandler")
public class LoginResponseHandler extends ChannelInboundHandlerAdapter {
    /**
     * 登录响应的业务逻辑处理
     */
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        //判断消息示例
        if (null==msg||!(msg instanceof ProtoMsg.Message)){
            super.channelRead(ctx,msg);
            return;
        }
        //判断类型
        ProtoMsg.Message pkg = (ProtoMsg.Message) msg;
        ProtoMsg.HeadType headType = ((ProtoMsg.Message)msg).getType();
        if (!headType.equals(ProtoMsg.HeadType.LOGIN_RESPONSE)){
            super.channelRead(ctx,msg);
            return;
        }
        //判断返回是否成功
        ProtoMsg.LoginResponse info = pkg.getLoginResponse();
        ProtoInstant.ResultCodeEnum result = ProtoInstant.ResultCodeEnum.values()[info.getCode()];
        if (!result.equals(ProtoInstant.ResultCodeEnum.SUCCESS)){
            //登录失败
            log.info(result.getDesc());
        }else {
            //登录成功
            ClientSession.loginSuccess(ctx,pkg);
            ChannelPipeline p = ctx.pipeline();
            //移除登录响应器
            p.remove(this);
            //在编码器后面动态插入心跳处理器
            p.addAfter("encoder","heartbeat",new HeartBeatClientHandler());

        }
    }
}
