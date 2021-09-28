package com.eoscode.springapitools.sample.infrastructure.configuration;

import com.eoscode.springapitools.sample.core.state.ports.StateRepository;
import com.eoscode.springapitools.sample.core.state.usecase.GetAllStateUserCase;
import com.eoscode.springapitools.sample.core.state.usecase.GetStateUserCase;
import com.eoscode.springapitools.sample.core.state.usecase.impl.GetAllStateUserCaseImpl;
import com.eoscode.springapitools.sample.core.state.usecase.impl.GetStateUserCaseImpl;
import com.eoscode.springapitools.sample.infrastructure.persistence.converters.StateRepositoryConverter;
import com.eoscode.springapitools.sample.infrastructure.persistence.impl.StateRepositoryImpl;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class StateConfiguration {

    private final ApplicationContext applicationContext;
    private final com.eoscode.springapitools.sample.infrastructure.persistence.repositories.StateRepository stateRepository;

    public StateConfiguration(ApplicationContext applicationContext, com.eoscode.springapitools.sample.infrastructure.persistence.repositories.StateRepository stateRepository) {
        this.applicationContext = applicationContext;
        this.stateRepository = stateRepository;
    }

    @Bean
    public StateRepositoryConverter createStateRepositoryConverter() {
        return new StateRepositoryConverter();
    }

    @Bean
    public StateRepository createStateRepositoryServiceImpl() {
        return new StateRepositoryImpl(applicationContext,
                createStateRepositoryConverter(),
                stateRepository);
    }

    @Bean
    public GetStateUserCase createGetStateUseCase() {
        return new GetStateUserCaseImpl(createStateRepositoryServiceImpl());
    }

    @Bean
    public GetAllStateUserCase createGetAllStateUseCase() {
        return new GetAllStateUserCaseImpl(createStateRepositoryServiceImpl());
    }

}
