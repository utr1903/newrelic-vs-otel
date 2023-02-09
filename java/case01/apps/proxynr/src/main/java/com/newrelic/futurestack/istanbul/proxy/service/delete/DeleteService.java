package com.newrelic.futurestack.istanbul.proxy.service.delete;

import java.util.Collections;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.newrelic.futurestack.istanbul.proxy.dtos.ResponseBase;

@Service
public class DeleteService {

  private final Logger logger = LoggerFactory.getLogger(DeleteService.class);

  @Value(value = "${PERSISTENCE_SERVICE_ENDPOINT}")
  private String persistenceServiceEndpoint;

  @Autowired
  private RestTemplate restTemplate;

  public ResponseEntity<ResponseBase<Boolean>> run(String error) {
    logger.info("Making request to persistence service...");

    var response = makeRequestToPersistenceService(error);

    logger.info("Request to persistence service is performed.");
    return response;
  }

  private ResponseEntity<ResponseBase<Boolean>> makeRequestToPersistenceService(
      String error) {

    var url = persistenceServiceEndpoint + "/delete";
    if (!error.isEmpty())
      url += "?error=" + error;

    var headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_JSON);
    headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));

    var entity = new HttpEntity<>(null, headers);
    return restTemplate.exchange(url, HttpMethod.DELETE, entity,
        new ParameterizedTypeReference<ResponseBase<Boolean>>() {
        });
  }
}
