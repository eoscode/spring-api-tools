package com.eoscode.springapitools.sample.core.application.state.usecases.impl;

import com.eoscode.springapitools.sample.core.application.state.exception.StateAlreadyExistException;
import com.eoscode.springapitools.sample.core.domain.repositories.IStateRepository;
import com.eoscode.springapitools.sample.core.application.state.usecases.GetStateUserCase;
import com.eoscode.springapitools.sample.core.domain.model.State;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public class GetStateUserCaseImpl implements GetStateUserCase {

    private final IStateRepository stateRepositoryService;

    @Override
    public State execute(String id) throws StateAlreadyExistException {
        return stateRepositoryService.findById(id);
    }
}
