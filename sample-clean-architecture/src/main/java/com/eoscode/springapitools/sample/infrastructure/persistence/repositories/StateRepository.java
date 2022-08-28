package com.eoscode.springapitools.sample.infrastructure.persistence.repositories;

import com.eoscode.springapitools.data.repository.Repository;
import com.eoscode.springapitools.sample.infrastructure.persistence.entities.StateEntity;

@org.springframework.stereotype.Repository
public interface StateRepository extends Repository<StateEntity, String> {
}
