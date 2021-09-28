package com.eoscode.springapitools.sample.core.state.usecase;

import com.eoscode.springapitools.sample.core.state.State;
import com.eoscode.springapitools.sample.core.state.exception.StateAlreadyExistException;

public interface GetStateUserCase {

    State execute(String id) throws StateAlreadyExistException;

}
