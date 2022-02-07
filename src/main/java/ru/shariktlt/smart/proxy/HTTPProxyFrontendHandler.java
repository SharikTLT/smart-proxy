package ru.shariktlt.smart.proxy;

import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.FullHttpRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HTTPProxyFrontendHandler extends SimpleChannelInboundHandler<FullHttpRequest> {

    private static final Logger LOGGER = LoggerFactory.getLogger(HTTPProxyFrontendHandler.class);

    private final ServersRegistry registry;

    public HTTPProxyFrontendHandler(ServersRegistry registry) {
        this.registry = registry;
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) {
        Channel inboundChannel = ctx.channel();



        inboundChannel.read();
    }


    @Override
    public boolean acceptInboundMessage(Object msg) throws Exception {
        return super.acceptInboundMessage(msg);
    }

    @Override
    public void channelRead0(final ChannelHandlerContext ctx, FullHttpRequest request) {
        registry.getIteratorFor(request.uri()).init(ctx.channel(), request.retain());
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        if(LOGGER.isDebugEnabled()){
            LOGGER.debug(cause.getMessage(), cause);
        }
        closeOnFlush(ctx.channel());
    }

    static void closeOnFlush(Channel ch) {
        if (ch.isActive()) {
            ch.writeAndFlush(Unpooled.EMPTY_BUFFER).addListener(ChannelFutureListener.CLOSE);
        }
    }
}
