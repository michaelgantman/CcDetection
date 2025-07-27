package com.github.michaelgantman.ccdtection.bl;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.github.michaelgantman.ccdtection.exceptions.WriteMessageToQueueException;
import com.mgnt.lifecycle.management.httpclient.HttpClient;
import com.mgnt.lifecycle.management.httpclient.HttpClient.HttpMethod;
import com.mgnt.lifecycle.management.httpclient.HttpClientCommunicationException;
import com.mgnt.lifecycle.management.httpclient.ResponseHolder;
import com.mgnt.utils.JsonUtils;
import com.mgnt.utils.TextUtils;

import jakarta.annotation.PostConstruct;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class MessageSender {

	private static final Log logger = LogFactory.getLog(MessageSender.class);

	@Value("${activemq.messagesending.url}")
	private String messageSendingUrl;

	@Value("${spring.activemq.user}")
	private String username;

	@Value("${spring.activemq.password}")
	private String password;

	@Value("${activemq.queue.name}")
	private String queueName;

	private HttpClient httpClient = new HttpClient();

	private static final String MESSAGE_PREFIX_PART_ONE = "{" + "\"type\": \"exec\","
			+ "\"mbean\": \"org.apache.activemq:type=Broker,brokerName=localhost,destinationType=Queue,destinationName=";
	private static final String MESSAGE_PREFIX_PART_TWO = "\","
			+ "\"operation\": \"sendTextMessage(java.util.Map, java.lang.String, java.lang.String, java.lang.String)\","
			+ "\"arguments\": [{" + "\"JMSDeliveryMode\": \"2\"" + "}," + "\"";
	private static String MESSAGE_PREFIX;

	private static final String MESSAGE_SUFFIX = "\"," + "null," + "null]" + "}";

	@PostConstruct
	private void init() {
		httpClient.setConnectionUrl(messageSendingUrl);
		String usernameAndPassword = username + ":" + password;
		String auth = "Basic "
				+ Base64.getEncoder().encodeToString(usernameAndPassword.getBytes(StandardCharsets.UTF_8));
		httpClient.setRequestHeader("Authorization", auth).setContentType("application/json; charset=UTF-8");
		MESSAGE_PREFIX = MESSAGE_PREFIX_PART_ONE + queueName + MESSAGE_PREFIX_PART_TWO;
	}

	public void sendMessage(String message) {
		String body = MESSAGE_PREFIX + message + MESSAGE_SUFFIX;
		try {
			ResponseHolder<String> response = httpClient.sendHttpRequest(HttpMethod.POST, body);
			if(response.getResponseCode() == HttpServletResponse.SC_OK) {
				validateResponseFromQueue(response.getResponseContent());
				logger.info("Message was successfully sent to ActiveMQ");
			} else {
				logger.error("Error occurred while sending message to ActiveMQ: " + "response code: " 
						+ response.getResponseCode() + " " + response.getResponseMessage());
			}
		} catch(HttpClientCommunicationException hce) {
			logger.error("Error occurred while sending message to ActiveMQ:" + TextUtils.getStacktrace(hce));
		} catch(WriteMessageToQueueException wmtqe) {
			logger.error("Message rejected by ActiveMQ\nMessage Body: " + body + "\nException details:"+ TextUtils.getStacktrace(wmtqe));
		}
	}

	private void validateResponseFromQueue(String responseContent) throws WriteMessageToQueueException {
		try {
			Map<String, Object> responseMap = (Map<String, Object>) JsonUtils.readObjectFromJsonString(responseContent, Map.class);
			if (responseMap.containsKey("error") && responseMap.containsKey("status")
					&& HttpServletResponse.SC_OK != (int) responseMap.get("status")) {
				throw new WriteMessageToQueueException("Message was regected by ActiveMQ due to invalid format or security violation: " 
					+ responseContent);
			}
		} catch (IOException ioe) {
			throw new WriteMessageToQueueException("Response content from ActiveMQ is not valid JSON:" + responseContent, ioe);
		}
	}
}