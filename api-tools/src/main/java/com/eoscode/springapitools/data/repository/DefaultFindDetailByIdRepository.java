package com.eoscode.springapitools.data.repository;

import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public class DefaultFindDetailByIdRepository<Entity, ID> extends BaseRepository<Entity, ID>
		implements CustomFindDetailByIdRepository<Entity, ID> {

	@Override
	public Optional<Entity> findDetailById(Class<Entity> entityClass, ID id) {
		return findWithEntityGraph("findDetailById", entityClass, id);
	}
	
}
