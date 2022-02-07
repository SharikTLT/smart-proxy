package ru.shariktlt.smart.proxy;

import ru.shariktlt.smart.proxy.iterators.ListProxyIterator;
import ru.shariktlt.smart.proxy.iterators.ProxyIterator;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import static java.util.Arrays.asList;

public class ServersRegistry {

    private Map<ServerRecord, Map<String, Boolean>> registry = new ConcurrentHashMap<>();

    public void register(ServerRecord record, String url) {
        register(record);
        registry.get(record).put(cleanupUrl(url), true);
    }

    public void register(ServerRecord record) {
        if (!registry.containsKey(record)) {
            synchronized (this) {
                registry.putIfAbsent(record, new ConcurrentHashMap<>());
            }
        }
    }

    public boolean check(ServerRecord record, String rawUrl) {
        String url = cleanupUrl(rawUrl);
        return registry.containsKey(record) && registry.get(record).containsKey(url);
    }

    public void unregister(ServerRecord record) {
        registry.remove(record);
    }

    public void clear(ServerRecord record) {
        if (registry.containsKey(record)) {
            registry.get(record).clear();
        }
    }

    public void remove(ServerRecord record, String... urls) {
        if (registry.containsKey(record)) {
            Arrays.stream(urls)
                    .filter(registry.get(record)::containsKey)
                    .map(registry.get(record)::remove);
        }
    }

    public ProxyIterator getIteratorFor(String rawUrl) {
        String url = cleanupUrl(rawUrl);
        Optional<Map.Entry<ServerRecord, Map<String, Boolean>>> exact = this.registry.entrySet().stream()
                .filter(e -> e.getValue().containsKey(url))
                .findFirst();
        if (exact.isPresent()) {
            return new ListProxyIterator(this, asList(exact.get().getKey()));
        }
        return new ListProxyIterator(this, new ArrayList<>(registry.keySet()));
    }

    public List<Map.Entry<ServerRecord, Map<String, Boolean>>> getServers() {
        return new ArrayList<>(registry.entrySet());
    }

    public List<String> getServerUrls(ServerRecord record) {
        if(!registry.containsKey(record)){
            return Collections.emptyList();
        }
        List<String> urls = new ArrayList<>(registry.get(record).keySet());
        urls.sort(String::compareTo);
        return urls;
    }

    private String cleanupUrl(String rawUrl) {
        int pos = rawUrl.indexOf('?');
        if (pos > -1) {
            return rawUrl.substring(0, pos);
        }
        return rawUrl;
    }
}
