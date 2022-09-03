package com.yaxingguo.imserver.processor;

import com.yaxingguo.imcommon.bean.Msg.ProtoMsg;
import com.yaxingguo.imserver.session.ServerSession;

public interface ServerProcessor {
    ProtoMsg.HeadType type();
    boolean action(ServerSession session, ProtoMsg.Message proto);
}
