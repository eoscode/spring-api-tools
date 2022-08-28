package com.eoscode.springapitools.sample.core.application.state.usecases;

import com.eoscode.springapitools.sample.core.domain.model.State;

import java.util.List;

public interface GetAllStateUserCase {

    List<State> execute();

}
