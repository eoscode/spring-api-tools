package com.eoscode.springapitools.sample.infrastructure.persistence.converters;

import com.eoscode.springapitools.sample.core.domain.model.State;
import com.eoscode.springapitools.sample.infrastructure.persistence.entities.StateEntity;
import com.eoscode.springapitools.sample.infrastructure.shared.RepositoryConverter;

import java.util.UUID;

public class StateRepositoryConverter implements RepositoryConverter<StateEntity, State> {

    @Override
    public StateEntity mapToTable(State persistenceObject) {
        return new StateEntity(UUID.fromString(persistenceObject.getId()),
                persistenceObject.getName(),
                null,
                persistenceObject.getUf(),
                persistenceObject.getDateOfFoundation(),
                persistenceObject.getAverageSalary());
    }

    @Override
    public State mapToEntity(StateEntity tableObject) {
        return new State(tableObject.getId().toString(),
                tableObject.getName(),
                tableObject.getUf(),
                tableObject.getDateOfFoundation(),
                tableObject.getAverageSalary());
    }
}
