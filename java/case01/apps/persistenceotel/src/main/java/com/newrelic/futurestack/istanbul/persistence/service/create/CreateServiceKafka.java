package com.newrelic.futurestack.istanbul.persistence.service.create;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import com.google.gson.Gson;
import com.newrelic.futurestack.istanbul.persistence.entity.PipelineData;
import com.newrelic.futurestack.istanbul.persistence.repository.PipelineDataRepository;
import com.newrelic.futurestack.istanbul.persistence.service.create.dtos.CreateRequestDto;

import io.opentelemetry.instrumentation.annotations.WithSpan;

@Service
public class CreateServiceKafka {

  private final Logger logger = LoggerFactory.getLogger(CreateServiceKafka.class);

  @Autowired
  private PipelineDataRepository repository;

  @WithSpan
  @KafkaListener(topics = "#{'${KAFKA_TOPIC}'}", groupId = "#{'${KAFKA_CONSUMER_GROUP_ID}'}")
  public void createPipelineData(
      String message) {
    logger.info(message);

    try {
      // Parse message
      var requestDto = parseMessage(message);

      // Store message
      storePipelineData(requestDto);
    } catch (Exception e) {
      logger.error("Unexpected error occured: ", e.getMessage());
    }
  }

  @WithSpan
  private CreateRequestDto parseMessage(
      String message) throws Exception {
    return new Gson().fromJson(message, CreateRequestDto.class);
  }

  @WithSpan
  private void storePipelineData(CreateRequestDto requestDto) {
    logger.info("Storing pipeline data...");

    var pipelineData = new PipelineData();
    pipelineData.setName(requestDto.getName());
    pipelineData.setValue(requestDto.getValue());
    repository.save(pipelineData);

    logger.info("Pipeline data is stored successfully.");
  }
}
