package com.newrelic.nrvsotel.simulator;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.web.client.RestTemplate;

import com.newrelic.nrvsotel.simulator.handler.RestTemplateResponseErrorHandler;

@SpringBootApplication
public class SimulatorApplication {

	public static void main(String[] args) {

		SpringApplication.run(SimulatorApplication.class, args);
	}

	@Bean
	public RestTemplate createRestTemplate() {
		return new RestTemplateBuilder()
				.errorHandler(new RestTemplateResponseErrorHandler())
				.build();
	}
}
