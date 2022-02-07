package ru.shariktlt.smart.proxy;

import org.junit.jupiter.api.Test;

import java.net.UnknownHostException;

import static org.junit.jupiter.api.Assertions.*;

class ServerRecordTest {

    @Test
    public void testWithHttps() throws UnknownHostException {
        ServerRecord record = new ServerRecord("https://wikipedia.org:8888");
        assertNotNull(record);
        assertEquals("wikipedia.org", record.getHost());
        assertEquals(8888, record.getPort());
        assertTrue(record.isHttps());
    }

    @Test
    public void testWithHttpsNoPort() throws UnknownHostException {
        ServerRecord record = new ServerRecord("https://wikipedia.org");
        assertNotNull(record);
        assertEquals("wikipedia.org", record.getHost());
        assertEquals(443, record.getPort());
        assertTrue(record.isHttps());
    }

    @Test
    public void testWithHttp() throws UnknownHostException {
        ServerRecord record = new ServerRecord("http://wikipedia.org:8888");
        assertNotNull(record);
        assertEquals("wikipedia.org", record.getHost());
        assertEquals(8888, record.getPort());
        assertFalse(record.isHttps());
    }

    @Test
    public void testWithHttpNoPort() throws UnknownHostException {
        ServerRecord record = new ServerRecord("http://wikipedia.org");
        assertNotNull(record);
        assertEquals("wikipedia.org", record.getHost());
        assertEquals(80, record.getPort());
        assertFalse(record.isHttps());
    }
}