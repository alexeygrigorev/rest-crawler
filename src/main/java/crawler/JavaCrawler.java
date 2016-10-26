package crawler;

import java.net.URL;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JavaCrawler implements Crawler {

    private static final Logger LOGGER = LoggerFactory.getLogger(JavaCrawler.class);

    private final ExecutorService executor;
    private final int timeout;

    public JavaCrawler(int timeout, ExecutorService executor) {
        this.timeout = timeout;
        this.executor = executor;
    }

    public JavaCrawler(int maxThreads, int timeout) {
        this(timeout, Executors.newFixedThreadPool(maxThreads));
    }

    @Override
    public Map<String, String> crawl(List<String> urls) {
        Map<String, String> result = new ConcurrentHashMap<>();

        urls.parallelStream().forEach(url -> {
            Optional<String> html = crawl(url);
            if (html.isPresent()) {
                result.put(url, html.get());
            }
        });

        return result;
    }

    @Override
    public Optional<String> crawl(String url) {
        Future<String> future = executor.submit(() -> doCrawl(url));
        try {
            String html = future.get(timeout, TimeUnit.MILLISECONDS);
            return Optional.of(html);
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            LOGGER.debug("time out for {}", url);
            return Optional.empty();
        }
    }

    private String doCrawl(String url) {
        try {
            return IOUtils.toString(new URL(url));
        } catch (Exception e) {
            LOGGER.warn("unexpected error happened, rethrowing it", e);
            throw new RuntimeException(e);
        } 
    }

}
