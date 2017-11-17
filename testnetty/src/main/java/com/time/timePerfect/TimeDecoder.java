package com.time.timePerfect;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;

import java.util.List;

/**
 * 对字节流进行策略切分片段的处理器
 *
 * @author HD
 * @date 2017/11/18
 */
public class TimeDecoder extends ByteToMessageDecoder { // (1)
    //4个字节为一个片段
    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) { // (2)
        if (in.readableBytes() < 4) {
            return; // (3)
        }
        //封装成实体类返回
        out.add(new UnixTime(in.readUnsignedInt())); // (4)
    }
}
