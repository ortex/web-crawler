package ru.ortex.crawler;

import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;

public class HtmlUrlExtractorTest {

    @Test
    public void extractUrlsTest() throws IOException {
        var urlExtractor = new JsoupHtmlUrlExtractor();

        InputStream inputStream = getClass().getClassLoader().getResourceAsStream("html/github-main.html");
        String html = new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);

        List<String> urls = urlExtractor.extract(html, "https://github.com");
        Assert.assertEquals(68, urls.size());
    }
}
