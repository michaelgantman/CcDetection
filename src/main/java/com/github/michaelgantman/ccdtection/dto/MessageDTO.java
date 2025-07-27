package com.github.michaelgantman.ccdtection.dto;

import java.util.List;

public class MessageDTO extends DetectionInfoDTO{
	private List<String> recipients;
	private String subject;
	private String body;
	
		public List<String> getRecipients() {
		return recipients;
	}
	
	public void setRecipients(List<String> recepients) {
		this.recipients = recepients;
	}
	
	public String getSubject() {
		return subject;
	}
	
	public void setSubject(String subject) {
		this.subject = subject;
	}
	
	public String getBody() {
		return body;
	}
	
	public void setBody(String body) {
		this.body = body;
	}
	
	public DetectionInfoDTO extractDetectionDetectionInfo() {
		return new DetectionInfoDTO(this);
	}
}
