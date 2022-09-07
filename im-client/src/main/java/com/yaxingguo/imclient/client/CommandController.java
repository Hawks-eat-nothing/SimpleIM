package com.yaxingguo.imclient.client;

import com.yaxingguo.concurrent.FutureTaskScheduler;
import com.yaxingguo.imclient.command.*;
import com.yaxingguo.imclient.sender.ChatSender;
import com.yaxingguo.imclient.sender.LoginSender;
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
import java.util.Map;
import java.util.Scanner;
import java.util.concurrent.TimeUnit;

@Slf4j
@Data
@Service("CommandController")
public class CommandController {

    /**
     * 负责收集用户在控制台输入的命令类型，根据相应的类型调用相应的命令处理器
     * 和收集相应的信息
     *
     * CommandController在完成聊天内容和目标用户信息的收集后，
     * 在自己的startOneChat()方法中调用chatSender发送器示例，
     * 将聊天消息组装成Protobuf数据包，通过客户端的通道发往服务器端
     */
    @Autowired
    ChatConsoleCommand chatConsoleCommand;
    @Autowired
    LoginConsoleCommand loginConsoleCommand;
    //登出命令收集类
    @Autowired
    LogoutConsoleCommand logoutConsoleCommand;
    @Autowired
    ClientCommandMenu clientCommandMenu;

    private Map<String, BaseCommand> commandMap;

    @Autowired
    private ChatSender chatSender;

    @Autowired
    private LoginSender loginSender;

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

    public void startConnectServer() {
        FutureTaskScheduler.add(() -> {
            chatNettyClient.setConnectedListener(connectedListener);
            chatNettyClient.doConnect();
        });
    }

    public void commandThreadRunning() {
        Thread.currentThread().setName("命令线程");
        while (true) {
            //建立连接
            while (connectFlag == false) {
                //开始连接
                startConnectServer();
                waitCommandThread();
            }
            //处理命令
            while (null != session) {
                Scanner scanner = new Scanner(System.in);
                clientCommandMenu.exec(scanner);
                String key = clientCommandMenu.getCommandInput();
                BaseCommand command = commandMap.get(key);

                if (null == command) {
                    System.err.println("无法识别[" + command + "]指令，请重新输入~");
                    continue;
                }

                switch (key) {
                    case ChatConsoleCommand.KEY:
                        command.exec(scanner);
                        startOneChat((ChatConsoleCommand) command);
                        break;
                    case LoginConsoleCommand.KEY:
                        command.exec(scanner);
                        startLogin((LoginConsoleCommand) command);
                        break;
                    case LogoutConsoleCommand.KEY:
                        command.exec(scanner);
                        startLogout(command);
                        break;
                    default:
                        continue;
                }

            }
        }

    }

    private void startOneChat(ChatConsoleCommand c) {
        //登录
        if (!isLogin()) {
            log.info("还没有登录，请先登录");
            return;
        }
        chatSender.setSession(session);
        chatSender.setUser(user);
        chatSender.sendChatMsg(c.getToUserId(), c.getMessage());
    }

    private void startLogin(LoginConsoleCommand command) {
        //登录
        if (!isConnectFlag()) {
            log.info("连接异常，请重新连接");
            return;
        }
        User user = new User();
        user.setUid(command.getUserName());
        user.setToken(command.getPassword());
        user.setDevId("1111");
        this.user = user;
        session.setUser(user);
        loginSender.setUser(user);
        loginSender.setSession(session);
        loginSender.sendLoginMsg();
    }

    private void startLogout(BaseCommand command) {
        //登出
        if (!isLogin()) {
            log.info("还没登录，请先登录");
            return;
        }
        //TODO 登出
    }

    private void waitCommandThread() {
        try {
            this.wait();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public boolean isLogin() {
        if (null == session) {
            log.info("session is null");
            return false;
        }
        return session.isLogin();
    }
}
