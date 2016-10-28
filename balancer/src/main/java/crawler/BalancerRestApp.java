package crawler;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class BalancerRestApp {

    public static void main(String[] args) {
        args = new String[] {"--server.port=9291"};
        SpringApplication.run(BalancerRestApp.class, args);
    }

}
