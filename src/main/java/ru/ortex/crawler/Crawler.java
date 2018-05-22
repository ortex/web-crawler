package ru.ortex.crawler;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.asynchttpclient.AsyncHttpClient;
import org.asynchttpclient.Dsl;
import ru.ortex.crawler.util.Exceptions;
import ru.ortex.crawler.util.NamedThreadFactory;
import ru.ortex.crawler.util.UrlUtils;

import java.time.Duration;
import java.util.*;
import java.util.concurrent.*;

public class Crawler {
    private static final Logger logger = LogManager.getLogger(Crawler.class);

    private final Queue<String> urls = new ConcurrentLinkedQueue<>();
    private final Set<String> visitedUrls = new HashSet<>();
    private final Set<String> exceptionallyUrls = Collections.newSetFromMap(new ConcurrentHashMap<>());
    private final Set<String> error5xxUrls = Collections.newSetFromMap(new ConcurrentHashMap<>());
    private final Set<String> errorHandleHtmlUrls = Collections.newSetFromMap(new ConcurrentHashMap<>());

    private final AsyncHttpClient client;
    private final ThreadPoolExecutor executor;
    private final int percent5xxToStop;
    private final int percentExceptionsToStop;
    private final HtmlUrlExtractor urlExtractor;
    private final Optional<Semaphore> throttler;

    private long timeStartMillis;


    public Crawler(Config config, HtmlUrlExtractor urlExtractor) {
        urls.add(config.startUrl);

        client = Dsl.asyncHttpClient(Dsl.config()
                .setIoThreadsCount(config.clientIoThreadCount)
                .setConnectTimeout(config.clientConnectTimeoutMillis)
                .setReadTimeout(config.clientReadTimeoutMillis)
                .setRequestTimeout(config.clientRequestTimeoutMillis)
                .setThreadPoolName("http-client")
                .setMaxConnections(config.clientMaxConnections)
        );

        var threadFactory = new NamedThreadFactory("html-parser");
        executor = (ThreadPoolExecutor) Executors.newFixedThreadPool(config.parserPoolSize, threadFactory);

        percent5xxToStop = config.percent5xxToStop;
        percentExceptionsToStop = config.percentExceptionsToStop;

        this.urlExtractor = urlExtractor;

        if (client.getConfig().getMaxConnections() != -1) {
            throttler = Optional.of(new Semaphore(client.getConfig().getMaxConnections()));
        } else {
            throttler = Optional.empty();
        }
    }

    public void run() {
        timeStartMillis = System.currentTimeMillis();
        while (!shouldStop()) {
            final var url = urls.poll();
            if (url == null || visitedUrls.contains(url)) {
                continue;
            }

            throttler.ifPresent(sem -> Exceptions.uncheck(sem::acquire));

            visitedUrls.add(url);

            try {
                handleUrl(url);
            } catch (Exception e) {
                logger.error("error on handle " + url, e);
            }
        }

        while (client.getClientStats().getTotalActiveConnectionCount() > 0) {
            Exceptions.uncheck(() -> Thread.sleep(25));
        }

        Exceptions.uncheck(client::close);
        executor.shutdown();
    }

    private void handleUrl(String url) {
        logger.trace("send request to {}", url);

        client.prepareGet(url)
                .execute()
                .toCompletableFuture()
                .exceptionally(th -> {

                    logger.error("request error " + url, th);
                    exceptionallyUrls.add(url);
                    return null;
                })
                .thenAcceptAsync(response -> {
                    throttler.ifPresent(Semaphore::release);
                    if (response == null) {
                        return;
                    }
                    if (response.getStatusCode() != 200) {
                        if (response.getStatusCode() / 100 == 5) {
                            error5xxUrls.add(url);
                        }
                        logger.trace("get {} code on {} page", response.getStatusCode(), url);
                        return;
                    }

                    urlExtractor.extract(response.getResponseBody(), url).forEach(ref -> {
                        Optional<String> href = UrlUtils.normalize(ref, url);
                        href.ifPresent(urls::add);
                    });

                }, executor)
                .whenComplete((__, th) -> {
                    if (th != null) {
                        errorHandleHtmlUrls.add(url);
                        logger.error("error with " + url, th);
                    }
                    int totalError = exceptionallyUrls.size() + errorHandleHtmlUrls.size() + error5xxUrls.size();
                    logger.trace("active conn: {} | parser queue: {}, active: {} | visited: {} | urls queue: {} | total error: {}",
                            client.getClientStats().getTotalActiveConnectionCount(), executor.getQueue().size(),
                            executor.getActiveCount(), visitedUrls.size(), urls.size(), totalError);

                });
    }

    private boolean shouldStop() {
        var maxConnections = client.getConfig().getMaxConnections();
        var hasReqInProgress = throttler.map(sem -> sem.availablePermits() == maxConnections).orElse(false);
        var crawledWholeSite = urls.isEmpty() && hasReqInProgress && executor.getActiveCount() == 0 && executor.getQueue().isEmpty();

        if (crawledWholeSite) {
            logger.info("End working. Crawled whole site.");
            return true;
        }
        if (visitedUrls.isEmpty()) {
            return false;
        }
        var reachExceptionThreshold = exceptionallyUrls.size() * 100 / visitedUrls.size() >= percentExceptionsToStop;
        if (reachExceptionThreshold) {
            logger.info("End working. Reach exceptions threshold - {}%", percentExceptionsToStop);
            return true;
        }
        var reach5xxThreshold = error5xxUrls.size() * 100 / visitedUrls.size() >= percent5xxToStop;
        if (reach5xxThreshold) {
            logger.info("End working. Reach error 5xx threshold - {}%", percent5xxToStop);
            return true;
        }
        return false;
    }

    public Duration getWorkDuration() {
        return Duration.ofMillis(System.currentTimeMillis() - timeStartMillis);
    }

    public Set<String> getVisitedUrls() {
        return new HashSet<>(visitedUrls);
    }

    public Set<String> getExceptionallyUrls() {
        return new HashSet<>(exceptionallyUrls);
    }

    public Set<String> getError5xxUrls() {
        return new HashSet<>(error5xxUrls);
    }

    public Set<String> getErrorHandleHtmlUrls() {
        return new HashSet<>(errorHandleHtmlUrls);
    }

}
