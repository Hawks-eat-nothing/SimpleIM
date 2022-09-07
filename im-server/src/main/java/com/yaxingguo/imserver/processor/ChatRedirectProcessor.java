package com.yaxingguo.imserver.processor;

import com.yaxingguo.imcommon.bean.Msg.ProtoMsg;
import com.yaxingguo.imserver.session.ServerSession;
import com.yaxingguo.imserver.session.SessionMap;
import com.yaxingguo.util.Logger;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 负责异步消息转发的ChatRedirectProcessor类，功能如下：
 * 1. 根据目标用户ID，找出所有的服务器端的会话列表
 * 2. 然后为每一个会话转发一份消息
 * @author Yaxing_GUo
 */

@Slf4j
@Service("ChatRedirectProcessor")
public class ChatRedirectProcessor implements ServerProcessor{
    @Override
    public ProtoMsg.HeadType type() {
        return ProtoMsg.HeadType.MESSAGE_REQUEST;
    }

    @Override
    public boolean action(ServerSession session, ProtoMsg.Message proto) {
        //聊天处理
        ProtoMsg.MessageRequest msg = proto.getMessageRequest();
        Logger.tcfo("chatMsg | from="
                + msg.getFrom()
                + " , to=" + msg.getTo()
                + " , content=" + msg.getContent());
        //获取接收方的chatID
        String to = msg.getTo();
        List<ServerSession> toSessions = SessionMap.inst().getSessionsBy(to);
        if (toSessions==null){
            //接收方离线
            // TODO 接收方离线应该可以发送离线消息哎
            Logger.tcfo("["+to+"]不在线，发送失败！");
        }else {
            toSessions.forEach((sessions)->{
                //将消息发送到接收方
                sessions.writeAndFlush(proto);
            });
        }
        return false;
    }
}
