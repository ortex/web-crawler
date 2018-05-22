package ru.ortex.crawler;

import org.junit.Assert;
import org.junit.Test;
import ru.ortex.crawler.util.UrlUtils;

import java.util.Optional;

import static org.hamcrest.CoreMatchers.is;

public class UrlUtilsTest {

    @Test
    public void normalizeUrlsTest() {
        Assert.assertThat(
                UrlUtils.normalize("/features", "https://github.com/"),
                is(Optional.of("https://github.com/features")));

        Assert.assertThat(
                UrlUtils.normalize("/features", "https://github.com"),
                is(Optional.of("https://github.com/features")));

        Assert.assertThat(
                UrlUtils.normalize("/features", "https://github.com/AsyncHttpClient"),
                is(Optional.of("https://github.com/features")));

        Assert.assertThat(
                UrlUtils.normalize("/features", "https://github.com:8080/AsyncHttpClient"),
                is(Optional.of("https://github.com:8080/features")));

        Assert.assertThat(
                UrlUtils.normalize("https://github.com/site/terms", "https://github.com/AsyncHttpClient"),
                is(Optional.of("https://github.com/site/terms")));

        Assert.assertThat(
                UrlUtils.normalize(" /open-source/stories/ariya ", "https://github.com/AsyncHttpClient"),
                is(Optional.of("https://github.com/open-source/stories/ariya")));


        Assert.assertThat(
                UrlUtils.normalize("#go-to-content", "https://github.com/AsyncHttpClient"),
                is(Optional.empty()));

        Assert.assertThat(
                UrlUtils.normalize("https://mozilla.org/firefox/", "https://github.com/AsyncHttpClient"),
                is(Optional.empty()));

        Assert.assertThat(
                UrlUtils.normalize("/features#super", "https://github.com:8080/AsyncHttpClient"),
                is(Optional.of("https://github.com:8080/features")));
    }
}
