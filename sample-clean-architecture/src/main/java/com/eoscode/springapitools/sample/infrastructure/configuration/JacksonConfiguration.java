<<<<<<<< HEAD:sample-clean-architecture/src/main/java/com/eoscode/springapitools/sample/infrastructure/configuration/JacksonConfiguration.java
package com.eoscode.springapitools.sample.infrastructure.configuration;
========
package com.eoscode.springapitools.sample.configuration;
>>>>>>>> refs/remotes/origin/master:sample/src/main/java/com/eoscode/springapitools/sample/configuration/JacksonConfiguration.java

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
