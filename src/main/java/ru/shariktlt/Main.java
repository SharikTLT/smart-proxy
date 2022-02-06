package ru.shariktlt;

import ru.shariktlt.proxy.Proxy;

public class Main {

    public static void main(String[] args) {
        Proxy proxy = new Proxy(7777);
        proxy.register(
                "https://stackoverflow.com/",
                "https://docs.oracle.com"
        );
        proxy.init();
    }
}
