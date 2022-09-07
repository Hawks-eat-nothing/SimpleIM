package com.yaxingguo.imclient.sender;

import com.yaxingguo.imclient.protoConverter.LoginMsgConverter;
import com.yaxingguo.imcommon.bean.Msg.ProtoMsg;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

//组装ProtoBuf数据包,将组装好的ProtoBuf登录数据包发送到服务器端
@Slf4j
@Service("LoginSender")
public class LoginSender extends BaseSender{
    public void sendLoginMsg() {
        if (!isConnected()) { log.info("还没有建立连接!");return; }
        log.info("构造登录消息");
        ProtoMsg.Message message =
                LoginMsgConverter.build(getUser(), getSession());
        log.info("发送登录消息");
        super.sendMsg(message);
    }
}
