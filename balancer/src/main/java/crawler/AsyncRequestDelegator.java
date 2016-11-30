package crawler;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;

public class AsyncRequestDelegator {

    private static final Logger LOGGER = LoggerFactory.getLogger(AsyncRequestDelegator.class);

    private final List<CrawlingSerivce> services;
    private final ExecutorService executor;

    public AsyncRequestDelegator(List<CrawlingSerivce> services) {
        this.services = services;
        this.executor = Executors.newFixedThreadPool(services.size());
    }

    public Map<String, String> crawl(boolean js, List<String> allUrlsToCrawl) {
        ServiceCall<String> call = (s, u) -> s.crawl(js, u);
        return asyncCall(allUrlsToCrawl, call);
    }

    public Map<String, ProcessedHtml> crawlProcessed(boolean js, List<String> allUrlsToCrawl) {
        ServiceCall<ProcessedHtml> call = (s, u) -> s.crawlProcessed(js, u);
        return asyncCall(allUrlsToCrawl, call);
    }

    private <E> Map<String, E> asyncCall(List<String> allUrlsToCrawl, ServiceCall<E> call) {
        Multimap<Integer, String> bucketedUrls = HashMultimap.create();
        for (String url : allUrlsToCrawl) {
            int bucket = bucket(url, services.size());
            bucketedUrls.put(bucket, url);
        }

        List<Future<Map<String, E>>> futures = Lists.newArrayList();
        for (int bucket : bucketedUrls.keySet()) {
            CrawlingSerivce service = services.get(bucket);
            Collection<String> bucketToCrawl = bucketedUrls.get(bucket);
            Future<Map<String, E>> future = executor.submit(() -> call.call(service, bucketToCrawl));
            futures.add(future);
        }

        ImmutableMap.Builder<String, E> allResults = new ImmutableMap.Builder<>();

        for (Future<Map<String, E>> future : futures) {
            try {
                allResults.putAll(future.get());
            } catch (Exception e) {
                LOGGER.error("got error while processing", e);
                throw new RuntimeException(e);
            }
        }

        return allResults.build();
    }

    private static int bucket(String url, int n) {
        return Math.abs(url.hashCode()) % n;
    }

    private static interface ServiceCall<E> {
        Map<String, E> call(CrawlingSerivce service, Collection<String> urls) throws Exception;
    }
}
