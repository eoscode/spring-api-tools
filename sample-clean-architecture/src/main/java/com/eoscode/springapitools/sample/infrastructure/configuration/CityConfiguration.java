package com.eoscode.springapitools.sample.infrastructure.configuration;

import com.eoscode.springapitools.sample.core.domain.repositories.ICityRepository;
import com.eoscode.springapitools.sample.infrastructure.persistence.converters.CityRepositoryConverter;
import com.eoscode.springapitools.sample.infrastructure.persistence.impl.CityRepositoryImpl;
import com.eoscode.springapitools.sample.infrastructure.persistence.repositories.CityRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class CityConfiguration {

    @Bean
    public CityRepositoryConverter createCityRepositoryConverter() {
        return new CityRepositoryConverter();
    }

    @Bean
    public ICityRepository createCityRepositoryService(CityRepository cityRepository,
                                                       CityRepositoryConverter cityRepositoryConverter) {
        return new CityRepositoryImpl(cityRepository, cityRepositoryConverter);
    }

}
