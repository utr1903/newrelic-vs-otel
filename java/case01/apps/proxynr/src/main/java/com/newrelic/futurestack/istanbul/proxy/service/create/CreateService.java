package com.newrelic.futurestack.istanbul.proxy.service.create;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import com.google.gson.Gson;
import com.newrelic.api.agent.Trace;
import com.newrelic.futurestack.istanbul.proxy.dtos.ResponseBase;
import com.newrelic.futurestack.istanbul.proxy.service.create.dtos.CreateRequestDto;

@Service
public class CreateService {

  private final Logger logger = LoggerFactory.getLogger(CreateService.class);

  @Value(value = "${KAFKA_TOPIC}")
  private String kafkaTopic;

  @Autowired
  private KafkaTemplate<String, String> kafkaTemplate;

  public ResponseEntity<ResponseBase<Boolean>> run(
      CreateRequestDto requestDto) {
    logger.info("Publishing to Kafka...");

    var response = publishToKafka(requestDto);

    logger.info("Request is published to Kafka.");
    return response;
  }

  @Trace(dispatcher = true)
  private ResponseEntity<ResponseBase<Boolean>> publishToKafka(
      CreateRequestDto requestDto) {

    // Publish
    kafkaTemplate.send(kafkaTopic, new Gson().toJson(requestDto));

    // Create response
    return createResponse(
        "Creation request is processed successfully.",
        true,
        HttpStatus.ACCEPTED);
  }

  private ResponseEntity<ResponseBase<Boolean>> createResponse(
      String message,
      Boolean hasSucceeded,
      HttpStatus statusCode) {
    return new ResponseEntity<ResponseBase<Boolean>>(
        new ResponseBase<Boolean>(
            message,
            hasSucceeded),
        statusCode);
  }
}
