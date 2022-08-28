package com.eoscode.springapitools.sample.core.domain.repositories;

import com.eoscode.springapitools.sample.core.domain.shared.CrudRepository;
import com.eoscode.springapitools.sample.core.domain.model.State;

public interface IStateRepository extends CrudRepository<State, String> {
}
