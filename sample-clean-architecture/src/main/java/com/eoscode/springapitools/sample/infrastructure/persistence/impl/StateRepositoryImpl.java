package com.eoscode.springapitools.sample.infrastructure.persistence.impl;

import com.eoscode.springapitools.sample.core.domain.model.State;
import com.eoscode.springapitools.sample.core.domain.repositories.IStateRepository;
import com.eoscode.springapitools.sample.infrastructure.persistence.converters.StateRepositoryConverter;
import com.eoscode.springapitools.sample.infrastructure.persistence.entities.StateEntity;
import com.eoscode.springapitools.sample.infrastructure.persistence.repositories.StateRepository;
import com.eoscode.springapitools.service.RepositoryService;
import org.springframework.context.ApplicationContext;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public class StateRepositoryImpl implements IStateRepository {

    private final RepositoryService<StateRepository, StateEntity, UUID> repositoryService;
    private final StateRepositoryConverter stateRepositoryConverter;

    public StateRepositoryImpl(ApplicationContext applicationContext,
                               StateRepositoryConverter stateRepositoryConverter,
                               StateRepository stateRepository) {
        this.stateRepositoryConverter = stateRepositoryConverter;
        //repositoryService = new RepositoryService<>(applicationContext, StateRepository.class, StateEntity.class, String.class);
        repositoryService = new RepositoryService<>(applicationContext, stateRepository);
    }

    @Override
    public State findById(String id) {
        return stateRepositoryConverter.mapToEntity(repositoryService.findById(UUID.fromString(id)));
    }

    @Override
    public State save(State state) {
       StateEntity stateEntity = repositoryService.save(stateRepositoryConverter.mapToTable(state));
       return stateRepositoryConverter.mapToEntity(stateEntity);
    }

    @Override
    public State update(State state) {
        StateEntity stateEntity = repositoryService.update(stateRepositoryConverter.mapToTable(state));
        return stateRepositoryConverter.mapToEntity(stateEntity);
    }

    @Override
    public void delete(State state) {
        repositoryService.delete(stateRepositoryConverter.mapToTable(state));
    }

    @Override
    public List<State> getAll() {
        return repositoryService.findAll()
                .stream()
                .map(stateRepositoryConverter::mapToEntity)
                .collect(Collectors.toList());
    }
}
