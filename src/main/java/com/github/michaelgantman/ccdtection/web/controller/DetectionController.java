package com.github.michaelgantman.ccdtection.web.controller;


import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.github.michaelgantman.ccdtection.dto.MessageDTO;
import com.github.michaelgantman.ccdtection.dto.SenderCountDTO;
import com.github.michaelgantman.ccdtection.exceptions.MaxHandlerCountViolationException;
import com.github.michaelgantman.ccdtection.web.service.DetectionInfoService;
import com.mgnt.utils.TextUtils;

import jakarta.annotation.Resource;

@RestController
public class DetectionController {

	private static final Log logger = LogFactory.getLog(DetectionController.class);

	@Resource
	private DetectionInfoService detectionService;

	@PostMapping(value = "/message", consumes = "application/json; charset=UTF-8")
	public ResponseEntity<String> addMessage(@RequestBody MessageDTO message) {
		detectionService.addMessageForHandling(message);
		return ResponseEntity.ok("Message added for handling");
	}
	
	@GetMapping("/handler/add")
	public ResponseEntity<String> addHandler() {
		ResponseEntity<String> result = ResponseEntity.ok("Message Handler added");
		try {
			detectionService.addHandler();
		} catch (MaxHandlerCountViolationException mhcve) {
			logger.error(TextUtils.getStacktrace(mhcve));
			result = new ResponseEntity<>(mhcve.getMessage(), HttpStatusCode.valueOf(403));
		}
		return result;
	}

	@GetMapping("/handler/remove")
	public ResponseEntity<String> removeHandler() {
		detectionService.removeHandler();
		return ResponseEntity.ok("Request to terminate handler is sent");
	}

	@GetMapping("/handler/count")
	public ResponseEntity<String> getHandlerCount() {
		int count = detectionService.getHandlerCount();
		return ResponseEntity.ok("Current handler count: " + count);
	}
	
	@GetMapping("/detections")
	public ResponseEntity<List<SenderCountDTO>> getDetections(@RequestParam Long timeFrom, @RequestParam Long timeTo) {
		List<SenderCountDTO> result;
		result = detectionService.getDetections(timeFrom, timeTo);
		return ResponseEntity.ok(result);
	}
}
