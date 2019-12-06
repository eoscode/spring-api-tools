package com.eoscode.springapitools.data.repository;

import java.util.Optional;


@org.springframework.stereotype.Repository
public class DefaultCustomDetailByIdRepository<Entity, ID> extends BaseCustomRepository implements CustomDetailByIdRepository<Entity, ID> {
	
	@Override
	public Optional<Entity> findDetailById(Class<Entity> entityClass, ID id) {
		return findWithEntityGraph("detail", entityClass, id);
	}
	
}
