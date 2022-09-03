package com.yaxingguo.imserver.protoConvertor;

import com.yaxingguo.imcommon.ProtoInstant;
import com.yaxingguo.imcommon.bean.Msg.ProtoMsg;
import org.springframework.stereotype.Service;

@Service("LoginResponceBuilder")
public class LoginResponceConverter {
    /**
     * 登录应答 应答消息protobuf
     */
    public ProtoMsg.Message build(
            ProtoInstant.ResultCodeEnum en, long seqId, String sessionId) {

        ProtoMsg.Message.Builder outer = ProtoMsg.Message.newBuilder()
                .setType(ProtoMsg.HeadType.LOGIN_RESPONSE)  //设置消息类型
                .setSequence(seqId)
                .setSessionId(sessionId);  //设置应答流水，与请求对应

        ProtoMsg.LoginResponse.Builder b = ProtoMsg.LoginResponse.newBuilder()
                .setCode(en.getCode())
                .setInfo(en.getDesc())
                .setExpose(1);

        outer.setLoginResponse(b.build());
        return outer.build();
    }

}
