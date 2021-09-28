package com.eoscode.springapitools.sample.core.state.usecase.impl;

import com.eoscode.springapitools.sample.core.state.State;
import com.eoscode.springapitools.sample.core.state.ports.StateRepository;
import com.eoscode.springapitools.sample.core.state.usecase.GetAllStateUserCase;
import lombok.AllArgsConstructor;

import java.util.List;

@AllArgsConstructor
public class GetAllStateUserCaseImpl implements GetAllStateUserCase {

    private final StateRepository stateRepositoryService;

    @Override
    public List<State> execute() {
        return stateRepositoryService.getAll();
    }

}
