package ru.shariktlt.proxy;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpRequestDecoder;
import io.netty.handler.stream.ChunkedWriteHandler;

public class HTTPProxyInitializer extends ChannelInitializer<SocketChannel> {

    private ServersRegistry registry;

    public HTTPProxyInitializer(ServersRegistry registry) {
        this.registry = registry;
    }

    @Override
    public void initChannel(SocketChannel ch) {
        ch.pipeline().addLast(
                new HttpRequestDecoder(),
                new HttpObjectAggregator(65_536),
                new ChunkedWriteHandler(),
                new HTTPProxyFrontendHandler(registry));
    }
}
