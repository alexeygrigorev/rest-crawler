package crawler;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.inject.Named;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.google.common.base.Stopwatch;

import crawler.crawlers.Crawler;
import crawler.process.HtmlProcessor;
import crawler.process.ProcessedHtml;

@RestController
public class CrawlerController {

    private static final Logger LOGGER = LoggerFactory.getLogger(CrawlerController.class);

    private final Crawler jsCrawler;
    private final Crawler noJsCrawler;
    private final HtmlProcessor htmlProcessor;

    @Autowired
    public CrawlerController(@Named("jsCrawler") Crawler jsCrawler, @Named("noJsCrawler") Crawler noJsCrawler,
            HtmlProcessor htmlProcessor) {
        this.jsCrawler = jsCrawler;
        this.noJsCrawler = noJsCrawler;
        this.htmlProcessor = htmlProcessor;
    }

    @RequestMapping("crawl")
    public Map<String, String> getHtml(@RequestParam(name = "urls") String urls,
            @RequestParam(name = "js", defaultValue = "false") boolean js) throws Exception {
        Stopwatch stopwatch = Stopwatch.createStarted();
        List<String> toCrawl = Arrays.asList(urls.split(";"));

        LOGGER.info("with js={} crawling {}", js, toCrawl);

        Map<String, String> result = html(js, toCrawl);
        result = clean(result);

        LOGGER.info("crawling took {}", stopwatch.stop());
        return result;
    }

    private Map<String, String> clean(Map<String, String> rawhtml) {
        Map<String, String> result = new HashMap<>(rawhtml.size());
        for (Entry<String, String> e : rawhtml.entrySet()) {
            String url = e.getKey();
            String html = htmlProcessor.clean(e.getValue());
            result.put(url, html);
        }
        return result;
    }

    @RequestMapping("crawl_processed")
    public Map<String, ProcessedHtml> getProcessed(@RequestParam(name = "urls") String urls,
            @RequestParam(name = "js", defaultValue = "false") boolean js) throws Exception {
        Stopwatch stopwatch = Stopwatch.createStarted();
        List<String> toCrawl = Arrays.asList(urls.split(";"));

        LOGGER.info("with js={} crawling {}", js, toCrawl);

        Map<String, String> htmls = html(js, toCrawl);
        Map<String, ProcessedHtml> result = new HashMap<>();

        for (Entry<String, String> e : htmls.entrySet()) {
            String url = e.getKey();
            String html = e.getValue();
            ProcessedHtml processed = htmlProcessor.process(html);
            result.put(url, processed);
        }

        LOGGER.info("crawling took {}", stopwatch.stop());
        return result;
    }

    private Map<String, String> html(boolean js, List<String> toCrawl) {
        if (js) {
            return jsCrawler.crawl(toCrawl);
        } else {
            return  noJsCrawler.crawl(toCrawl);
        }
    }

}
