package com.newrelic.futurestack.istanbul.proxy.service.list;

import java.util.Collections;
import java.util.List;

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
import com.newrelic.futurestack.istanbul.proxy.service.list.dtos.PipelineData;

@Service
public class ListService {

  private final Logger logger = LoggerFactory.getLogger(ListService.class);

  @Value(value = "${PERSISTENCE_SERVICE_ENDPOINT}")
  private String persistenceServiceEndpoint;
  
  @Autowired
  private RestTemplate restTemplate;

  public ResponseEntity<ResponseBase<List<PipelineData>>> run(String error) {
    logger.info("Making request to persistence service...");

    var response = makeRequestToPersistenceService(error);

    logger.info("Request to persistence service is performed.");
    return response;
  }

  private ResponseEntity<ResponseBase<List<PipelineData>>> makeRequestToPersistenceService(
      String error) {

    var url = persistenceServiceEndpoint + "/list";
    if (!error.isEmpty())
      url += "?error=" + error;

    var headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_JSON);
    headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));

    var entity = new HttpEntity<>(null, headers);
    return restTemplate.exchange(url, HttpMethod.GET, entity,
        new ParameterizedTypeReference<ResponseBase<List<PipelineData>>>() {
        });
  }
}
