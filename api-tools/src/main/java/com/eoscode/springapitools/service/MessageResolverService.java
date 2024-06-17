package com.eoscode.springapitools.service;

import org.springframework.context.MessageSource;

import java.util.Locale;

public class MessageResolverService implements MessageResolver {
	private final MessageSource messageSource;

	public MessageResolverService(MessageSource messageSource) {
		this.messageSource = messageSource;
	}

	@Override
	public String getMessage(String messageId) {
		return messageSource.getMessage(messageId, null, Locale.getDefault());
	}

	@Override
	public String getMessage(String messageId, Object[] args) {
		return messageSource.getMessage(messageId, args, Locale.getDefault());
	}

}
