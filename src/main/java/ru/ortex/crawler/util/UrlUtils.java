package ru.ortex.crawler.util;

import org.asynchttpclient.uri.Uri;

import java.util.Optional;

public class UrlUtils {

    public static Optional<String> normalize(String url, String baseUrl) {

        url = url.trim();
        if (url.isEmpty() || url.startsWith("#")) {
            return Optional.empty();
        }

        int sharpIndex = url.indexOf('#');
        if (sharpIndex != -1) {
            url = url.substring(0, sharpIndex);
        }

        baseUrl = cutPath(baseUrl);

        if (url.startsWith("/")) {
            return Optional.of(baseUrl + url);
        }
        if (url.startsWith(baseUrl)) {
            return Optional.of(url);
        }
        return Optional.empty();
    }

    private static String cutPath(String fullUrl) {
        var url = Uri.create(fullUrl);

        return url.getScheme() + "://" + url.getHost() + (url.getPort() == -1 ? "" : ":" + url.getPort());
    }
}
