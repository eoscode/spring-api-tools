package com.eoscode.springapitools.config;

import com.eoscode.springapitools.service.MessageResolver;
import com.eoscode.springapitools.service.MessageResolverService;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.ReloadableResourceBundleMessageSource;

import java.util.Locale;

@Configuration
public class MessageI18nConfiguration {
	@Bean
	public MessageSource messageSource() {
		ReloadableResourceBundleMessageSource messageSource = new ReloadableResourceBundleMessageSource();
		messageSource.setBasename("classpath:i18n/messages");
		messageSource.setAlwaysUseMessageFormat(false);
		messageSource.setUseCodeAsDefaultMessage(true);
		messageSource.setDefaultLocale(new Locale("pt", "BR"));

		return messageSource;
	}

	@Bean
	public MessageResolver messageResolver(MessageSource messageSource) {
		return new MessageResolverService(messageSource);
	}

}
