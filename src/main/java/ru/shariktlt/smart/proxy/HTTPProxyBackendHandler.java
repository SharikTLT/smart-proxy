package ru.shariktlt.smart.proxy;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandler;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.FullHttpResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.function.Consumer;

public class HTTPProxyBackendHandler extends SimpleChannelInboundHandler<FullHttpResponse> {
    private static final Logger LOGGER = LoggerFactory.getLogger(HTTPProxyBackendHandler.class);

    private Consumer<FullHttpResponse> consumer;

    public HTTPProxyBackendHandler(Consumer<FullHttpResponse> consumer) {
        this.consumer = consumer;
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) {
        //ctx.pipeline().addLast(new HttpRequestEncoder());
        ctx.read();
    }

    @Override
    public void channelRead0(final ChannelHandlerContext ctx, FullHttpResponse msg) {
        consumer.accept(msg);
        ctx.channel().close();
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) {
        //consumer.accept(null);
        ctx.close();
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        LOGGER.error(cause.getMessage(), cause.getStackTrace());
        consumer.accept(null);
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
