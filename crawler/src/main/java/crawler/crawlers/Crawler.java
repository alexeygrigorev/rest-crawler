package crawler.crawlers;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface Crawler {

    Map<String, String> crawl(List<String> urls);

    Optional<String> crawl(String url);

}