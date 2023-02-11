package com.newrelic.nrvsotel.simulator.service;

import java.util.Collections;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
public class Simulator implements CommandLineRunner {

  private final Logger logger = LoggerFactory.getLogger(Simulator.class);

  @Value(value = "${ENDPOINT_NEWRELIC}")
  private String endpointNewrelic;

  @Value(value = "${ENDPOINT_OTEL}")
  private String endpointOtel;

  @Value(value = "${SIMULATION_INTERVAL}")
  private int simulationInterval;

  @Autowired
  private RestTemplate restTemplate;

  public Simulator() {
  }

  public void run(String... args) {

    // Set simulation interval for each endpoint
    var simulationIntervalCreate = simulationInterval;
    var simulationIntervalList = simulationInterval * 2;
    var simulationIntervalDelete = simulationInterval * 4;

    // Create scheduler
    var scheduler = Executors.newScheduledThreadPool(6);

    // Simulate New Relic services
    scheduler.scheduleAtFixedRate(() -> create(false), 0, simulationIntervalCreate,
        TimeUnit.MILLISECONDS);
    scheduler.scheduleAtFixedRate(() -> list(false), 0, simulationIntervalList,
        TimeUnit.MILLISECONDS);
    scheduler.scheduleAtFixedRate(() -> delete(false), 0, simulationIntervalDelete,
        TimeUnit.MILLISECONDS);

    // Simulate OTel services
    scheduler.scheduleAtFixedRate(() -> create(true), simulationInterval, simulationIntervalCreate,
        TimeUnit.MILLISECONDS);
    scheduler.scheduleAtFixedRate(() -> list(true), simulationInterval, simulationIntervalList,
        TimeUnit.MILLISECONDS);
    scheduler.scheduleAtFixedRate(() -> delete(true), simulationInterval, simulationIntervalDelete,
        TimeUnit.MILLISECONDS);
  }

  private void create(
      boolean isOtel) {

    var url = setEndpointUrl(isOtel) + "/create";
    var dto = "{\"name\":\"name\",\"value\":\"value\"}";

    var headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_JSON);
    headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));

    var entity = new HttpEntity<>(dto, headers);
    var response = restTemplate.exchange(url, HttpMethod.POST, entity, String.class);

    logger.info(url + response);
  }

  private void list(
      boolean isOtel) {

    var url = setEndpointUrl(isOtel) + "/list";

    var headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_JSON);
    headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));

    var entity = new HttpEntity<>(null, headers);
    var response = restTemplate.exchange(url, HttpMethod.GET, entity, String.class);

    logger.info(url + response);
  }

  private void delete(
      boolean isOtel) {

    var url = setEndpointUrl(isOtel) + "/delete";

    var headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_JSON);
    headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));

    var entity = new HttpEntity<>(null, headers);
    var response = restTemplate.exchange(url, HttpMethod.DELETE, entity, String.class);

    logger.info(url + response);
  }

  private String setEndpointUrl(
      boolean isOtel) {
    return isOtel ? endpointOtel : endpointNewrelic;
  }
}
