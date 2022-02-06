package ru.shariktlt.proxy.iterators;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelPipeline;
import io.netty.handler.codec.http.*;
import ru.shariktlt.proxy.HTTPProxyBackendHandler;
import ru.shariktlt.proxy.ServerRecord;
import ru.shariktlt.proxy.ServersRegistry;

public abstract class AbstractProxyIterator implements ProxyIterator {

    protected ServersRegistry registry;

    protected Channel inboundChannel;

    protected Bootstrap remoteBootstrap;

    protected FullHttpRequest originalRequest;

    protected ServerRecord current;

    public AbstractProxyIterator(ServersRegistry registry) {
        this.registry = registry;
    }

    @Override
    public void init(Channel channel, Bootstrap bootstrap, FullHttpRequest request) {
        inboundChannel = channel;
        remoteBootstrap = bootstrap;
        originalRequest = request;

        iterate();
    }

    protected abstract ServerRecord next();

    private void iterate() {
        current = next();
        if (current == null) {
            if (inboundChannel.isActive()) {
                HttpResponse resp = new DefaultHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.NOT_FOUND);
                resp.headers().set("X-Smart-Proxy-Error", "route not found");
                resp.headers().set("X-Smart-Proxy-ErrorCode", 404);
                sendResponseToInbound(resp);
            }
        }

        ChannelFuture channelFuture = remoteBootstrap.connect(current.getHost(), current.getPort());
        Channel channel = channelFuture.channel();
        ChannelPipeline pipeline = channel.pipeline();
        pipeline.addLast(new HttpRequestEncoder(),
                new HTTPProxyBackendHandler(this::callback)
        );

        channelFuture.addListener(future -> {
            if (future.isSuccess()) {
                channel.read();
            } else {
                callback(null);
            }
        });
    }

    private void sendResponseToInbound(HttpResponse resp) {
        inboundChannel.read();
        inboundChannel.writeAndFlush(resp);
    }

    public void callback(HttpResponse response) {
        if (response == null) {
            iterate();
            return;
        }

        if (response.status().code() != 404) {
            registry.register(current, originalRequest.uri());
        }
        if (inboundChannel.isActive()) {
            sendResponseToInbound(response);
        }
    }
}
