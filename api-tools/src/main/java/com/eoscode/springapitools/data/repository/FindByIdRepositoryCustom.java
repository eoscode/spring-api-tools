package com.eoscode.springapitools.data.repository;

import org.springframework.data.repository.NoRepositoryBean;

import java.util.Optional;

@NoRepositoryBean
public interface FindByIdRepositoryCustom<Entity, ID> {

    Optional<Entity> findById(Class<Entity> entityClass, ID id);

    Optional<Entity> findDetailById(Class<Entity> entityClass, ID id);

}
