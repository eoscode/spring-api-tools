package com.eoscode.springapitools.sample.core.state.usecase;

import com.eoscode.springapitools.sample.core.state.State;

import java.util.List;

public interface GetAllStateUserCase {

    List<State> execute();

}
