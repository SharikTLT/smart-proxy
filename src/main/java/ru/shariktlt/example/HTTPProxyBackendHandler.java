package ru.shariktlt.example;

import io.netty.buffer.ByteBuf;
import io.netty.channel.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HTTPProxyBackendHandler extends SimpleChannelInboundHandler<ByteBuf> {
    private static final Logger LOGGER = LoggerFactory.getLogger(HTTPProxyBackendHandler.class);

    private final Channel inboundChannel;

    public HTTPProxyBackendHandler(Channel inboundChannel) {
        this.inboundChannel = inboundChannel;
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) {
        //ctx.pipeline().addLast(new HttpRequestEncoder());
        ctx.read();
    }

    @Override
    public void channelRead0(final ChannelHandlerContext ctx, ByteBuf msg) {
        inboundChannel.writeAndFlush(msg.copy()).addListener(new ChannelFutureListener() {
            @Override
            public void operationComplete(ChannelFuture future) {
                if (future.isSuccess()) {
                    ctx.channel().read();
                } else {
                    LOGGER.error(future.cause().getMessage(), future.cause());
                    future.channel().close();
                }
            }


        });
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) {
        HTTPProxyFrontendHandler.closeOnFlush(inboundChannel);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        cause.printStackTrace();
        HTTPProxyFrontendHandler.closeOnFlush(ctx.channel());
    }

    /**
     * Returns {@code true} if the given message should be handled. If {@code false} it will be passed to the next
     * {@link ChannelInboundHandler} in the {@link ChannelPipeline}.
     *
     * @param msg
     */
    @Override
    public boolean acceptInboundMessage(Object msg) throws Exception {
        return super.acceptInboundMessage(msg);
    }
}
