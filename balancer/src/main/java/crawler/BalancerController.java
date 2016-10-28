package crawler;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.google.common.base.Stopwatch;

@RestController
public class BalancerController {

    private static final Logger LOGGER = LoggerFactory.getLogger(BalancerController.class);
    private final DelegatingCrawler crawler;

    @Autowired
    public BalancerController(DelegatingCrawler crawler) {
        this.crawler = crawler;
        
    }

    @RequestMapping("crawl")
    public Map<String, String> getHtml(@RequestParam(name = "urls") String urls,
            @RequestParam(name = "js", defaultValue = "false") boolean js) throws Exception {
        List<String> allUrlsToCrawl = Arrays.asList(urls.split(";"));

        Stopwatch stopwatch = Stopwatch.createStarted();
        Map<String, String> result = crawler.crawl(js, allUrlsToCrawl);
        LOGGER.info("crawling took {}", stopwatch.stop());

        return result;
    }



}
