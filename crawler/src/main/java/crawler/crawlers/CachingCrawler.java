package crawler.crawlers;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import org.mapdb.DB;
import org.mapdb.DBMaker;
import org.mapdb.HTreeMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CachingCrawler implements Crawler, AutoCloseable {

    private static final Logger LOGGER = LoggerFactory.getLogger(CachingCrawler.class);

    private static final int MINIMAL_HTML_LEN_FOR_CACHING = 200;

    private final Crawler crawler;

    private final Map<String, String> cache;
    private final DB db;

    public CachingCrawler(Crawler crawler, String cacheName, int timeToLive) {
        this.crawler = crawler;

        this.db = makeDb(cacheName);
        this.cache = createUrlMapDatabase(this.db, timeToLive);
    }

    @Override
    public Map<String, String> crawl(List<String> urls) {
        List<String> cacheMisses = new ArrayList<>();
        Map<String, String> results = new HashMap<>();

        for (String url : urls) {
            Optional<String> hit = htmlIfCached(url);
            if (hit.isPresent()) {
                results.put(url, hit.get());
            } else {
                cacheMisses.add(url);
            }
        }

        if (cacheMisses.isEmpty()) {
            return results;
        }

        Map<String, String> missingResults = crawler.crawl(cacheMisses);
        for (Entry<String, String> e : missingResults.entrySet()) {
            cache(e.getKey(), e.getValue());
        }

        results.putAll(missingResults);
        return results;
    }

    @Override
    public Optional<String> crawl(String url) {
        Optional<String> result = crawler.crawl(url);
        if (result.isPresent()) {
            cache(url, result.get());
            return result;
        }

        return Optional.empty();
    }

    private Optional<String> htmlIfCached(String url) {
        if (cache.containsKey(url)) {
            LOGGER.debug("cache hit for url {}", url);
            String html = cache.get(url);
            return Optional.of(html);
        }

        return Optional.empty();
    }

    private void cache(String url, String html) {
        if (html.length() < MINIMAL_HTML_LEN_FOR_CACHING) {
            LOGGER.debug("the content of {} is too small to put into cache - probably a crawling error", url);
            return;
        }

        cache.put(url, html);
    }

    @Override
    public void close() throws Exception {
        db.close();
    }

    private static DB makeDb(String cacheName) {
        File dir = new File("cache/");
        dir.mkdir();

        File dbFile = new File(dir, cacheName + ".db");
        return DBMaker.fileDB(dbFile).closeOnJvmShutdown().make();
    }

    private static Map<String, String> createUrlMapDatabase(DB db, int timeToLive) {
        HTreeMap<?, ?> htreeMap = db.hashMap("urls").expireAfterCreate(timeToLive, TimeUnit.HOURS).createOrOpen();
        @SuppressWarnings("unchecked")
        Map<String, String> map = (Map<String, String>) htreeMap;
        return map;
    }

}
