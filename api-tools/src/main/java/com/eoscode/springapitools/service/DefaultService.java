package com.eoscode.springapitools.service;

import org.springframework.context.ApplicationContext;

import java.lang.reflect.Type;

@SuppressWarnings("unchecked")
public class DefaultService<Repository extends com.eoscode.springapitools.data.repository.Repository<Entity, ID>, Entity, ID>
        extends AbstractService<Repository, Entity, ID> {

    private Repository repository;

    public DefaultService(ApplicationContext applicationContext, Type repositoryType, Type entityType, Type identifierType) {
        super(repositoryType, entityType, identifierType);

        Class<Repository> repositoryClass = (Class<Repository>) repositoryType;
        this.repository = applicationContext.getBean(repositoryClass);
    }

    @Override
    public Repository getRepository() {
        return repository;
    }

}
