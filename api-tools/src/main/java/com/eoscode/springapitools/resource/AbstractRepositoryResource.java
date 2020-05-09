package com.eoscode.springapitools.resource;

import com.eoscode.springapitools.service.DefaultService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;

import javax.annotation.PostConstruct;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

@SuppressWarnings("Duplicates")
public abstract class AbstractRepositoryResource<Repository extends com.eoscode.springapitools.data.repository.Repository<Entity, ID>, Entity, ID>
	extends AbstractResource<DefaultService<Repository, Entity, ID>, Entity, ID>{

	protected final Log log = LogFactory.getLog(this.getClass());

	@Autowired
	private ApplicationContext applicationContext;

	private DefaultService<Repository, Entity, ID> defaultService;
	private final Type repositoryType;
	private final Type entityType;
	private final Type identifierType;

	public AbstractRepositoryResource() {

		Type type = getClass().getGenericSuperclass();
		ParameterizedType pType = (ParameterizedType) type;

		repositoryType = pType.getActualTypeArguments()[0];
		entityType =  pType.getActualTypeArguments()[1];
		identifierType = pType.getActualTypeArguments()[2];

	}

	@PostConstruct
	private void metaData() {
		this.defaultService = new DefaultService<>(applicationContext, getRepositoryType(), getEntityType(), getIdentifierType());
	}

	@Override
	public Type getEntityType() {
		return entityType;
	}

	@Override
	public Type getIdentifierType() {
		return identifierType;
	}

	public Type getRepositoryType() {
		return repositoryType;
	}

	@Override
	public DefaultService<Repository, Entity, ID> getService() {
		return defaultService;
	}

}
