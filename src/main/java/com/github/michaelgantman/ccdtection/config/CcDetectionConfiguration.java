package com.github.michaelgantman.ccdtection.config;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.jms.DefaultJmsListenerContainerFactoryConfigurer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jms.annotation.EnableJms;
import org.springframework.jms.config.DefaultJmsListenerContainerFactory;

import jakarta.jms.ConnectionFactory;

@Configuration
@EnableJms
public class CcDetectionConfiguration {

	@Value("${spring.activemq.user}")
	private String username;
	
	@Value("${spring.activemq.password}")
	private String password;
	@Bean
	public ConnectionFactory connectionFactory() {
	    ActiveMQConnectionFactory factory = new ActiveMQConnectionFactory("tcp://localhost:61616");
	    factory.setUserName(username);
	    factory.setPassword(password);
	    return (ConnectionFactory)factory;
	}
	
	  @Bean
	  public DefaultJmsListenerContainerFactory jmsListenerContainerFactory(
	      ConnectionFactory connectionFactory,
	      DefaultJmsListenerContainerFactoryConfigurer configurer) {
	    DefaultJmsListenerContainerFactory factory = new DefaultJmsListenerContainerFactory();
	    configurer.configure(factory, connectionFactory);
	    return factory;
	  }
}
