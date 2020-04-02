package com.eoscode.springapitools.sample.config;

import com.eoscode.springapitools.config.Jackson2HttpMessageConfiguration;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.hibernate5.Hibernate5Module;
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
        Hibernate5Module hibernate5Module = new Hibernate5Module();
        objectMapper.registerModule(hibernate5Module);

        return mapping;
    }
}
