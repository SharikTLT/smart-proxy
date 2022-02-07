package ru.shariktlt.smart;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import ru.shariktlt.smart.proxy.Proxy;
import ru.shariktlt.smart.proxy.ServersRegistry;

import javax.imageio.spi.ServiceRegistry;

import static org.springframework.util.SocketUtils.findAvailableTcpPort;

@SpringBootApplication
public class SmartProxyApplication {

    @Value("${smartProxy.port:-1}")
    private int proxyPort;

    public static void main(String[] args) {
        SpringApplication.run(SmartProxyApplication.class, args);
    }

    @Bean
    @Qualifier("SmartProxyPort")
    public int smartProxyPortBean(){
        if(proxyPort < 0){
            proxyPort = findAvailableTcpPort();
        }
        return proxyPort;
    }

    @Bean
    public ServersRegistry serversRegistryBean(){
        return new ServersRegistry();
    }

    @Bean
    public Proxy proxyBean(ServersRegistry serversRegistry){
        Proxy proxy = new Proxy(smartProxyPortBean(), serversRegistry);
        new Thread(()-> proxy.init()).start();
        return proxy;
    }

}
