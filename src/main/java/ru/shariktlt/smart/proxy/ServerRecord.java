package ru.shariktlt.smart.proxy;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.SneakyThrows;
import lombok.ToString;

import java.net.URI;
import java.net.UnknownHostException;

@Getter
@EqualsAndHashCode
public class ServerRecord {

    private boolean isHttps;

    private String host;

    private int port;

    @SneakyThrows
    public ServerRecord(String url) {
        URI uri = URI.create(url);
        String scheme = uri.getScheme();

        isHttps = "https".equals(uri.getScheme());
        host = uri.getHost();
        port = uri.getPort() > 0 ? uri.getPort() : isHttps ? 443 : 80;
    }

    @Override
    public String toString() {
        return String.format("%s://%s%s", getProtocolString(), host, getPortString());
    }

    private String getProtocolString() {
        return isHttps ? "https" : "http";
    }

    private String getPortString(){
        if(port == 80 || port == 443){
            return "";
        }
        return String.format(":%d", port);
    }
}
