package ru.shariktlt.smart.proxy.iterators;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.handler.codec.http.*;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import io.netty.handler.ssl.util.InsecureTrustManagerFactory;
import lombok.SneakyThrows;
import ru.shariktlt.smart.proxy.HTTPProxyBackendHandler;
import ru.shariktlt.smart.proxy.ServerRecord;
import ru.shariktlt.smart.proxy.ServersRegistry;

import java.util.List;

import static io.netty.handler.codec.http.HttpResponseStatus.NOT_FOUND;

public class ListProxyIterator implements ProxyIterator {

    public static final String HEADER_PREFIX = "X-Smart-Proxy-";

    public static final String HEADER_ERROR = HEADER_PREFIX + "Error";
    public static final String HEADER_ERROR_CODE = HEADER_PREFIX + "ErrorCode";
    public static final String HEADER_PROVIDER = HEADER_PREFIX + "Provider";
    public static final String HEADER_CONSUME_302 = HEADER_PREFIX + "Consume-302";
    public static final String HEADER_CONSUME_301 = HEADER_PREFIX + "Consume-301";
    public static final String HEADER_CONTENT_LENGTH = "Content-Length";
    public static final int MAX_CONTENT_LENGTH = 1024 * 1024;


    private final ServersRegistry registry;

    private final List<ServerRecord> list;

    private Channel inboundChannel;

    private Bootstrap remoteBootstrap;

    private FullHttpRequest originalRequest;

    private ServerRecord current;

    private int pos = 0;

    public ListProxyIterator(ServersRegistry registryLink, List<ServerRecord> listExternal) {
        registry = registryLink;
        list = listExternal;
    }

    @Override
    public void init(Channel channel, FullHttpRequest request) {
        inboundChannel = channel;
        originalRequest = request;

        remoteBootstrap = new Bootstrap();
        remoteBootstrap.group(inboundChannel.eventLoop())
                .channel(inboundChannel.getClass())
                .handler(new LoggingHandler(LogLevel.DEBUG))
                .option(ChannelOption.AUTO_READ, false);

        iterate();
    }

    protected ServerRecord next() {
        if (pos < list.size()) {
            return list.get(pos++);
        }
        return null;
    }

    private void iterate() {
        current = next();
        if (current == null) {
            if (inboundChannel.isActive()) {
                FullHttpResponse resp = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, NOT_FOUND);
                resp.headers().set(HEADER_ERROR, NOT_FOUND.reasonPhrase());
                resp.headers().set(HEADER_ERROR_CODE, NOT_FOUND.code());
                resp.headers().set(HEADER_CONTENT_LENGTH, 0);
                sendResponseToInbound(resp);
            }
            return;
        }

        ChannelFuture channelFuture = remoteBootstrap.connect(current.getHost(), current.getPort());
        Channel channel = channelFuture.channel();
        ChannelPipeline pipeline = channel.pipeline();

        if(current.isHttps()){
            pipeline.addLast(getSslContext().newHandler(channel.alloc()));
        }

        pipeline.addLast(
                new HttpRequestEncoder(),
                new HttpResponseDecoder(),
                new HttpObjectAggregator(MAX_CONTENT_LENGTH),
                new HTTPProxyBackendHandler(this::callback)
        );

        FullHttpRequest backendRequest = originalRequest.copy();
        backendRequest.headers().set("Host", current.getHost());

        channelFuture.addListener(future -> {
            if (future.isSuccess()) {
                channel.read();
                channel.writeAndFlush(backendRequest).addListener(writeFuture -> {
                   if(!writeFuture.isSuccess()){
                       callback(null);
                   }
                });
            } else {
                callback(null);
            }
        });
    }

    @SneakyThrows
    private SslContext getSslContext() {
        return SslContextBuilder.forClient()
                .trustManager(InsecureTrustManagerFactory.INSTANCE)
                .build();
    }

    private void sendResponseToInbound(HttpResponse resp) {
        inboundChannel.read();
        inboundChannel.writeAndFlush(resp).addListener(future -> {
            if(future.isSuccess()){
                if(inboundChannel.isActive()){

                }
            }else{
                if(inboundChannel.isActive()){

                }
            }
        });
    }

    public void callback(FullHttpResponse response) {
        if (response == null
                || response.status().code() == 404
                || (!originalRequest.headers().contains(HEADER_CONSUME_302) && response.status().code() == 302)
                || (!originalRequest.headers().contains(HEADER_CONSUME_301) && response.status().code() == 301)
        ) {
            iterate();
            return;
        }
        if(response.status().code() == 200){
            registry.register(current, originalRequest.uri());
        }
        if (inboundChannel.isActive()) {
            FullHttpResponse clientResponse = response.copy();
            clientResponse.headers().set(HEADER_PROVIDER, current.toString());
            sendResponseToInbound(clientResponse);
        }
    }


}
