package ru.ortex.crawler;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.util.Properties;

public class Main {

    private static final Logger logger = LogManager.getLogger(Main.class);
    private static final String configName = "config.properties";

    public static void main(String[] args) {

        var properties = new Properties();
        try {
            properties.load(Main.class.getClassLoader().getResourceAsStream(configName));
        } catch (IOException e) {
            logger.error("Can't load properties from {}", configName);
        }

        var crawler = new Crawler(new Config(properties), new JsoupHtmlUrlExtractor());

        crawler.run();

        var workDuration = crawler.getWorkDuration();
        logger.info("Work duration: {}", workDuration.toString().substring(2));

        int visitedTotal = crawler.getVisitedUrls().size();
        int visitedError = crawler.getError5xxUrls().size() + crawler.getExceptionallyUrls().size();
        int parseHtmlError = crawler.getErrorHandleHtmlUrls().size();
        logger.info("Visited total={}, error={} | html handle error: {}", visitedTotal, visitedError, parseHtmlError);

    }
}
