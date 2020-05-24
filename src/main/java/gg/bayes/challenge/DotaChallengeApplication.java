package gg.bayes.challenge;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication
@EnableJpaRepositories({"gg.bayes.challenge.repository"})
@ComponentScan({"gg.bayes.challenge.*", "gg.bayes.challenge.entity.*"})
public class DotaChallengeApplication {

    public static void main(String[] args) {
        SpringApplication.run(DotaChallengeApplication.class, args);
    }

}
