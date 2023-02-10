package com.newrelic.futurestack.istanbul.proxy.controller;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.newrelic.futurestack.istanbul.proxy.dtos.ResponseBase;
import com.newrelic.futurestack.istanbul.proxy.service.create.CreateService;
import com.newrelic.futurestack.istanbul.proxy.service.create.dtos.CreateRequestDto;
import com.newrelic.futurestack.istanbul.proxy.service.delete.DeleteService;
import com.newrelic.futurestack.istanbul.proxy.service.list.ListService;
import com.newrelic.futurestack.istanbul.proxy.service.list.dtos.PipelineData;

@RestController
@RequestMapping("proxy")
public class ProxyController {

	private final Logger logger = LoggerFactory.getLogger(ProxyController.class);

	@Autowired
	private CreateService createService;

	@Autowired
	private ListService listService;

	@Autowired
	private DeleteService deleteService;

	@GetMapping("ping")
	public ResponseEntity<String> ping() {
		logger.info("PONG");
		var response = new ResponseEntity<>("pong", HttpStatus.OK);
		return response;
	}

	@PostMapping("create")
	public ResponseEntity<ResponseBase<Boolean>> create(
			@RequestParam(name = "error", defaultValue = "", required = false) String error,
			@RequestBody CreateRequestDto requestDto) {
		logger.info("Create method is triggered...");

		var response = createService.run(requestDto);

		logger.info("Create method is executed.");

		return response;
	}

	@GetMapping("list")
	public ResponseEntity<ResponseBase<List<PipelineData>>> list(
			@RequestParam(name = "error", defaultValue = "", required = false) String error) {
		logger.info("List method is triggered...");

		var response = listService.run(error);

		logger.info("List method is executed.");

		return response;
	}

	@DeleteMapping("delete")
	public ResponseEntity<ResponseBase<Boolean>> delete(
			@RequestParam(name = "error", defaultValue = "", required = false) String error) {
		logger.info("Delete method is triggered...");

		var response = deleteService.run(error);

		logger.info("Delete method is executed.");

		return response;
	}
}
