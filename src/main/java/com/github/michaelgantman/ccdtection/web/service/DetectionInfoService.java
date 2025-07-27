package com.github.michaelgantman.ccdtection.web.service;


import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestParam;

import com.github.michaelgantman.ccdtection.bl.DetectionEngine;
import com.github.michaelgantman.ccdtection.bl.MessageSender;
import com.github.michaelgantman.ccdtection.dto.MessageDTO;
import com.github.michaelgantman.ccdtection.dto.SenderCountDTO;
import com.github.michaelgantman.ccdtection.exceptions.MaxHandlerCountViolationException;
import com.mgnt.utils.JsonUtils;
import com.mgnt.utils.TextUtils;

import jakarta.annotation.Resource;

@Service
public class DetectionInfoService {

    private static final Log logger = LogFactory.getLog(DetectionInfoService.class);

    private static final String QUOTATION_TO_BE_ESCAPED = "\\\"";
    private static final String ESQAPED_QUOTATION = "\\\\\"";
	
	@Resource
	private MessageSender messageSender;
 
	@Resource
	private DetectionEngine detectionEngine;
	
	public void addMessageForHandling(MessageDTO message) {
		try {
			String escapedJson = JsonUtils.writeObjectToJsonString(message).replaceAll(QUOTATION_TO_BE_ESCAPED, ESQAPED_QUOTATION);
			messageSender.sendMessage(escapedJson);
		} catch (Exception e) {
			logger.error("Error occurred while Converting Message DTO to String:" + TextUtils.getStacktrace(e));
		}
	}
	
	public void addHandler() throws MaxHandlerCountViolationException {
		detectionEngine.addMessageHandler();
	}
	
	public void removeHandler() {
		detectionEngine.requestHandlerRemoval();
	}
	
	public int getHandlerCount() {
		return detectionEngine.getHadlerCount();
	}
	
	public List<SenderCountDTO> getDetections(@RequestParam Long timeFrom, @RequestParam Long timeTo) {
		return detectionEngine.readDetectionsFromStorageBetweenTimes(timeFrom, timeTo);
	}
}
