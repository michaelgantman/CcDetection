package com.github.michaelgantman.ccdtection.config;

import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import com.mgnt.utils.TextUtils;

@Component
public class DynamicPropertiesListener {

	@EventListener
	public void onApplicationEvent(ContextRefreshedEvent event) {
		TextUtils.setRelevantPackage("com.github.michaelgantman.");
	}
}
