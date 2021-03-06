package com.simulateHttp.simulatehttpclient;

import com.simulateHttp.simulatehttpclient.ByteBufToBytes;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.codec.http.*;

import static io.netty.handler.codec.http.HttpHeaders.Names.CONTENT_LENGTH;
import static io.netty.handler.codec.http.HttpHeaders.Names.CONTENT_TYPE;
import static io.netty.handler.codec.http.HttpResponseStatus.OK;
import static io.netty.handler.codec.http.HttpVersion.HTTP_1_1;
import static io.netty.handler.codec.rtsp.RtspHeaders.Names.CONNECTION;

/**
 * 供自定义的http客户端访问的
 * 服务端处理器
 *
 * @author HD
 * @date 2017/11/18
 */
public class CustomHttpChannelHandler extends ChannelInboundHandlerAdapter {
    private ByteBufToBytes reader;
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg)
            throws Exception {
        if (msg instanceof HttpRequest) {
            HttpRequest request = (HttpRequest) msg;
            System.out.println("messageType:"+ request.headers().get("messageType"));
            System.out.println("businessType:"+ request.headers().get("businessType"));
            if (HttpUtil.isContentLengthSet(request)) {
                reader = new ByteBufToBytes(
                        (int) HttpUtil.getContentLength(request));
            }
        }
        if (msg instanceof HttpContent) {
            HttpContent httpContent = (HttpContent) msg;
            ByteBuf content = httpContent.content();
            reader.reading(content);
            content.release();
            if (reader.isEnd()) {
                String resultStr = new String(reader.readFull());
                System.out.println("Client said:" + resultStr);
                FullHttpResponse response = new DefaultFullHttpResponse(
                        HTTP_1_1, OK, Unpooled.wrappedBuffer("I am ok"
                        .getBytes()));
                response.headers().set(CONTENT_TYPE, "text/plain");
                response.headers().set(CONTENT_LENGTH,
                        response.content().readableBytes());
                response.headers().set(CONNECTION, HttpHeaderValues.KEEP_ALIVE);
                ctx.write(response);
                ctx.flush();
            }
        }
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
        System.out.println("server channelReadComplete..");
        ctx.flush();//刷新后才将数据发出到SocketChannel
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause)
            throws Exception {
        System.out.println("server exceptionCaught..");
        ctx.close();
    }
}
