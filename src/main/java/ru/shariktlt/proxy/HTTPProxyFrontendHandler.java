package ru.shariktlt.proxy;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpRequestEncoder;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.shariktlt.proxy.iterators.ProxyIterator;

public class HTTPProxyFrontendHandler extends SimpleChannelInboundHandler<FullHttpRequest> {

    private static final Logger LOGGER = LoggerFactory.getLogger(HTTPProxyFrontendHandler.class);

    private final ServersRegistry registry;

    private Channel outboundChannel;
    private Bootstrap remoteBootstrap;

    public HTTPProxyFrontendHandler(ServersRegistry registry) {
        this.registry = registry;
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) {
        final Channel inboundChannel = ctx.channel();

        // Start the connection attempt.
        remoteBootstrap = new Bootstrap();
        remoteBootstrap.group(inboundChannel.eventLoop())
                .channel(ctx.channel().getClass())
                .handler(new LoggingHandler(LogLevel.INFO))
                .option(ChannelOption.AUTO_READ, false);

//
//
//        ChannelFuture f = remoteBootstrap.connect(remoteHost, remotePort);
//        outboundChannel = f.channel();
//        outboundChannel.pipeline().addLast(
//                new HttpRequestEncoder(),
//                new HTTPProxyBackendHandler(inboundChannel)
//        );
        inboundChannel.read();

        /*
        f.addListener(new ChannelFutureListener() {
            @Override
            public void operationComplete(ChannelFuture future) {
                if (future.isSuccess()) {
                    // connection complete start to read first data
                    inboundChannel.read();
                } else {
                    // Close the connection if the connection attempt has failed.
                    inboundChannel.close();
                }
            }
        });
        */
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

    @Override
    public void channelRead0(final ChannelHandlerContext ctx, FullHttpRequest request) {
        if (outboundChannel.isActive()) {
            // FullHttpRequest copy = request.copy();
            request.retain();
//            request.headers().set("Host", "wikipedia.org");

            ProxyIterator proxyIterator = registry.getIteratorFor(request.uri());

            proxyIterator.init(ctx.channel(), remoteBootstrap, request);

            /*
            outboundChannel.writeAndFlush(request).addListener(new ChannelFutureListener() {
                @Override
                public void operationComplete(ChannelFuture future) {
                    if (future.isSuccess()) {
                        // was able to flush out data, start to read the next chunk
                        ctx.channel().read();
                    } else {
                        LOGGER.error(future.cause().getMessage(), future.cause());
                        future.channel().close();
                    }
                    //copy.release();
                }
            });
             */
        }
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) {
        if (outboundChannel != null) {
            closeOnFlush(outboundChannel);
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        cause.printStackTrace();
        closeOnFlush(ctx.channel());
    }

    static void closeOnFlush(Channel ch) {
        if (ch.isActive()) {
            ch.writeAndFlush(Unpooled.EMPTY_BUFFER).addListener(ChannelFutureListener.CLOSE);
        }
    }
}
