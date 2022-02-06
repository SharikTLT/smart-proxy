package ru.shariktlt.proxy.iterators;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.handler.codec.http.FullHttpRequest;

public interface ProxyIterator {

    void init(Channel channel, Bootstrap remoteBootstrap, FullHttpRequest request);
}
