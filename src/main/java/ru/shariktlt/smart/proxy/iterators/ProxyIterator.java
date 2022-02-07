package ru.shariktlt.smart.proxy.iterators;

import io.netty.channel.Channel;
import io.netty.handler.codec.http.FullHttpRequest;

public interface ProxyIterator {

    void init(Channel channel, FullHttpRequest request);
}
