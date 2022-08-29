package com.eoscode.springapitools.service;

import org.springframework.context.ApplicationContext;

import java.lang.reflect.Type;

public class RepositoryService<Repository extends com.eoscode.springapitools.data.repository.Repository<Entity, ID>, Entity, ID>
        extends AbstractService<Repository, Entity, ID> {

    public RepositoryService(ApplicationContext applicationContext, Repository repository) {
        super(applicationContext, repository);
    }

    public RepositoryService(ApplicationContext applicationContext, Type repositoryType, Type entityType, Type identifierType) {
        super(applicationContext, repositoryType, entityType, identifierType);
    }

}
