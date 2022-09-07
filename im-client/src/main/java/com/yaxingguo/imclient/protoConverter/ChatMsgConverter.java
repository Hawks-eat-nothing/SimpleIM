package com.yaxingguo.imclient.protoConverter;

import com.sun.org.apache.bcel.internal.generic.NEW;
import com.yaxingguo.imclient.session.ClientSession;
import com.yaxingguo.imcommon.bean.ChatMsg;
import com.yaxingguo.imcommon.bean.Msg.ProtoMsg;
import com.yaxingguo.imcommon.bean.User;

public class ChatMsgConverter extends BaseConverter{
    private User user;
    private ChatMsg chatMsg;
    public ChatMsgConverter(ClientSession session) {
        super(ProtoMsg.HeadType.MESSAGE_REQUEST, session);
    }

    public ProtoMsg.Message build(ChatMsg chatMsg, User user) {

        this.chatMsg = chatMsg;
        this.user = user;
        ProtoMsg.Message.Builder outerBuilder = getOuterBuilder(-1);
        ProtoMsg.MessageRequest.Builder cb = ProtoMsg.MessageRequest.newBuilder();
        //填充字段
        this.chatMsg.fillMsg(cb);
        ProtoMsg.Message requestMsg = outerBuilder.setMessageRequest(cb).build();
        return requestMsg;
    }

    public static ProtoMsg.Message build(
            ChatMsg chatMsg,
            User user1,
            ClientSession session){
        ChatMsgConverter chatMsgConverter = new ChatMsgConverter(session);
        return chatMsgConverter.build(chatMsg,user1);
    }



}
