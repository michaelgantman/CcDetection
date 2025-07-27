package com.github.michaelgantman.ccdtection.bl;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicLong;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.github.michaelgantman.ccdtection.dto.DetectionInfoDTO;
import com.github.michaelgantman.ccdtection.dto.MessageDTO;
import com.mgnt.utils.JsonUtils;
import com.mgnt.utils.TextUtils;

public class MessageHandler implements Runnable {

	private static final AtomicLong ID_GENERATOR = new AtomicLong(0L);
	private static final Log logger = LogFactory.getLog(MessageHandler.class);

	private final Long id;
	private DetectionEngine detectionEngine;
	private String message = null;

    // Regex pattern for credit card number (with optional space/dash separators)
    private static final String REGEX = "\\b(?:4[0-9]{3}([ -]?)[0-9]{4}\\1[0-9]{4}\\1[0-9]{4}" +
                   "|5[1-5][0-9]{2}([ -]?)[0-9]{4}\\2[0-9]{4}\\2[0-9]{4}" +
                   "|3[47][0-9]{2}([ -]?)[0-9]{6}\\3[0-9]{5}" +
                   "|6(?:011|5[0-9]{2})([ -]?)[0-9]{4}\\4[0-9]{4}\\4[0-9]{4})\\b";

    private static final Pattern CC_PATTERN = Pattern.compile(REGEX);

    public MessageHandler(DetectionEngine detectionEngine) {
		id = ID_GENERATOR.incrementAndGet();
		this.detectionEngine = detectionEngine;
		detectionEngine.incrementHandlerCount();
		logger.info("Message Handler id " + id + " is initialized");
	}

	@Override
	public void run() {
		if (message != null) {
			logger.info("Message Handler id " + id + " handling message: " + message);
			try {
				MessageDTO messageDTO = JsonUtils.readObjectFromJsonString(message, MessageDTO.class);
			if(hasCCMatch(messageDTO)) {
				handleMatch(messageDTO);
			}
		} catch (IOException ioe) {
			logger.warn("Error occurred while parsing message:" + TextUtils.getStacktrace(ioe));
		}
		} else {
			logger.error("Handler id " + id + ": No Message to handle. Message received is null");
		}
		completeTask();
	}

	private void handleMatch(MessageDTO messageDTO) {
		logger.info("Credit Card MATCH found for sender: " + messageDTO.getSender() + " at time: " + messageDTO.getSentTime());
		detectionEngine.writeDetectionInfoDtoToStorage(messageDTO.extractDetectionDetectionInfo());
	}

	private boolean hasCCMatch(MessageDTO messageDTO) {
		boolean result = false;
		if(StringUtils.isNotBlank(messageDTO.getSubject())) {
			result = doMatch(CC_PATTERN.matcher(messageDTO.getSubject()));
		}
		if(!result && StringUtils.isNotBlank(messageDTO.getBody())) {
			result = doMatch(CC_PATTERN.matcher(messageDTO.getBody()));
		}
		return result;
	}

	private boolean doMatch(Matcher matcher) {
        boolean found = false;
        while (matcher.find()) {
            String rawMatch = matcher.group();
            String digitsOnly = rawMatch.replaceAll("[^0-9]", ""); // remove spaces/dashes

            if (isValidLuhn(digitsOnly)) {
                found = true;
                break;
            }
        }
		return found;
	}

    // Luhn Algorithm implementation
    public static boolean isValidLuhn(String number) {
        int sum = 0;
        boolean alternate = false;

        for (int i = number.length() - 1; i >= 0; i--) {
            int n = Character.getNumericValue(number.charAt(i));
            if (alternate) {
                n *= 2;
                if (n > 9) {
                    n -= 9;
                }
            }
            sum += n;
            alternate = !alternate;
        }

        return sum % 10 == 0;
    }

    private void completeTask() {
		if (!detectionEngine.isHandlerRemovalRequestPending()) {
			detectionEngine.returnHandlerBackToPool(this);
			logger.info("Handler id " + id + " returned to pool of available handlers");
		} else {
			logger.info("Handler id " + id + " is terminated");
			detectionEngine.decrementHandlerCount();
		}
	}

	public void setMessage(String message) {
		this.message = message;
	}
}
