package com.newrelic.futurestack.istanbul.persistence.service.list;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import com.newrelic.futurestack.istanbul.persistence.dtos.ResponseBase;
import com.newrelic.futurestack.istanbul.persistence.entity.PipelineData;
import com.newrelic.futurestack.istanbul.persistence.repository.PipelineDataRepository;
import com.newrelic.futurestack.istanbul.persistence.service.error.ErrorService;

@Service
public class ListService {

  private final Logger logger = LoggerFactory.getLogger(ListService.class);

  @Autowired
  private PipelineDataRepository repository;

  @Autowired
  private ErrorService errorService;

  public ResponseEntity<ResponseBase<List<PipelineData>>> run(String error) {
    logger.info("Retrieving pipeline datas...");

    try {
      // Generate error if given
      causeError(error);

      // Get data
      var pipelineDatas = getPipelineDatas();

      // Create success response
      String message = "Pipeline datas are retrieved successfully.";
      return createResponse(message, pipelineDatas, HttpStatus.OK);
    } catch (Exception e) {

      // Create fail response
      String message = "Pipeline datas are not retrieved successfully.";
      logger.error(message);
      return createResponse(message, null, HttpStatus.INTERNAL_SERVER_ERROR);
    }
  }

  private void causeError(
      String error) throws Exception {
    errorService.causeError(error);
  }

  private List<PipelineData> getPipelineDatas() {
    return repository.findAll();
  }

  private ResponseEntity<ResponseBase<List<PipelineData>>> createResponse(
      String message,
      List<PipelineData> pipelineDatas,
      HttpStatus statusCode) {
    return new ResponseEntity<ResponseBase<List<PipelineData>>>(
        new ResponseBase<List<PipelineData>>(
            message,
            pipelineDatas),
        statusCode);
  }
}
