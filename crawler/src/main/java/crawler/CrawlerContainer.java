package crawler;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.inject.Named;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import crawler.crawlers.CachingCrawler;
import crawler.crawlers.Crawler;
import crawler.crawlers.HttpComponentsCrawler;
import crawler.crawlers.PhantomJsCrawler;

@Configuration
public class CrawlerContainer {

    @Bean(name = "crawlingExecutor")
    public ExecutorService executor(@Value("${crawler.threads}") int numThreads) {
        return Executors.newFixedThreadPool(numThreads);
    }

    @Bean(name = "jsCrawler")
    public Crawler jsCrawler(
            @Named("crawlingExecutor") ExecutorService executor,
            @Value("${crawler.js.phantomjs.bin}") String phantomJsPath,
            @Value("${crawler.js.phantomjs.instances}") int phantomJsInstances,
            @Value("${crawler.js.timeout}") int timeOut, 
            @Value("${crawler.js.sleep}") int waitingTime,
            @Value("${crawler.js.cache.ttl.hours}") int cacheTtl) {
        PhantomJsCrawler.setPhantomJsPathToBin(phantomJsPath);
        PhantomJsCrawler crawler = new PhantomJsCrawler(phantomJsInstances, timeOut, waitingTime, executor);
        return new CachingCrawler(crawler, "phantomjs", cacheTtl);
    }

    @Bean(name = "noJsCrawler")
    public Crawler noJsCrawler(
            @Named("crawlingExecutor") ExecutorService executor,
            @Value("${crawler.java.timeout}") int timeOut, 
            @Value("${crawler.java.cache.ttl.hours}") int cacheTtl) {
        HttpComponentsCrawler crawler = new HttpComponentsCrawler(timeOut, executor);
        return new CachingCrawler(crawler, "java", cacheTtl);
    }

}
