package in.flatmate;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.util.TimeZone;

@SpringBootApplication
@ConfigurationPropertiesScan
@EnableScheduling
public class FlatmateApplication {

    public static void main(String[] args) {
        // Everything is stored and serialised as UTC. Without this the JVM
        // picks up the machine timezone, Hibernate converts on read/write, and
        // every timestamp the API returns is silently offset.
        TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
        SpringApplication.run(FlatmateApplication.class, args);
    }
}
