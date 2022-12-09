package me.code;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpRequestEncoder;
import io.netty.handler.codec.http.HttpResponseDecoder;

public class ProxyHandler extends SimpleChannelInboundHandler<FullHttpRequest> {

    private final ProxyServer server;

    public ProxyHandler(ProxyServer server) {
        this.server = server;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, FullHttpRequest request) throws Exception {
        var host = request.headers().get("destination").split(":");
        var address = host[0];
        var port = Integer.parseInt(host[1]);

        request.headers().remove("destination");
        var copy = request.copy();
        var originalChannel = ctx.channel();

        var bootstrap = new Bootstrap();
        try {
             bootstrap
                    .group(server.getWorkerGroup())
                    .channel(NioSocketChannel.class)
                    .handler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel socketChannel) throws Exception {
                            var pipeline = socketChannel.pipeline();

                            pipeline.addLast(new HttpRequestEncoder());
                            pipeline.addLast(new HttpResponseDecoder());
                            pipeline.addLast(new HttpObjectAggregator(10024));
                            pipeline.addLast(new ClientHandler(originalChannel, copy));
                        }
                    })
                    .connect(address, port);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}
