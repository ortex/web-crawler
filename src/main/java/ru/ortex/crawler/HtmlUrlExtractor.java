package ru.ortex.crawler;

import java.util.List;

public interface HtmlUrlExtractor {

    List<String> extract(String html, String baseUri);
}
