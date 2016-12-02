package crawler.process;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Service;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ListMultimap;

@Service
public class HtmlProcessor {
    private static final String TAGS_TO_REMOVE = "iframe, embed, script, noscript, nobr, "
            + "style, link, img, source, wbr, area, base, param";

    public String clean(String html) {
        Document doc = Jsoup.parse(html);
        doc.select(TAGS_TO_REMOVE).remove();
        return doc.outerHtml();
    }

    public ProcessedHtml process(String html) {
        Document doc = Jsoup.parse(html);
        String title = doc.title();

        doc.select(TAGS_TO_REMOVE).remove();

        String bodyHtml = doc.select("body").outerHtml();
        Document body = Jsoup.parse(bodyHtml);

        JsoupTextExtractor visitor = new JsoupTextExtractor();
        body.traverse(visitor);
        String content = visitor.getText();

        ListMultimap<String, String> tags = ArrayListMultimap.create();
        Elements headers = body.select("h1, h2, h3, h4, h5, h6");
        for (Element htag : headers) {
            String tagName = htag.nodeName().toLowerCase();
            String text = htag.text().trim();
            if (!text.isEmpty()) {
                tags.put(tagName, text);
            }
        }

        ProcessedHtml result = new ProcessedHtml();
        result.setTitle(title);
        result.setContent(content);
        result.setH1(tags.get("h1"));
        result.setH2(tags.get("h2"));
        result.setH3(tags.get("h3"));
        result.setH4(tags.get("h4"));
        result.setH5(tags.get("h5"));
        result.setH6(tags.get("h6"));

        return result;
    }

}