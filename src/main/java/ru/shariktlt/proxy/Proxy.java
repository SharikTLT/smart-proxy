package ru.shariktlt.proxy;

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
    private final static Logger LOGGER = LoggerFactory.getLogger(ru.shariktlt.example.Proxy.class);

    private final int serverPort;

    private ServersRegistry registry = new ServersRegistry();

    public Proxy(int serverPort) {
        this.serverPort = serverPort;
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
            ServerBootstrap b = new ServerBootstrap();
            b.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .handler(new LoggingHandler(LogLevel.INFO))
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
