package com.eoscode.springapitools.data.repository;

import java.util.Optional;


@org.springframework.stereotype.Repository
public class DetailByIdRepositoryImpl<Entity, ID> extends BaseCustomRepository implements DetailByIdRepositoryCustom<Entity, ID> {
	
	@Override
	public Optional<Entity> findDetailById(Class<Entity> entityClass, ID id) {
		return findWithEntityGraph("detail", entityClass, id);
	}
	
}
