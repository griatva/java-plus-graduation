package ru.practicum.ewm.main.config;

import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.retry.backoff.FixedBackOffPolicy;
import org.springframework.retry.policy.MaxAttemptsRetryPolicy;
import org.springframework.retry.support.RetryTemplate;
import ru.practicum.ewm.client.StatClient;

@Configuration
public class StatClientConfig {


    @Bean
    public RetryTemplate retryTemplate() {
        RetryTemplate template = new RetryTemplate();

        FixedBackOffPolicy backOff = new FixedBackOffPolicy();
        backOff.setBackOffPeriod(2000L);
        template.setBackOffPolicy(backOff);

        MaxAttemptsRetryPolicy retryPolicy = new MaxAttemptsRetryPolicy();
        retryPolicy.setMaxAttempts(3);
        template.setRetryPolicy(retryPolicy);

        return template;
    }

    @Bean
    public StatClient statClient(DiscoveryClient discoveryClient, RetryTemplate retryTemplate) {
        return new StatClient(discoveryClient, retryTemplate);
    }
}