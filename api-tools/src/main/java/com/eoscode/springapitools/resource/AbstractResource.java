package com.eoscode.springapitools.resource;

import com.eoscode.springapitools.config.SpringApiToolsProperties;
import com.eoscode.springapitools.data.domain.Identifier;
import com.eoscode.springapitools.data.filter.QueryDefinition;
import com.eoscode.springapitools.data.filter.QueryParameter;
import com.eoscode.springapitools.service.AbstractService;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.monitorjbl.json.JsonView;
import com.monitorjbl.json.Match;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.reflections.ReflectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.data.web.SortDefault;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import javax.annotation.PostConstruct;
import javax.validation.Valid;
import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@SuppressWarnings({"Duplicates", "unchecked"})
public abstract class AbstractResource<Service extends AbstractService<?, Entity, ID>, Entity, ID> {

	protected final Log log = LogFactory.getLog(this.getClass());

	@Autowired
	private ApplicationContext applicationContext;

	@Autowired
	private SpringApiToolsProperties springApiToolsProperties;

	@Autowired
	private MappingJackson2HttpMessageConverter jackson2HttpMessageConverter;

	private Service service;

	private final Type serviceType;
	private final Type entityType;
	private final Type identifierType;
	private final Class<Entity> entityClass;

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

	@PostMapping(produces = MediaType.APPLICATION_JSON_VALUE)
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

	@GetMapping(value="/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<Entity> find(@PathVariable ID id) {
		Entity entity = getService().findById(id);
		return ResponseEntity.ok().body(entity);
	}

	@GetMapping(value="/detail/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<Entity> findDetail(@PathVariable ID id) {
		Entity entity = getService().findDetailById(id);
		return ResponseEntity.ok().body(entity);
	}

	@SuppressWarnings("unchecked")
	@PutMapping(value="/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<Void> update(@Valid @RequestBody Entity entity, @PathVariable ID id) {
		if (entity instanceof Identifier) {
			Identifier<ID> identifier = (Identifier<ID>) entity;
			identifier.setId(id);
		}

		getService().save(entity);
		return ResponseEntity.noContent().build();
	}

	@SuppressWarnings("unchecked")
	@PatchMapping(value="/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<Void> patch(@Valid @RequestBody Entity entity, @PathVariable ID id) {
		if (entity instanceof Identifier) {
			Identifier<ID> identifier = (Identifier<ID>) entity;
			identifier.setId(id);
		}

		getService().update(entity);
		return ResponseEntity.noContent().build();
	}

	@DeleteMapping(value="/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<Void> delete(@PathVariable ID id) {
		getService().deleteById(id);
		return ResponseEntity.noContent().build();
	}

	@GetMapping(value = {"/page","/find/page"}, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<Page<Entity>> findWithPage(Entity filterBy,
													 @PageableDefault Pageable pageable) {
		Page<Entity> page = getService().find(filterBy, pageable);
		return ResponseEntity.ok(page);
	}

	@GetMapping(value = {"","/find"}, produces = MediaType.APPLICATION_JSON_VALUE)
	public <T> T find(Entity filterBy, @PageableDefault Pageable pageable,
					  QueryParameter queryParameter) {
		T result;
		if (isDefaultPageable(queryParameter.getPageable())) {
			result = (T) getService().find(filterBy, pageable);
		} else {
			int maxSize = getListDefaultSize(queryParameter.getSize());
			if (maxSize > 0) {
				Page<Entity> page = getService().find(filterBy, PageRequest.of(0, maxSize, pageable.getSort()));
				result = (T) page.getContent();
				if (page.getTotalElements() > maxSize) {
					log.warn(String.format("list truncated, %d occurrence of %d. rule list-default-size=%d," +
									" list-default-size-override=%s",
							maxSize, page.getTotalElements(), maxSize, springApiToolsProperties.isListDefaultSizeOverride()));
				}
			} else {
				result = (T) getService().find(filterBy, pageable.getSort());
			}
		}
		return (T) ResponseEntity.ok(result);
	}

	@GetMapping(value = "/query/page", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<Page<Entity>> queryWithPage(@RequestParam(value = "opt", required = false, defaultValue = "") String query,
													  @PageableDefault Pageable pageable,
													  QueryParameter queryParameter) {
		Page<Entity> list = getService().query(query, queryParameter, pageable);
		return ResponseEntity.ok(list);
	}

	@GetMapping(value = "/query", produces = MediaType.APPLICATION_JSON_VALUE)
	public <T> T query(@RequestParam(value = "opt", required = false, defaultValue = "") String query,
					   @PageableDefault Pageable pageable,
					   QueryParameter queryParameter) {
		T result;
		if (isDefaultPageable(queryParameter.getPageable())) {
			result = (T) getService().query(query, queryParameter, pageable);
		} else {
			int maxSize = getListDefaultSize(queryParameter.getSize());
			if (maxSize > 0) {
				Page<Entity> page = getService().query(query, queryParameter, PageRequest.of(0, maxSize, pageable.getSort()));
				result = (T) page.getContent();
				if (page.getTotalElements() > maxSize) {
					log.warn(String.format("list truncated, %d occurrence of %d. rule list-default-size=%d," +
									" list-default-size-override=%s",
							maxSize, page.getTotalElements(), maxSize, springApiToolsProperties.isListDefaultSizeOverride()));
				}
			} else {
				result = (T) getService().query(query, queryParameter, pageable.getSort());
			}
		}
		return (T) ResponseEntity.ok(result);
	}

	@PostMapping(value = "/query/page", produces = MediaType.APPLICATION_JSON_VALUE)
	public <T> T queryWitPage(@RequestBody QueryDefinition queryDefinition,
					   @PageableDefault Pageable pageable) {
		T result = (T) getService().query(queryDefinition, pageable);

		if (queryDefinition.getViews() != null && !queryDefinition.getViews().isEmpty()) {
			return (T) ResponseEntity.ok(getJsonViewSerialized(queryDefinition, result));
		} else {
			return (T) ResponseEntity.ok(result);
		}
	}

	@PostMapping(value = "/query", produces = MediaType.APPLICATION_JSON_VALUE)
	public <T> T query(@RequestBody QueryDefinition queryDefinition,
											  @RequestParam(value = "pageable", required = false) Boolean page,
											  @PageableDefault Pageable pageable) {
		T result;
		if (isDefaultPageable(page)) {
			result = (T) getService().query(queryDefinition, pageable);
		} else {
			result = (T) getService().query(queryDefinition, pageable.getSort());
		}

		if (queryDefinition.getViews() != null && !queryDefinition.getViews().isEmpty()) {
			return (T) ResponseEntity.ok(getJsonViewSerialized(queryDefinition, result));
		} else {
			return (T) ResponseEntity.ok(result);
		}
	}

	@GetMapping(value = "/all", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<List<Entity>> findAll(@SortDefault Sort sort) {
		List<Entity> list = getService().findAll(sort);
		return ResponseEntity.ok(list);
	}

	@GetMapping(value = "/pages", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<Page<Entity>> findAllPageAndSort(@PageableDefault Pageable pageable/*, PagedResourcesAssembler<Entity> pagedAssembler*/) {
		Page<Entity> page = getService().findAllWithPage(pageable);
		return ResponseEntity.ok(page);
	}

	private boolean isDefaultPageable(Boolean pageable) {
		if (pageable != null) {
			return pageable;
		}
		return springApiToolsProperties.isEnableDefaultPageable();
	}

	private int getListDefaultSize(Integer size) {
		if (size != null && springApiToolsProperties.isListDefaultSizeOverride()) {
			return size;
		}
		return springApiToolsProperties.getListDefaultSize();
	}

	private <T> String getJsonViewSerialized(QueryDefinition queryDefinition, T result) {
		List<String> views = queryDefinition.getViews();
		try {
			Set<Field> ignoreAnnotations = ReflectionUtils.getAllFields(entityClass,
					field -> field.isAnnotationPresent(JsonIgnoreProperties.class)
							|| field.isAnnotationPresent(JsonIgnore.class));

			List<String> jsonIgnore = new ArrayList<>();
			ignoreAnnotations.forEach(field -> {
				if (field.isAnnotationPresent(JsonIgnoreProperties.class)) {
					JsonIgnoreProperties properties = field.getAnnotation(JsonIgnoreProperties.class);
					Arrays.stream(properties.value()).forEach(value -> jsonIgnore.add(String.format("%s.%s",
							field.getName(), value)));
				} else {
					jsonIgnore.add(field.getName());
				}
			});

			jsonIgnore.add("*");
			views.removeAll(jsonIgnore);

			List<String> fetches = new ArrayList<>();
			if (queryDefinition.getFilters() != null && !queryDefinition.getFilters().isEmpty()) {
				queryDefinition.getFilters().forEach(filterDefinition -> {
					if (filterDefinition.isJoin() && filterDefinition.isFetch()) {
						fetches.add(filterDefinition.getPathJoin());
					}
				});
			}
			if (queryDefinition.getJoins() != null && !queryDefinition.getJoins().isEmpty()) {
				queryDefinition.getJoins().forEach(joinDefinition -> {
					if (joinDefinition.isFetch()) {
						fetches.add(joinDefinition.getField());
					}
				});
			}

			views = views.stream()
					.filter(view -> {
						int idx = view.indexOf(".");
						if (idx == -1) {
							return true;
						} else {
							try {
								String fieldName = view.substring(0, idx);
								Field field = entityClass.getDeclaredField(fieldName);
								if (field.getGenericType() instanceof ParameterizedType) {
									ParameterizedType pType = (ParameterizedType) field.getGenericType();
									field = ((Class<?>) pType.getActualTypeArguments()[0])
											.getDeclaredField(view.substring(idx + 1));
								} else {
									field = field.getType().getDeclaredField(view.substring(idx + 1));
								}

								boolean jsonIgnoreAnnotation = field.isAnnotationPresent(JsonIgnore.class);
								boolean jsonFetch = fetches.contains(fieldName);
								boolean showField = !jsonIgnoreAnnotation && jsonFetch;
								if (!showField) {
									log.debug(String.format("ignore field %s to query view in entity %s. annotation JsonIgnore: %s, fetch: %s",
											view, entityClass.getName(), jsonIgnoreAnnotation, jsonFetch));
								}
								return showField;
							} catch (NoSuchFieldException e) {
								log.error(String.format("field %s to query view in entity %s. %s",
										view, entityClass.getName(), e.getMessage()), e);
							}
							return false;
						}
					}).collect(Collectors.toList());

			JsonView<?> jsonView = JsonView.with(result)
					.onClass(entityClass,
							Match.match()
									.exclude("*")
									.include(views.toArray(new String[0])));

			return jackson2HttpMessageConverter
					.getObjectMapper()
					.copy()
					.writeValueAsString(jsonView);
		} catch (JsonProcessingException e) {
			throw new RuntimeException(e);
		}

	}

}
