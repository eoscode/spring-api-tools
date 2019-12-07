package com.eoscode.springapitools.resource;

import com.eoscode.springapitools.service.DefaultService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

@SuppressWarnings("Duplicates")
public abstract class AbstractRepositoryResource<Repository extends com.eoscode.springapitools.data.repository.Repository<Entity, ID>, Entity, ID>
	extends AbstractResource<DefaultService<Repository, Entity, ID>, Entity, ID>{

	protected final Log log = LogFactory.getLog(this.getClass());

	private DefaultService<Repository, Entity, ID> defaultService;

	private Type repositoryType;
	private Type entityType;
	private Type identifierType;

	@Autowired
	public AbstractRepositoryResource(ApplicationContext applicationContext) {

		Type type = getClass().getGenericSuperclass();
		ParameterizedType pType = (ParameterizedType) type;

		this.repositoryType = pType.getActualTypeArguments()[0];
		this.entityType =  pType.getActualTypeArguments()[1];
		this.identifierType = pType.getActualTypeArguments()[2];

		this.defaultService = new DefaultService<>(applicationContext, repositoryType, entityType, identifierType);

	}

	public Type getRepositoryType() {
		return repositoryType;
	}

	private Type getEntityType() {
		return entityType;
	}

	public Type getIdentifierType() {
		return identifierType;
	}

	@SuppressWarnings("unchecked")
	private Class<Entity> getEntityClass() {
		return (Class<Entity>) entityType;
	}

	@Override
	public DefaultService<Repository, Entity, ID> getService() {
		return defaultService;
	}

}
