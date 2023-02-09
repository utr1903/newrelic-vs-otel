package com.newrelic.futurestack.istanbul.proxy;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.web.client.RestTemplate;

import com.newrelic.futurestack.istanbul.proxy.handler.RestTemplateResponseErrorHandler;

@SpringBootApplication
public class ProxyApplication {

	public static void main(String[] args) {
		SpringApplication.run(ProxyApplication.class, args);
	}

	@Bean
	public RestTemplate createRestTemplate() {
		return new RestTemplateBuilder()
				.errorHandler(new RestTemplateResponseErrorHandler())
				.build();
	}
}
