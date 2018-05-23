package ru.ortex.crawler;

import java.util.Objects;
import java.util.Properties;

public class Config {

    public final String startUrl;

    public final int clientMaxConnections;
    public final int clientConnectTimeoutMillis;
    public final int clientReadTimeoutMillis;
    public final int clientRequestTimeoutMillis;
    public final int clientIoThreadCount;

    public final int parserPoolSize;

    public final int percent5xxToStop;
    public final int percentExceptionsToStop;

    public Config(Properties properties) {
        startUrl = Objects.requireNonNull(properties.getProperty("crawler.startUrl"), "Not found crawler.startUrl in properties file");

        clientMaxConnections = Integer.parseInt(properties.getProperty("crawler.client.maxConnections", "-1"));
        clientConnectTimeoutMillis = Integer.parseInt(properties.getProperty("crawler.client.connectTimeoutMillis", "10000"));
        clientReadTimeoutMillis = Integer.parseInt(properties.getProperty("crawler.client.readTimeoutMillis", "10000"));
        clientRequestTimeoutMillis = Integer.parseInt(properties.getProperty("crawler.client.requestTimeoutMillis", "15000"));
        String propClientIoThreadCount = properties.getProperty("crawler.client.ioThreadCount");
        clientIoThreadCount = propClientIoThreadCount != null ? Integer.parseInt(propClientIoThreadCount) : Runtime.getRuntime().availableProcessors();

        String propParserPoolSize = properties.getProperty("crawler.parserExecutorPoolSize");
        parserPoolSize = propParserPoolSize != null ? Integer.parseInt(propParserPoolSize) : 1;

        percent5xxToStop = Integer.parseInt(properties.getProperty("crawler.percent5xxToStop", "10"));
        percentExceptionsToStop = Integer.parseInt(properties.getProperty("crawler.percentExceptionsToStop", "10"));
    }

    @Override
    public String toString() {
        return "startUrl='" + startUrl + "'\n" +
                "clientMaxConnections=" + clientMaxConnections + "\n" +
                "clientConnectTimeoutMillis=" + clientConnectTimeoutMillis + "\n" +
                "clientReadTimeoutMillis=" + clientReadTimeoutMillis + "\n" +
                "clientRequestTimeoutMillis=" + clientRequestTimeoutMillis + "\n" +
                "clientIoThreadCount=" + clientIoThreadCount + "\n" +
                "parserPoolSize=" + parserPoolSize + "\n" +
                "percent5xxToStop=" + percent5xxToStop + "\n" +
                "percentExceptionsToStop=" + percentExceptionsToStop + "\n"
                ;
    }
}
