package com.eoscode.springapitools.data.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.NoRepositoryBean;

@NoRepositoryBean
public interface Repository<Entity, ID> extends JpaRepository<Entity, ID>,
        JpaSpecificationExecutor<Entity> {
}
