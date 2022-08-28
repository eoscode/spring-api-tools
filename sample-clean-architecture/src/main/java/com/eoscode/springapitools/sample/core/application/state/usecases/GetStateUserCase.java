package com.eoscode.springapitools.sample.core.application.state.usecases;

import com.eoscode.springapitools.sample.core.application.state.exception.StateAlreadyExistException;
import com.eoscode.springapitools.sample.core.domain.model.State;

public interface GetStateUserCase {

    State execute(String id) throws StateAlreadyExistException;

}
