package com.newrelic.nrvsotel.persistence.service.create;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import com.newrelic.nrvsotel.persistence.dtos.ResponseBase;
import com.newrelic.nrvsotel.persistence.entity.PipelineData;
import com.newrelic.nrvsotel.persistence.repository.PipelineDataRepository;
import com.newrelic.nrvsotel.persistence.service.create.dtos.CreateRequestDto;
import com.newrelic.nrvsotel.persistence.service.error.ErrorService;

import io.opentelemetry.instrumentation.annotations.WithSpan;

@Service
public class CreateService {

  private final Logger logger = LoggerFactory.getLogger(CreateService.class);

  @Autowired
  private PipelineDataRepository repository;

  @Autowired
  private ErrorService errorService;

  public ResponseEntity<ResponseBase<Boolean>> run(
      String error, CreateRequestDto requestDto) {

    try {
      // Generate error if given
      causeError(error);

      // Store data
      storePipelineData(requestDto);

      // Create success response
      String message = "Pipeline data is stored successfully.";
      return createResponse(message, true, HttpStatus.OK);
    } catch (Exception e) {

      // Create fail response
      String message = "Unexpected error occured: " + e.getMessage();
      logger.error(message);
      return createResponse(message, false, HttpStatus.INTERNAL_SERVER_ERROR);
    }
  }

  @WithSpan
  private void causeError(
      String error) throws Exception {
    errorService.causeError(error);
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

  @WithSpan
  private ResponseEntity<ResponseBase<Boolean>> createResponse(
      String message,
      Boolean isValid,
      HttpStatus statusCode) {
    return new ResponseEntity<ResponseBase<Boolean>>(
        new ResponseBase<Boolean>(
            message,
            isValid),
        statusCode);
  }
}
