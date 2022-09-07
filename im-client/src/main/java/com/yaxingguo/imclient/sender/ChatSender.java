package com.yaxingguo.imclient.sender;

import com.yaxingguo.imclient.protoConverter.ChatMsgConverter;
import com.yaxingguo.imcommon.bean.ChatMsg;
import com.yaxingguo.imcommon.bean.Msg.ProtoMsg;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service("ChatSender")
public class ChatSender extends BaseSender{
    public void sendChatMsg(String touid,String content){
        log.info("发送消息 startConnectServer");
        ChatMsg chatMsg = new ChatMsg(getUser());
        chatMsg.setContent(content);
        chatMsg.setMsgType(ChatMsg.MSGTYPE.TEXT);
        chatMsg.setTo(touid);
        chatMsg.setMsgId(System.currentTimeMillis());
        ProtoMsg.Message message =
                ChatMsgConverter.build(chatMsg, getUser(), getSession());

        super.sendMsg(message);
    }

    @Override
    protected void sendSucceed(ProtoMsg.Message message) {
        log.info("发送成功:" + message.getMessageRequest().getContent());
    }
    @Override
    protected void sendFailed(ProtoMsg.Message message) {
        log.info("发送失败:" + message.getMessageRequest().getContent());
    }
}
