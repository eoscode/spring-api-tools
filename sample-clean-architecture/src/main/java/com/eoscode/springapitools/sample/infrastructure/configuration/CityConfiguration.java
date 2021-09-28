package com.eoscode.springapitools.sample.infrastructure.configuration;

import com.eoscode.springapitools.sample.core.city.ports.CityRepository;
import com.eoscode.springapitools.sample.infrastructure.persistence.impl.CityRepositoryImpl;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class CityConfiguration {

    @Bean
    public CityRepository createCityRepositoryService(com.eoscode.springapitools.sample.infrastructure.persistence.repositories.CityRepository cityRepository) {
        return new CityRepositoryImpl(cityRepository);
    }

}
