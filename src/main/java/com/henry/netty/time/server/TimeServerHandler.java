package com.henry.netty.time.server;

import com.henry.netty.time.client.Message;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.util.concurrent.GlobalEventExecutor;

public class TimeServerHandler extends SimpleChannelInboundHandler<Message> {

    public static ChannelGroup channels = new DefaultChannelGroup(GlobalEventExecutor.INSTANCE);

    protected TimeServerHandler() {
        super();
    }

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
    protected void channelRead0(ChannelHandlerContext ctx, Message msg) throws Exception {
        System.out.println("received message in server: " + msg.getMsg());
        for(Channel channel:channels) {
            if(channel != ctx.channel()) {
                channel.writeAndFlush(new Message(ctx.channel().remoteAddress() + "说:" + msg.getMsg()));
            }
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
        ctx.close();
    }
}
