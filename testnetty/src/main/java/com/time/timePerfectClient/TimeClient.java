package com.time.timePerfectClient;

import com.time.TimeClientHandler;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;

/**
 * 解决服务器发来的字节流可能被错误拆分成多个片段
 * 时间客户端, 转码时间服务器返回的数
 *
 * @author hd
 * @date 2017/11/18
 */
public class TimeClient {
    public static void main(String[] args) throws Exception {
        // String host = args[0];
        String host = "localhost";
        // int port = Integer.parseInt(args[1]);
        int port = 8080;
        EventLoopGroup workerGroup = new NioEventLoopGroup();
        try {
            Bootstrap b = new Bootstrap(); // (1)
            b.group(workerGroup); // (2)
            b.channel(NioSocketChannel.class); // (3)
            b.option(ChannelOption.SO_KEEPALIVE, true); // (4)
            b.handler(new ChannelInitializer<SocketChannel>() {
                @Override
                public void initChannel(SocketChannel ch) throws Exception {
                    //添加两个处理器 先按策略切分字节流为片段,在转换数据
                    ch.pipeline().addLast(new TimeDecoder(), new TimeClientHandler());
                }
            });

            // Start the client.
            ChannelFuture f = b.connect(host, port).sync(); // (5)

            // Wait until the connection is closed.
            f.channel().closeFuture().sync();
        } finally {
            workerGroup.shutdownGracefully();
        }
    }
}