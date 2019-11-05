package com.eoscode.springapitools.sample.repository;

import com.eoscode.springapitools.data.repository.Repository;
import com.eoscode.springapitools.sample.entity.State;

@org.springframework.stereotype.Repository
public interface StateRepository extends Repository<State, String> {
}
