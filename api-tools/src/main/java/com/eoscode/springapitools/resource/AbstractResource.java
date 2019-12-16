package com.eoscode.springapitools.resource;

import com.eoscode.springapitools.data.domain.Identifier;
import com.eoscode.springapitools.data.domain.QueryDefinition;
import com.eoscode.springapitools.service.AbstractService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import javax.annotation.PostConstruct;
import javax.validation.Valid;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.net.URI;
import java.util.List;

@SuppressWarnings({"Duplicates", "unchecked"})
public abstract class AbstractResource<Service extends AbstractService<?, Entity, ID>, Entity, ID> {

	protected final Log log = LogFactory.getLog(this.getClass());
	
	@Autowired
	private ApplicationContext applicationContext;

	private Service service;

	private Type serviceType;
	private Type entityType;
	private Type identifierType;
	private Class<Entity> entityClass;
	
	public AbstractResource() {
		Type type = getClass().getGenericSuperclass();
		ParameterizedType pType = (ParameterizedType) type;

		serviceType = pType.getActualTypeArguments()[0];
		entityType =  pType.getActualTypeArguments()[1];
		identifierType = pType.getActualTypeArguments()[2];
		entityClass = (Class<Entity>) entityType;

	}

	@PostConstruct
	private void metaData() {
		if (applicationContext != null) {
			if (getService() == null) {
				Class<Service> serviceClass = (Class<Service>) serviceType;
				if (serviceClass.isAnnotationPresent(org.springframework.stereotype.Service.class)) {
					service = applicationContext.getBean(serviceClass);
				}
			}
		}
	}

	public Type getServiceType() {
		return serviceType;
	}

	public Type getEntityType() {
		return entityType;
	}

	public Type getIdentifierType() {
		return identifierType;
	}

	private Class<Entity> getEntityClass() {
		return entityClass;
	}
	
	protected Service getService() {
		return service;
	}
	
	@PostMapping
	public ResponseEntity<Entity> save(@Valid @RequestBody Entity entity) {
		entity = getService().save(entity);

		Identifier<?> identifier = null;
		if (entity instanceof Identifier) {
			identifier = (Identifier<?>) entity;
		}

		if (identifier != null) {
			URI uri = ServletUriComponentsBuilder.fromCurrentRequest()
					.path("/{id}").buildAndExpand(identifier.getId()).toUri();
			return ResponseEntity.created(uri).build();
		} else {
			return ResponseEntity.ok().body(entity);
		}
	}

	@GetMapping(value="/{id}")
	public ResponseEntity<Entity> find(@PathVariable ID id) {
		Entity entity = getService().findById(id);
		return ResponseEntity.ok().body(entity);
	}

	@GetMapping(value="/detail/{id}")
	public ResponseEntity<Entity> findDetail(@PathVariable ID id) {
		Entity entity = getService().findDetailById(id);
		return ResponseEntity.ok().body(entity);
	}

	@SuppressWarnings("unchecked")
	@PutMapping(value="/{id}")
	public ResponseEntity<Void> update(@Valid @RequestBody Entity entity, @PathVariable ID id) {
		if (entity instanceof Identifier) {
			Identifier<ID> identifier = (Identifier<ID>) entity;
			identifier.setId(id);
		}

		getService().save(entity);
		return ResponseEntity.noContent().build();
	}

	@SuppressWarnings("unchecked")
	@PatchMapping(value="/{id}")
	public ResponseEntity<Void> patch(@Valid @RequestBody Entity entity, @PathVariable ID id) {
		if (entity instanceof Identifier) {
			Identifier<ID> identifier = (Identifier<ID>) entity;
			identifier.setId(id);
		}

		getService().update(entity);
		return ResponseEntity.noContent().build();
	}

	@DeleteMapping(value="/{id}")
	public ResponseEntity<Void> delete(@PathVariable ID id) {
		getService().deleteById(id);
		return ResponseEntity.noContent().build();
	}

	@GetMapping(value = {"","/find"})
	public ResponseEntity<Page<Entity>> find(Entity filterBy,
											 @PageableDefault Pageable pageable) {
		Page<Entity> page = getService().find(filterBy, pageable);
		return ResponseEntity.ok(page);
	}

	@GetMapping("/query")
	public ResponseEntity<Page<Entity>> query(@RequestParam(value = "opt") String query,
											  @RequestParam(value = "distinct", required = false) boolean distinct,
											  @PageableDefault Pageable pageable) {
		Page<Entity> page = getService().query(query, pageable, distinct);
		return ResponseEntity.ok(page);
	}

	@PostMapping("/query")
	public ResponseEntity<Page<Entity>> query(@RequestBody QueryDefinition queryDefinition,
											  @PageableDefault Pageable pageable) {
		Page<Entity> page = getService().query(queryDefinition, pageable);
		return ResponseEntity.ok(page);
	}

	@GetMapping("/all")
	public ResponseEntity<List<Entity>> findAll() {
		List<Entity> list = getService().findAll();
		return ResponseEntity.ok(list);
	}

	@GetMapping("/pages")
	public ResponseEntity<Page<Entity>> findAllPage(@PageableDefault Pageable pageable/*, PagedResourcesAssembler<Entity> pagedAssembler*/) {
		Page<Entity> page = getService().findAllPages(pageable);
		return ResponseEntity.ok(page);
	}

}
