package com.eoscode.springapitools.data.repository;

import org.springframework.data.repository.NoRepositoryBean;

@NoRepositoryBean
public interface NoDeleteRepositoryCustom<Entity, ID> {

    void noDeleteById(Class<Entity> entityClass, ID id);

}
