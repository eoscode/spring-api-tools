package com.eoscode.springapitools.data.repository;

import org.springframework.data.repository.NoRepositoryBean;

@NoRepositoryBean
public interface CustomDeleteByIdRepository<Entity, ID> {

    int deleteById(Class<Entity> entityClass, ID id);

}
