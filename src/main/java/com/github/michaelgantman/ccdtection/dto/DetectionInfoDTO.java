package com.github.michaelgantman.ccdtection.dto;

import java.util.UUID;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "detected_cc")
public class DetectionInfoDTO {

	@Id
	private UUID id;
	private String sender;
	private Long sentTime;

	public DetectionInfoDTO() {
		
	}
	
	protected DetectionInfoDTO(MessageDTO messageDTO) {
		setId(messageDTO.getId());
		setSender(messageDTO.getSender());
		setSentTime(messageDTO.getSentTime());
	}

	public UUID getId() {
		return id;
	}
	
	public void setId(UUID id) {
		this.id = id;
	}
	
	public String getSender() {
		return sender;
	}
	
	public void setSender(String sender) {
		this.sender = sender;
	}
	
	public Long getSentTime() {
		return sentTime;
	}
	
	public void setSentTime(Long sentTime) {
		this.sentTime = sentTime;
	}

}
