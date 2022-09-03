package com.yaxingguo.imclient.client;

import com.yaxingguo.imclient.command.BaseCommand;
import com.yaxingguo.imclient.command.LoginConsoleCommand;
import com.yaxingguo.imclient.session.ClientSession;
import com.yaxingguo.imcommon.bean.User;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.EventLoop;
import io.netty.util.concurrent.GenericFutureListener;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.Scanner;
import java.util.concurrent.TimeUnit;

@Slf4j
@Data
@Service("CommandController")
public class CommandController {
    private Channel channel;
    private boolean connectFlag = false;
    private User user;

    //会话类
    private ClientSession session;

    @Autowired
    private ChatNettyClient chatNettyClient;

    GenericFutureListener<ChannelFuture> closeFuture = (ChannelFuture f) -> {
        log.info(new Date() + "连接已断开...");
        channel = f.channel();
        //创建会话
        ClientSession session = channel.attr(ClientSession.SESSION_KEY).get();
        session.close();
        //唤醒用户线程
        notifyCommandThread();
    };

    private void notifyCommandThread() {
        //唤醒，命令收集线程
        this.notify();
    }


    GenericFutureListener<ChannelFuture> connectedListener = (ChannelFuture f) -> {
        final EventLoop eventLoop = f.channel().eventLoop();
        if (!f.isSuccess()) {
            log.info("连接失败，在10s后准备尝试重连");
            eventLoop.schedule(() -> chatNettyClient.doConnect(), 10, TimeUnit.SECONDS);
            connectFlag = false;
        } else {
            connectFlag = true;
            log.info("simple IM 服务器连接成功！");
            channel = f.channel();

            //创建会话
            session = new ClientSession(channel);
            channel.closeFuture().addListener(closeFuture);
        }
    };
}
