package com.eoscode.springapitools.sample.configuration;

import com.eoscode.springapitools.config.Jackson2HttpMessageConfiguration;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.hibernate5.jakarta.Hibernate5JakartaModule;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;

@Configuration
public class JacksonConfiguration extends Jackson2HttpMessageConfiguration {

    @Bean
    @Override
    public MappingJackson2HttpMessageConverter mappingJackson2HttpMessageConverter() {
        MappingJackson2HttpMessageConverter mapping = super.mappingJackson2HttpMessageConverter();

        ObjectMapper objectMapper = mapping.getObjectMapper();
        Hibernate5JakartaModule hibernate5Module = new Hibernate5JakartaModule();
        objectMapper.registerModule(hibernate5Module);

        return mapping;
    }
}
