package com.eoscode.springapitools.data.repository;

import org.springframework.data.repository.NoRepositoryBean;

import java.util.Optional;

@NoRepositoryBean
public interface DetailByIdRepositoryCustom<Entity, ID> {

	Optional<Entity> findDetailById(Class<Entity> entityClass, ID id);

}
