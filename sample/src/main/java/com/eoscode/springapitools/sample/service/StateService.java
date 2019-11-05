package com.eoscode.springapitools.sample.service;

import com.eoscode.springapitools.sample.entity.State;
import com.eoscode.springapitools.sample.repository.StateRepository;
import com.eoscode.springapitools.service.AbstractService;
import org.springframework.stereotype.Service;

@Service
public class StateService extends AbstractService<StateRepository, State, String> {

    private final StateRepository stateRepository;

    public StateService(StateRepository stateRepository) {
        this.stateRepository = stateRepository;
    }

    @Override
    public StateRepository getRepository() {
        return stateRepository;
    }

}
