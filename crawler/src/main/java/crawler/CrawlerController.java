package crawler;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import javax.inject.Named;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.google.common.base.Stopwatch;

@RestController
public class CrawlerController {

    private static final Logger LOGGER = LoggerFactory.getLogger(CrawlerController.class);

    private final Crawler jsCrawler;
    private final Crawler noJsCrawler;

    @Autowired
    public CrawlerController(@Named("jsCrawler") Crawler jsCrawler, @Named("noJsCrawler") Crawler noJsCrawler) {
        this.jsCrawler = jsCrawler;
        this.noJsCrawler = noJsCrawler;
    }

    @RequestMapping("crawl")
    public Map<String, String> getHtml(@RequestParam(name = "urls") String urls,
            @RequestParam(name = "js", defaultValue = "false") boolean js) throws Exception {
        Stopwatch stopwatch = Stopwatch.createStarted();
        List<String> toCrawl = Arrays.asList(urls.split(";"));

        LOGGER.info("with js={} crawling {}", js, toCrawl);

        Map<String, String> result = null;

        if (js) {
            result = jsCrawler.crawl(toCrawl);
        } else {
            result = noJsCrawler.crawl(toCrawl);
        }

        LOGGER.info("crawling took {}", stopwatch.stop());
        return result;
    }

}
