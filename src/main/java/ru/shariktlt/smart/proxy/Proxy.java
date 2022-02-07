package ru.shariktlt.smart.proxy;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;

public class Proxy {
    private final static Logger LOGGER = LoggerFactory.getLogger(Proxy.class);

    private final int serverPort;

    private ServersRegistry registry;

    public Proxy(int port, ServersRegistry serversRegistry) {
        serverPort = port;
        registry = serversRegistry;
    }

    public void register(String... urls){
        Arrays.stream(urls)
                .map(ServerRecord::new)
                .forEach(registry::register);
    }

    public void init() {
        EventLoopGroup bossGroup = new NioEventLoopGroup(1);
        EventLoopGroup workerGroup = new NioEventLoopGroup();
        try {
            LOGGER.info("Start SmartProxy at port: {}", serverPort);
            ServerBootstrap b = new ServerBootstrap();
            b.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .handler(new LoggingHandler(LogLevel.DEBUG))
                    .childHandler(new HTTPProxyInitializer(registry))
                    .childOption(ChannelOption.AUTO_READ, false)
                    .bind(serverPort).sync().channel().closeFuture().sync();
        } catch (InterruptedException e) {
            LOGGER.error(e.getMessage(), e);
        } finally {
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }
    }
}
