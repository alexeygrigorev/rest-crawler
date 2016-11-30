package crawler;

import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Collection;
import java.util.Map;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

public class CrawlingSerivce {

    private static final Logger LOGGER = LoggerFactory.getLogger(CrawlingSerivce.class);

    private static final TypeReference<Map<String, String>> MAP_STRING_STRING = 
            new TypeReference<Map<String, String>>(){};
    private static final TypeReference<Map<String, ProcessedHtml>> MAP_STRING_PROCESSED_HTML = 
            new TypeReference<Map<String, ProcessedHtml>>(){};

    private final ObjectMapper mapper = new ObjectMapper();
    private final String serviceBaseUrl;

    public CrawlingSerivce(String serviceBaseUrl) {
        this.serviceBaseUrl = serviceBaseUrl;
    }

    public Map<String, String> crawl(boolean js, Collection<String> urls) throws Exception {
        LOGGER.debug("from {} with js={} crawling {}", serviceBaseUrl, js, urls);
        String joinedUrls = encode(String.join(";", urls));
        URL service = new URL(serviceBaseUrl + "/crawl?js=" + js + "&urls=" + joinedUrls);
        String json = IOUtils.toString(service);
        return mapper.readValue(json, MAP_STRING_STRING);
    }

    public Map<String, ProcessedHtml> crawlProcessed(boolean js, Collection<String> urls) throws Exception {
        LOGGER.debug("from {} with js={} crawling and processing {}", serviceBaseUrl, js, urls);
        String joinedUrls = encode(String.join(";", urls));
        URL service = new URL(serviceBaseUrl + "/crawl_processed?js=" + js + "&urls=" + joinedUrls);
        String json = IOUtils.toString(service);
        return mapper.readValue(json, MAP_STRING_PROCESSED_HTML);
    }

    private static String encode(String joinedUrls) {
        try {
            return URLEncoder.encode(joinedUrls, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e); // cant happen
        }
    }

}
