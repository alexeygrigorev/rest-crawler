package crawler;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.apache.commons.pool.PoolableObjectFactory;
import org.apache.commons.pool.impl.GenericObjectPool;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.phantomjs.PhantomJSDriver;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PhantomJsCrawler implements AutoCloseable, Crawler {

    private static final Logger LOGGER = LoggerFactory.getLogger(PhantomJsCrawler.class);

    private static final int DEFAULT_WAIT_TIME = 500;

    private final ExecutorService executor;
    private final int timeout;
    private final int waitTime;
    private final GenericObjectPool<WebDriver> pool;

    public PhantomJsCrawler(int phantomJsInstances, int timeout, int waitTime, ExecutorService executor) {
        this.timeout = timeout;
        this.waitTime = waitTime;
        this.executor = executor;
        this.pool = createPhantomJsPool(phantomJsInstances);
    }

    private static GenericObjectPool<WebDriver> createPhantomJsPool(int poolSize) {
        PoolableObjectFactory<WebDriver> driverFactory = new PhantomJsFactory();
        GenericObjectPool<WebDriver> pool = new GenericObjectPool<>(driverFactory);
        pool.setMaxActive(poolSize);
        pool.setMaxIdle(poolSize);
        return pool;
    }

    public PhantomJsCrawler(int maxThreads, int timeout) {
        this(maxThreads, timeout, DEFAULT_WAIT_TIME, Executors.newFixedThreadPool(maxThreads));
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
        return doRetryCrawl(url);
    }

    private Optional<String> doRetryCrawl(String url) {
        int numTrials = 3;

        // sometimes the driver is not available. let's try several times with different drivers
        while (numTrials > 0) {
            try {
                return unsafeTimeoutCrawl(url);
            } catch (Exception e) {
                LOGGER.warn("unexpected error happened, swallowing it", e);
            }
            numTrials--;
            sleep();
        }

        LOGGER.warn("could not finish the crawl after 3 trials");
        return Optional.empty();
    }

    private Optional<String> unsafeTimeoutCrawl(String url) throws Exception {
        LOGGER.info("crawling {}...", url);
        WebDriver driver = pool.borrowObject();

        try {
            Future<String> future = executor.submit(() -> {
                driver.get(url);
                sleep();
                return driver.getPageSource();
            });

            try {
                String html = future.get(timeout, TimeUnit.MILLISECONDS);
                return Optional.of(html);
            } catch (TimeoutException e) {
                LOGGER.debug("timeout while crawling {}", url);
                return Optional.empty();
            }
        } finally {
            pool.returnObject(driver);
        }
    }

    private void sleep() {
        try {
            Thread.sleep(waitTime);
        } catch (InterruptedException e) {
        }
    }

    @Override
    public void close() throws Exception {
        pool.clear();
        pool.close();
    }

    private static final class PhantomJsFactory implements PoolableObjectFactory<WebDriver> {
        @Override
        public boolean validateObject(WebDriver obj) {
            return true;
        }

        @Override
        public void passivateObject(WebDriver obj) throws Exception {
        }

        @Override
        public WebDriver makeObject() throws Exception {
            DesiredCapabilities phantomjs = DesiredCapabilities.phantomjs();
            return new PhantomJSDriver(phantomjs);
        }

        @Override
        public void destroyObject(WebDriver driver) throws Exception {
            try {
                driver.close();
            } catch (Exception e) {
                LOGGER.warn("something terrible happened on close()", e);
            }

            try {
                driver.quit();
            } catch (Exception e) {
                LOGGER.warn("something terrible happened on close()", e);
            }
        }

        @Override
        public void activateObject(WebDriver driver) throws Exception {
        }
    }

    public static void setPhantomJsPathToBin(String path) {
        System.setProperty("phantomjs.binary.path", path);
    }

}
