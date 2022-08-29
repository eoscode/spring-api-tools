package com.eoscode.springapitools.sample.core.application.state.usecases.impl;

import com.eoscode.springapitools.sample.core.domain.repositories.IStateRepository;
import com.eoscode.springapitools.sample.core.domain.model.State;
import com.eoscode.springapitools.sample.core.application.state.usecases.GetAllStateUserCase;
import lombok.AllArgsConstructor;

import java.util.List;

@AllArgsConstructor
public class GetAllStateUserCaseImpl implements GetAllStateUserCase {

    private final IStateRepository stateRepositoryService;

    @Override
    public List<State> execute() {
        return stateRepositoryService.getAll();
    }

}
