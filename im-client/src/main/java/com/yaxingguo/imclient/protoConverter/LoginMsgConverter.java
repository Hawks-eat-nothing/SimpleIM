package com.yaxingguo.imclient.protoConverter;

import com.yaxingguo.imclient.session.ClientSession;
import com.yaxingguo.imcommon.bean.Msg.ProtoMsg;
import com.yaxingguo.imcommon.bean.User;
import org.omg.PortableInterceptor.SUCCESSFUL;

//将User类组装成ProtoBuf登录请求数据包
public class LoginMsgConverter extends BaseConverter{
    private final User user;

    public LoginMsgConverter(User user, ClientSession session) {
        super(ProtoMsg.HeadType.LOGIN_REQUEST,session);
        this.user = user;
    }
    public ProtoMsg.Message build(){
        ProtoMsg.Message.Builder outerBuilder = getOuterBuilder(-1);
        ProtoMsg.LoginRequest.Builder lb =
                ProtoMsg.LoginRequest.newBuilder()
                        .setDeviceId(user.getDevId())
                        .setPlatform(user.getPlatform().ordinal())
                        .setToken(user.getToken())
                        .setUid(user.getUid());

        ProtoMsg.Message requestMsg = outerBuilder.setLoginRequest(lb).build();
        return requestMsg;
    }
    public static ProtoMsg.Message build(User user,ClientSession session){
        LoginMsgConverter converter = new LoginMsgConverter(user,session);
        return converter.build();
    }
}
