package com.henry.netty.webSocket.server;

import com.henry.netty.time.client.Message;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.codec.http.websocketx.BinaryWebSocketFrame;
import io.netty.handler.codec.http.websocketx.ContinuationWebSocketFrame;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketServerProtocolHandler;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import io.netty.handler.ssl.SslHandler;
import io.netty.util.concurrent.GlobalEventExecutor;
import org.apache.log4j.Logger;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLEngine;
import java.io.FileInputStream;
import java.security.KeyStore;
import java.security.Security;

public class WebSocketServer {

    static Logger logger = Logger.getLogger(WebSocketServer.class);

    public static void main(String[] args) {
        EventLoopGroup bossGroup = new NioEventLoopGroup();
        EventLoopGroup workGroup = new NioEventLoopGroup();

        try{
            ServerBootstrap b = new ServerBootstrap();
            b.group(bossGroup, workGroup).channel(NioServerSocketChannel.class)
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel ch) throws Exception {
                            KeyManagerFactory keyManagerFactory = null;
                            KeyStore keyStore = KeyStore.getInstance("JKS");
                            keyStore.load(new FileInputStream("D:\\temp\\sChat.jks"), "sNetty".toCharArray());
                            String algorithm = Security.getProperty("ssl.KeyManagerFactory.algorithm");
                            if (algorithm == null) {
                                algorithm = "SunX509";
                            }
                            keyManagerFactory = KeyManagerFactory.getInstance(algorithm);
                            keyManagerFactory.init(keyStore,"sNetty".toCharArray());
                            SslContext sslContext = SslContextBuilder.forServer(keyManagerFactory).build();
                            SSLEngine engine = sslContext.newEngine(ch.alloc());

                            engine.setUseClientMode(false);

                            ch.pipeline().addFirst(new SslHandler(engine));
                            ch.pipeline().addLast(new HttpServerCodec());
                            ch.pipeline().addLast(new HttpObjectAggregator(65536));
                            ch.pipeline().addLast(new WebSocketServerProtocolHandler("/websocket"));
                            ch.pipeline().addLast(new TextFrameHandler());
                            ch.pipeline().addLast(new BinaryFrameHandler());
                            ch.pipeline().addLast(new ContinuationFrameHandler());
                        }
                    });
            Channel ch = b.bind(8888).sync().channel();
            logger.info("启动成功");
            ch.closeFuture().sync();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            bossGroup.shutdownGracefully();
            workGroup.shutdownGracefully();
        }
    }

    public static final class TextFrameHandler extends SimpleChannelInboundHandler<TextWebSocketFrame> {
        public static ChannelGroup channels = new DefaultChannelGroup(GlobalEventExecutor.INSTANCE);

        Logger logger = Logger.getLogger(TextFrameHandler.class);

        @Override
        public void channelActive(ChannelHandlerContext ctx) throws Exception {
            Channel inComing = ctx.channel();
            for(Channel channel:channels) {
                channel.writeAndFlush(new Message("欢迎" + inComing.remoteAddress() + "进入聊天室"));
            }
            channels.add(inComing);
        }

        @Override
        public void channelInactive(ChannelHandlerContext ctx) throws Exception {
            Channel outComing = ctx.channel();
            for(Channel channel:channels) {
                channel.writeAndFlush(new Message(outComing.remoteAddress() + "离开聊天室"));
            }
            channels.remove(outComing);
        }

        @Override
        protected void channelRead0(ChannelHandlerContext ctx, TextWebSocketFrame frame) throws Exception {
            logger.info("received message in server: " + frame.text());
            for(Channel channel:channels) {
                if(channel != ctx.channel()) {
                    channel.writeAndFlush(new TextWebSocketFrame(ctx.channel().remoteAddress() + "说:" + frame.text()));
                }
            }
        }

        @Override
        public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
            cause.printStackTrace();
            ctx.close();
        }
    }

    public static final class BinaryFrameHandler extends SimpleChannelInboundHandler<BinaryWebSocketFrame> {
        @Override
        public void channelRead0(ChannelHandlerContext ctx, BinaryWebSocketFrame msg) throws Exception {
            // Handle binary frame
        }
    }

    public static final class ContinuationFrameHandler extends SimpleChannelInboundHandler<ContinuationWebSocketFrame> {
        @Override
        public void channelRead0(ChannelHandlerContext ctx, ContinuationWebSocketFrame msg) throws Exception {
            // Handle continuation frame
        }
    }

}
