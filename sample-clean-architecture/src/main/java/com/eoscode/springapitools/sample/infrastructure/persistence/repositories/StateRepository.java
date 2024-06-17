package com.eoscode.springapitools.sample.infrastructure.persistence.repositories;

import com.eoscode.springapitools.data.repository.Repository;
import com.eoscode.springapitools.sample.infrastructure.persistence.entities.StateEntity;

import java.util.UUID;

@org.springframework.stereotype.Repository
public interface StateRepository extends Repository<StateEntity, UUID> {
}
