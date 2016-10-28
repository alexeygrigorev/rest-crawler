package crawler;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;

public class DelegatingCrawler {

    private static final Logger LOGGER = LoggerFactory.getLogger(DelegatingCrawler.class);

    private final List<CrawlingSerivce> services;
    private final ExecutorService executor;

    public DelegatingCrawler(List<CrawlingSerivce> services) {
        this.services = services;
        this.executor = Executors.newFixedThreadPool(services.size());
    }

    public Map<String, String> crawl(boolean js, List<String> allUrlsToCrawl) {
        Map<String, String> allResults = new ConcurrentHashMap<>();

        Multimap<Integer, String> bucketedUrls = HashMultimap.create();
        for (String url : allUrlsToCrawl) {
            int bucket = bucket(url, services.size());
            bucketedUrls.put(bucket, url);
        }

        List<Future<Map<String, String>>> futures = Lists.newArrayList();
        for (int bucket : bucketedUrls.keySet()) {
            CrawlingSerivce service = services.get(bucket);
            Collection<String> bucketToCrawl = bucketedUrls.get(bucket);
            Future<Map<String, String>> future = executor.submit(() -> service.crawl(js, bucketToCrawl));
            futures.add(future);
        }

        for (Future<Map<String, String>> future : futures) {
            try {
                allResults.putAll(future.get());
            } catch (Exception e) {
                LOGGER.error("got error while processing", e);
                throw new RuntimeException(e);
            }
        }

        return allResults;
    }

    private static int bucket(String url, int n) {
        return Math.abs(url.hashCode()) % n;
    }

}
