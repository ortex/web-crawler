package ru.ortex.crawler;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.util.List;
import java.util.stream.Collectors;

public class JsoupHtmlUrlExtractor implements HtmlUrlExtractor {
    private static final Logger logger = LogManager.getLogger(JsoupHtmlUrlExtractor.class);

    @Override
    public List<String> extract(String html, String baseUri) {

        var timeBegin = System.currentTimeMillis();
        Document doc = Jsoup.parse(html, baseUri);
        var timeTaken = System.currentTimeMillis() - timeBegin;

        Elements elements = doc.select("a[href]");

        logger.trace("found {} urls on {} page; parsed for {} ms", elements.size(), baseUri, timeTaken);

        return elements.stream().map(e -> e.attr("href")).collect(Collectors.toList());
    }
}
