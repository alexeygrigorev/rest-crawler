package crawler;

import java.nio.charset.StandardCharsets;
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
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.LaxRedirectStrategy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HttpComponentsCrawler implements Crawler, AutoCloseable {

    private static final Logger LOGGER = LoggerFactory.getLogger(HttpComponentsCrawler.class);

    private final ExecutorService executor;
    private final int timeout;
    private final CloseableHttpClient client;

    public HttpComponentsCrawler(int timeout, ExecutorService executor) {
        this.timeout = timeout;
        this.executor = executor;
        this.client = createHttpComponent();
    }

    private static CloseableHttpClient createHttpComponent() {
        return HttpClientBuilder.create()
                .setRedirectStrategy(new LaxRedirectStrategy())
                .setUserAgent("Mozilla/5.0 (X11; Ubuntu; Linux x86_64; rv:47.0) Gecko/20100101 Firefox/47.0")
                .build();
    }

    public HttpComponentsCrawler(int maxThreads, int timeout) {
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
        } catch (TimeoutException e) {
            LOGGER.debug("time out for {}", url);
        } catch (InterruptedException | ExecutionException e) {
            LOGGER.debug("execution exception for {}: {}", url, e.getMessage());
        }

        return Optional.empty();
    }

    private String doCrawl(String url) {
        try {
            HttpGet get = new HttpGet(url);
            
            try (CloseableHttpResponse response = client.execute(get)) {
                HttpEntity entity = response.getEntity();
                return IOUtils.toString(entity.getContent(), StandardCharsets.UTF_8);
            }
        } catch (Exception e) {
            LOGGER.warn("unexpected error happened, rethrowing it", e);
            throw new RuntimeException(e);
        }
    }

    @Override
    public void close() throws Exception {
        client.close();
    }

}

