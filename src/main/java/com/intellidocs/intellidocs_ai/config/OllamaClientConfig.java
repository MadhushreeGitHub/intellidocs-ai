package com.intellidocs.intellidocs_ai.config;

import org.springframework.boot.web.client.ClientHttpRequestFactories;
import org.springframework.boot.web.client.ClientHttpRequestFactorySettings;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.ClientHttpRequest;
import org.springframework.web.client.RestClient;

import java.time.Duration;

@Configuration
public class OllamaClientConfig {

    //Spring AI's Ollama client uses RestClient.Builder Bean.
    //Local LLM generation on CPU is slow, so we give it long timeouts.

    @Bean
    public RestClient.Builder ollamaRestClientBuilder() {
        ClientHttpRequestFactorySettings settings =
                ClientHttpRequestFactorySettings.DEFAULTS
                        .withConnectTimeout(Duration.ofSeconds(10))
                        .withReadTimeout(Duration.ofSeconds(180));
        return RestClient.builder()
                .requestFactory(ClientHttpRequestFactories.get(settings));
    }
}
