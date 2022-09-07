package com.yaxingguo.imserver.processor;

import com.yaxingguo.imcommon.ProtoInstant;
import com.yaxingguo.imcommon.bean.Msg.ProtoMsg;
import com.yaxingguo.imcommon.bean.User;
import com.yaxingguo.imserver.protoConvertor.LoginResponceConverter;
import com.yaxingguo.imserver.session.ServerSession;
import com.yaxingguo.imserver.session.SessionMap;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

//处理异步业务逻辑，将处理结果写入用户绑定的子通道。

@Slf4j
@Service("LoginProcessor")
public class LoginProcessor implements ServerProcessor{

    @Autowired
    LoginResponceConverter loginResponceConverter;


    @Override
    public ProtoMsg.HeadType type() {
        return ProtoMsg.HeadType.LOGIN_REQUEST;
    }

    @Override
    public boolean action(ServerSession session, ProtoMsg.Message proto) {
        //取出token验证
        ProtoMsg.LoginRequest info = proto.getLoginRequest();
        long seqNo = proto.getSequence();

        User user = User.fromMsg(info);
        //检查用户
        boolean isValidUser = checkUser(user);
        if (!isValidUser){
            ProtoInstant.ResultCodeEnum resultCode = ProtoInstant.ResultCodeEnum.NO_TOKEN;
            //构造登录失败的报文
            ProtoMsg.Message response = loginResponceConverter.build(resultCode,seqNo,"-1");
            //发送登录失败的报文
            session.writeAndFlush(response);
            return false;
        }
        session.setUser(user);
        //服务端session和传输channel绑定的核心代码
        session.reverseBind();
        //登录成功
        ProtoInstant.ResultCodeEnum resultCode = ProtoInstant.ResultCodeEnum.SUCCESS;
        //构造登录成功的报文
        ProtoMsg.Message response = loginResponceConverter.build(resultCode,seqNo,session.getSessionId());
        session.writeAndFlush(response);
        return true;
    }

    private boolean checkUser(User user) {
        if (SessionMap.inst().hasLogin(user)){
            return false;
        }
        return true;
    }
}
