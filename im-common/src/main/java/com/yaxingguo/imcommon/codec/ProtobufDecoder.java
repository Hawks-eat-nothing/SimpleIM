package com.yaxingguo.imcommon.codec;

import com.google.protobuf.InvalidProtocolBufferException;
import com.yaxingguo.imcommon.ProtoInstant;
import com.yaxingguo.imcommon.bean.Msg.ProtoMsg;
import com.yaxingguo.util.Logger;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
@Slf4j
public class ProtobufDecoder extends ByteToMessageDecoder {

    @Override
    protected void decode(ChannelHandlerContext channelHandlerContext, ByteBuf byteBuf, List<Object> list) throws Exception {
        Object outmsg = decode0(channelHandlerContext,byteBuf);
        if (outmsg!=null){
            list.add(outmsg);
        }
    }

    public static Object decode0(ChannelHandlerContext ctx, ByteBuf in) throws InvalidProtocolBufferException {
        //标记当前readIndex的位置
        in.markReaderIndex();
        //判断包头长度
        if (in.readableBytes()<8){
            //不够包头
            return null;
        }
        //读取魔数
        short magic = in.readShort();
        if (magic!= ProtoInstant.MAGIC_CODE){
            String error = "客户端口令不对:"+ctx.channel().remoteAddress();
            //异常连接，直接报错，关闭连接
            throw new InvalidFrameException(error);
        }
        //读取版本
        short version = in.readShort();
        if (version!=ProtoInstant.VERSION_CODE){
            String error = "协议版本不对:"+ctx.channel().remoteAddress();
            //异常连接，直接报错，关闭连接
            throw new InvalidFrameException(error);
        }
        //读取传送过来的消息长度
        int length = in.readInt();
        //长度如果小于0
        if (length<0){
            //非法数据，关闭连接
            ctx.close();
        }
        if (length>in.readableBytes()){
            //读到的消息体长度如果小于传送过来的消息长度
            //重置读取位置
            in.resetReaderIndex();
            return null;
        }
        Logger.cfo("decoder length="+in.readableBytes());

        byte[] array;
        if (in.hasArray()){
            array = new byte[length];
            in.readBytes(array,0,length);
        }else {
            array = new byte[length];
            in.readBytes(array,0,length);
        }
        //字节转换成对象
        ProtoMsg.Message outmsg = ProtoMsg.Message.parseFrom(array);
        return outmsg;

    }
}
