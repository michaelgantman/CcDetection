package com.github.michaelgantman.ccdtection.dto;

public class SenderCountDTO {
    private String sender;
    private long count;

    public SenderCountDTO() {
    }
    
    public SenderCountDTO(String sender, long count) {
        this.sender = sender;
        this.count = count;
    }

	public String getSender() {
		return sender;
	}

	public void setSender(String sender) {
		this.sender = sender;
	}

	public long getCount() {
		return count;
	}

	public void setCount(long count) {
		this.count = count;
	}
}
