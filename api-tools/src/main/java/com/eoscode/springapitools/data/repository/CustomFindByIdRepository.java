package com.eoscode.springapitools.data.repository;

import org.springframework.data.repository.NoRepositoryBean;

import java.util.Optional;

@NoRepositoryBean
public interface CustomFindByIdRepository<Entity, ID> {

    Optional<Entity> findCustomById(Class<Entity> entityClass, ID id);

    Optional<Entity> findDetailById(Class<Entity> entityClass, ID id);

}
