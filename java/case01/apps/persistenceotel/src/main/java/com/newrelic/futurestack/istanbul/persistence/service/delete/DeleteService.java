package com.newrelic.futurestack.istanbul.persistence.service.delete;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import com.newrelic.futurestack.istanbul.persistence.dtos.ResponseBase;
import com.newrelic.futurestack.istanbul.persistence.repository.PipelineDataRepository;
import com.newrelic.futurestack.istanbul.persistence.service.error.ErrorService;

import io.opentelemetry.instrumentation.annotations.WithSpan;

@Service
public class DeleteService {

  private final Logger logger = LoggerFactory.getLogger(DeleteService.class);

  @Autowired
  private PipelineDataRepository repository;

  @Autowired
  private ErrorService errorService;

  public ResponseEntity<ResponseBase<Boolean>> run(String error) {
    logger.info("Deleting pipeline datas...");

    try {
      // Generate error if given
      causeError(error);

      // Delete data
      deletePipelineDatas();

      // Create success response
      String message = "Pipeline datas are deleted successfully.";
      return createResponse(message, true, HttpStatus.OK);
    } catch (Exception e) {

      // Create fail response
      String message = "Pipeline datas are not deleted successfully.";
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
  private void deletePipelineDatas() {
    repository.deleteAll();
  }

  @WithSpan
  private ResponseEntity<ResponseBase<Boolean>> createResponse(
      String message,
      Boolean isSuccessful,
      HttpStatus statusCode) {
    return new ResponseEntity<ResponseBase<Boolean>>(
        new ResponseBase<Boolean>(
            message,
            isSuccessful),
        statusCode);
  }
}
