package ru.shariktlt.smart.proxy;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ServersRegistryTest {

    public static final ServerRecord RECORD = new ServerRecord("http://localhost");
    public static final String SOME_PATH_WITH_PARAM_1 = "/somePath?withParam=1";
    public static final String SOME_PATH_WITHOUT_PARAM = "/somePath";
    private ServersRegistry registry;

    @BeforeEach
    void setUp() {
        registry = new ServersRegistry();
    }

    @Test
    void register() {
        assertEquals(0, registry.getServers().size());
        registry.register(RECORD);
        registry.register(new ServerRecord("http://localhost/"));
        assertEquals(1, registry.getServers().size());
    }

    @Test
    void check() {
        assertFalse(registry.check(RECORD, SOME_PATH_WITH_PARAM_1));
        assertFalse(registry.check(RECORD, SOME_PATH_WITHOUT_PARAM));
        registry.register(RECORD, SOME_PATH_WITH_PARAM_1);
        assertTrue(registry.check(RECORD, SOME_PATH_WITH_PARAM_1));
        assertTrue(registry.check(RECORD, SOME_PATH_WITHOUT_PARAM));
    }

    @Test
    void unregister() {
    }

    @Test
    void clear() {
    }

    @Test
    void remove() {
    }

    @Test
    void getIteratorFor() {
    }

    @Test
    void getServers() {
    }

    @Test
    void getServerUrls() {
    }
}