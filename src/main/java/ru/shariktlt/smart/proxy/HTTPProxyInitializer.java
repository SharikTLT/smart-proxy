package ru.shariktlt.smart.proxy;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpRequestDecoder;
import io.netty.handler.codec.http.HttpResponseEncoder;
import io.netty.handler.stream.ChunkedWriteHandler;

public class HTTPProxyInitializer extends ChannelInitializer<SocketChannel> {

    private ServersRegistry registry;

    public HTTPProxyInitializer(ServersRegistry registry) {
        this.registry = registry;
    }

    @Override
    public void initChannel(SocketChannel ch) {
        ch.pipeline().addLast(
                new ChunkedWriteHandler(),
                new HttpRequestDecoder(),
                new HttpObjectAggregator(1024*1024),
                new HTTPProxyFrontendHandler(registry),
                new HttpResponseEncoder()
                );
    }
}
