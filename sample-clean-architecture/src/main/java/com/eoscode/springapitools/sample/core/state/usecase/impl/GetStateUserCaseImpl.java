package com.eoscode.springapitools.sample.core.state.usecase.impl;

import com.eoscode.springapitools.sample.core.state.State;
import com.eoscode.springapitools.sample.core.state.exception.StateAlreadyExistException;
import com.eoscode.springapitools.sample.core.state.ports.StateRepository;
import com.eoscode.springapitools.sample.core.state.usecase.GetStateUserCase;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public class GetStateUserCaseImpl implements GetStateUserCase {

    private final StateRepository stateRepositoryService;

    @Override
    public State execute(String id) throws StateAlreadyExistException {
        return stateRepositoryService.findById(id);
    }
}
