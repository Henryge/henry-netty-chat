package com.henry.netty.time.client;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.serialization.ClassResolvers;
import io.netty.handler.codec.serialization.ObjectDecoder;
import io.netty.handler.codec.serialization.ObjectEncoder;

import java.io.BufferedReader;
import java.io.InputStreamReader;

public class TimeClient {

    public void writeMessage() throws Exception {
        String host = "localhost";
        int port = 8080;
        ChannelFuture f = null;

        EventLoopGroup workerGroup = new NioEventLoopGroup();
        try {
            Bootstrap b = new Bootstrap();
            b.group(workerGroup).channel(NioSocketChannel.class).option(ChannelOption.SO_KEEPALIVE, true)
                    .handler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel ch) throws Exception {
//                            ch.pipeline().addLast(new LengthFieldBasedFrameDecoder(Integer.MAX_VALUE, 0, 4, 0, 4));
//                            ch.pipeline().addLast(new LineBasedFrameDecoder(2048));
                            ch.pipeline().addLast(new ObjectDecoder(1024*1024, ClassResolvers.cacheDisabled(this.getClass().getClassLoader())));
                            ch.pipeline().addLast(new ObjectEncoder());
                            ch.pipeline().addLast(new TimeClientHandler());
                        }
                    });

            f = b.connect(host, port).sync();
            BufferedReader input = new BufferedReader(new InputStreamReader(System.in));

            while (true){
                f.channel().writeAndFlush(new Message(input.readLine()));
            }
        } finally {
            workerGroup.spliterator();
        }
    }

    public static void main(String[] args) throws Exception {
        new TimeClient().writeMessage();
    }

}
