package com.eoscode.springapitools.sample.core.state.ports;

import com.eoscode.springapitools.sample.core.shared.CrudRepository;
import com.eoscode.springapitools.sample.core.state.State;

public interface StateRepository extends CrudRepository<State, String> {
}
