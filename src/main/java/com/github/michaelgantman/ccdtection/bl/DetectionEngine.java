package com.github.michaelgantman.ccdtection.bl;

import java.util.List;
import java.util.Queue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.github.michaelgantman.ccdtection.dto.DetectionInfoDTO;
import com.github.michaelgantman.ccdtection.dto.SenderCountDTO;
import com.github.michaelgantman.ccdtection.exceptions.MaxHandlerCountViolationException;
import com.github.michaelgantman.ccdtection.web.service.StorageCommunicationService;
import com.mgnt.utils.JsonUtils;
import com.mgnt.utils.TextUtils;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;

@Component
public class DetectionEngine {
	
    private static final Log logger = LogFactory.getLog(DetectionEngine.class);
    
    private BlockingQueue<MessageHandler> handlerQueue = new LinkedBlockingQueue<>();
	private Queue<Boolean> removalHandlerRequestQueue = new ConcurrentLinkedQueue<>();
	private ExecutorService pool = Executors.newCachedThreadPool();
	private AtomicInteger handlerCount = new AtomicInteger(0);
	private static final Integer DEFAULT_MAX_HANDLER_COUNT = 100;
	private static final Integer DEFAULT_INITIAL_HANDLER_COUNT = 1;

	@Value("${initial.handler.count}")
	private Integer initialHandlerCount;
	
	@Value("${max.handler.count}")
	private Integer maxHandlerCount;
	
	@Resource
	private StorageCommunicationService storageCommunicationService;

    @JmsListener(destination = "${activemq.queue.name}")
    public void receive(String message) {
    	logger.info("Message received from queue: " + message);
    	MessageHandler messageHandler;
		try {
			messageHandler = handlerQueue.take();
			messageHandler.setMessage(message);
			pool.submit(messageHandler);
		} catch (InterruptedException ie) {
			logger.error("Unexpected exception occurred while reading message from the Queue" + TextUtils.getStacktrace(ie));
		}
    }

    public void addMessageHandler() throws MaxHandlerCountViolationException {
    	if(getHadlerCount() >= maxHandlerCount) {
    		throw new MaxHandlerCountViolationException("Attempt was made to increase number of handlers above max limit: " + maxHandlerCount);
    	}
    	handlerQueue.offer(new MessageHandler(this));
	}
    
    public boolean  returnHandlerBackToPool(MessageHandler messageHandler) {
    	messageHandler.setMessage(null);
    	return handlerQueue.offer(messageHandler);
    }
	
	public void requestHandlerRemoval() {
		removalHandlerRequestQueue.add(Boolean.TRUE);
	}
	
	public boolean isHandlerRemovalRequestPending() {
		Boolean result = removalHandlerRequestQueue.poll();
		if(result == null) {
			result = Boolean.FALSE;
		}
		return result;
	}
	
	public void writeDetectionInfoDtoToStorage(DetectionInfoDTO detectionInfoDTO) {
		storageCommunicationService.writeDetectionDoc(detectionInfoDTO);
	}
	
	public List<SenderCountDTO> readDetectionsFromStorageBetweenTimes(Long startTime, Long endTme) {
		List<SenderCountDTO> result = null;
		result = storageCommunicationService.readDetectionsBetweenTimes(startTime, endTme);
		return result;
	}
	
	public int incrementHandlerCount() {
		return handlerCount.incrementAndGet();
	}
	
	public int decrementHandlerCount() {
		return handlerCount.decrementAndGet();
	}
	
	public int getHadlerCount() {
		return handlerCount.get();
	}
	
	@PostConstruct
	private void init() {
		if(maxHandlerCount < 1 || maxHandlerCount > DEFAULT_MAX_HANDLER_COUNT) {
			maxHandlerCount = DEFAULT_INITIAL_HANDLER_COUNT;
		}
		if(initialHandlerCount < 1 || initialHandlerCount > maxHandlerCount) {
			initialHandlerCount = maxHandlerCount;
		}
		for(int i = 0; i < initialHandlerCount; i++) {
			try {
				addMessageHandler();
			} catch (MaxHandlerCountViolationException mhcve) {
				logger.error(TextUtils.getStacktrace(mhcve));
				break;
			}
		}
	}

}
