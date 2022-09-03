package com.yaxingguo.imclient.handler;

import io.netty.channel.ChannelHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@ChannelHandler.Sharable
@Service("ChatMsgHandler")
public class ChatMsgHandler {
}
