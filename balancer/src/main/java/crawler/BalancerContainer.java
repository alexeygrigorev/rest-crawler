package crawler;

import java.util.List;

import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;

import com.google.common.collect.Lists;

@Configuration
public class BalancerContainer {

    @Value("classpath:addresses.txt")
    private Resource serverUrls;

    @Bean
    public AsyncRequestDelegator crawler() throws Exception {
        List<String> urls = IOUtils.readLines(serverUrls.getInputStream());

        List<CrawlingSerivce> services = Lists.newArrayList();
        for (String url : urls) {
            services.add(new CrawlingSerivce(url));
        }

        return new AsyncRequestDelegator(services);
    }

}
