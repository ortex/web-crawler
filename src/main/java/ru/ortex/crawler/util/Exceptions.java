package ru.ortex.crawler.util;

public class Exceptions {

    public static void uncheck(ThrowingVoid throwingVoid) {
        try {
            throwingVoid.get();
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @FunctionalInterface
    public interface ThrowingVoid {
        void get() throws Exception;
    }
}
