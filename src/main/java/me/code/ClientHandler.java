package me.code;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpRequest;

public class ClientHandler extends SimpleChannelInboundHandler<FullHttpResponse> {

    private final Channel channel;
    private final HttpRequest request;

    public ClientHandler(Channel channel, HttpRequest request) {
        this.channel = channel;
        this.request = request;
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        ctx.channel().writeAndFlush(request);
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, FullHttpResponse response) throws Exception {
        channel.writeAndFlush(response.copy());
        ctx.channel().close();
    }
}