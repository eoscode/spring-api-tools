package com.eoscode.springapitools.resource;

import com.eoscode.springapitools.config.QueryView;
import com.eoscode.springapitools.config.SpringApiToolsProperties;
import com.eoscode.springapitools.data.domain.DynamicView;
import com.eoscode.springapitools.data.domain.Identifier;
import com.eoscode.springapitools.data.filter.QueryDefinition;
import com.eoscode.springapitools.data.filter.QueryParameter;
import com.eoscode.springapitools.data.filter.ViewDefinition;
import com.eoscode.springapitools.resource.exception.MethodNotAllowedException;
import com.eoscode.springapitools.service.AbstractService;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.monitorjbl.json.JsonView;
import com.monitorjbl.json.Match;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.reflections.ReflectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.data.domain.*;
import org.springframework.data.web.PageableDefault;
import org.springframework.data.web.SortDefault;
import org.springframework.http.HttpMethod;
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
import java.util.*;
import java.util.stream.Collectors;

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
	private Set<HttpMethod> methodNotAllowed = new HashSet<>();

	private final Type serviceType;
	private final Type entityType;
	private final Type identifierType;
	private final Class<Entity> entityClass;
	private boolean queryWithViews = false;

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
		if (springApiToolsProperties.getQueryWithViews() == QueryView.all) {
			queryWithViews = true;
		} else if (springApiToolsProperties.getQueryWithViews() == QueryView.entity) {
			queryWithViews = entityClass.isAnnotationPresent(DynamicView.class);
		} else if (springApiToolsProperties.getQueryWithViews() == QueryView.none) {
			queryWithViews = false;
		}

		if (getClass().isAnnotationPresent(MethodNotAllowed.class)) {
			MethodNotAllowed methodNotAllowedAnnotation = getClass().getAnnotation(MethodNotAllowed.class);
			methodNotAllowed.addAll(Arrays.stream(methodNotAllowedAnnotation.methods()).collect(Collectors.toSet()));
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

		if (methodNotAllowed.contains(HttpMethod.POST)) {
			throw new MethodNotAllowedException(HttpMethod.POST.name());
		}

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
	public <T> T find(@PathVariable ID id,
					  @RequestParam(value = "views", required = false, defaultValue = "") Set<String> views) {

		if (methodNotAllowed.contains(HttpMethod.GET)) {
			throw new MethodNotAllowedException(HttpMethod.GET.name());
		}

		Entity entity = getService().findById(id);
		return (T) ResponseEntity.ok(toJson(ViewDefinition.create(views), entity));
	}

	@GetMapping(value="/detail/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
	public <T> T findDetail(@PathVariable ID id,
							@RequestParam(value = "views", required = false, defaultValue = "") Set<String> views) {

		if (methodNotAllowed.contains(HttpMethod.GET)) {
			throw new MethodNotAllowedException(HttpMethod.GET.name());
		}

		Entity entity = getService().findDetailById(id);

		ViewDefinition viewDefinition = ViewDefinition.create(views);
		return (T) ResponseEntity.ok(toJson(viewDefinition, entity));
	}

	@SuppressWarnings("unchecked")
	@PutMapping(value="/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<Void> update(@Valid @RequestBody Entity entity, @PathVariable ID id) {

		if (methodNotAllowed.contains(HttpMethod.PUT)) {
			throw new MethodNotAllowedException(HttpMethod.PUT.name());
		}

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

		if (methodNotAllowed.contains(HttpMethod.PATCH)) {
			throw new MethodNotAllowedException(HttpMethod.PATCH.name());
		}

		if (entity instanceof Identifier) {
			Identifier<ID> identifier = (Identifier<ID>) entity;
			identifier.setId(id);
		}

		getService().update(entity);
		return ResponseEntity.noContent().build();
	}

	@DeleteMapping(value="/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<Void> delete(@PathVariable ID id) {

		if (methodNotAllowed.contains(HttpMethod.DELETE)) {
			throw new MethodNotAllowedException(HttpMethod.DELETE.name());
		}

		getService().deleteById(id);
		return ResponseEntity.noContent().build();
	}

	@GetMapping(value = {"/page","/find/page"}, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<Page<Entity>> findWithPage(Entity filterBy,
													 @PageableDefault Pageable pageable) {
		if (methodNotAllowed.contains(HttpMethod.GET)) {
			throw new MethodNotAllowedException(HttpMethod.GET.name());
		}

		Page<Entity> page = getService().find(filterBy, pageable);
		return ResponseEntity.ok(page);
	}

	@GetMapping(value = {"","/find"}, produces = MediaType.APPLICATION_JSON_VALUE)
	public <T> T find(Entity filterBy, @PageableDefault Pageable pageable,
					  QueryParameter queryParameter) {

		if (methodNotAllowed.contains(HttpMethod.GET)) {
			throw new MethodNotAllowedException(HttpMethod.GET.name());
		}

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
	public <T> T queryWithPage(@RequestParam(value = "opt", required = false, defaultValue = "") String query,
													  @RequestParam(value = "views", required = false, defaultValue = "") Set<String> views,
													  @PageableDefault Pageable pageable,
													  QueryParameter queryParameter) {

		if (methodNotAllowed.contains(HttpMethod.GET)) {
			throw new MethodNotAllowedException(HttpMethod.GET.name());
		}

		queryParameter.setPageable(true); //force pageable
		T result = query(query, views, pageable, queryParameter);

		return (T) ResponseEntity.ok(result);
	}

	@GetMapping(value = "/query", produces = MediaType.APPLICATION_JSON_VALUE)
	public <T> T query(@RequestParam(value = "opt", required = false, defaultValue = "") String query,
					   @RequestParam(value = "views", required = false, defaultValue = "") Set<String> views,
					   @PageableDefault Pageable pageable,
					   QueryParameter queryParameter) {

		if (methodNotAllowed.contains(HttpMethod.GET)) {
			throw new MethodNotAllowedException(HttpMethod.GET.name());
		}

		QueryDefinition queryDefinition = getService().createQueryDefinition(query, queryParameter);
		queryDefinition.setViews(views);

		T result;
		if (isDefaultPageable(queryParameter.getPageable())) {
			result = query(queryDefinition, queryParameter.getPageable(), pageable);
		} else {
			int maxSize = getListDefaultSize(queryParameter.getSize());
			if (maxSize > 0) {
				Page<Entity> page = query(queryDefinition, queryParameter.getPageable(), PageRequest.of(0, maxSize, pageable.getSort()));
				result = (T) page.getContent();
				if (page.getTotalElements() > maxSize) {
					log.warn(String.format("list truncated, %d occurrence of %d. rule list-default-size=%d," +
									" list-default-size-override=%s",
							maxSize, page.getTotalElements(), maxSize, springApiToolsProperties.isListDefaultSizeOverride()));
				}
			} else {
				result = query(queryDefinition, false, pageable);
			}
		}

		return result;
	}

	@PostMapping(value = "/query/page", produces = MediaType.APPLICATION_JSON_VALUE)
	public <T> T queryWitPage(@RequestBody(required = false) QueryDefinition queryDefinition,
					   @PageableDefault Pageable pageable) {

		if (methodNotAllowed.contains(HttpMethod.POST)) {
			throw new MethodNotAllowedException(HttpMethod.POST.name());
		}

		T result = (T) getService().query(queryDefinition, pageable);

		return (T) ResponseEntity.ok(toJson(queryDefinition, result));
	}

	@PostMapping(value = "/query", produces = MediaType.APPLICATION_JSON_VALUE)
	public <T> T query(@RequestBody(required = false) QueryDefinition queryDefinition,
											  @RequestParam(value = "pageable", required = false) Boolean page,
											  @PageableDefault Pageable pageable) {

		if (methodNotAllowed.contains(HttpMethod.POST)) {
			throw new MethodNotAllowedException(HttpMethod.POST.name());
		}

		T result;
		if (isDefaultPageable(page)) {
			result = (T) getService().query(queryDefinition, pageable);
		} else {
			result = (T) getService().query(queryDefinition, pageable.getSort());
		}

		return (T) ResponseEntity.ok(toJson(queryDefinition, result));
	}

	@GetMapping(value = "/all", produces = MediaType.APPLICATION_JSON_VALUE)
	public <T> T findAll(@SortDefault Sort sort,
						 @RequestParam(value = "views", required = false, defaultValue = "") Set<String> views) {

		if (methodNotAllowed.contains(HttpMethod.GET)) {
			throw new MethodNotAllowedException(HttpMethod.GET.name());
		}

		List<Entity> list = getService().findAll(sort);

		ViewDefinition viewDefinition = ViewDefinition.create(views);
		return (T) ResponseEntity.ok(toJson(viewDefinition, list));
	}

	@GetMapping(value = "/pages", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<String> findAllPageAndSort(@PageableDefault Pageable pageable,
													 @RequestParam(value = "views", required = false, defaultValue = "") Set<String> views) {

		if (methodNotAllowed.contains(HttpMethod.GET)) {
			throw new MethodNotAllowedException(HttpMethod.GET.name());
		}

		Page<Entity> page = getService().findAllWithPage(pageable);
		ViewDefinition viewDefinition = ViewDefinition.create(views);
		return ResponseEntity.ok(toJson(viewDefinition, page));
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

	/**
	 * Serialize Object to Json with ViewDefinition supported
	 *
	 * @param viewDefinition views
	 * @param result Object
	 * @return json
	 */
	private <T> String toJson(ViewDefinition viewDefinition, T result) {

		ObjectMapper objectMapper = jackson2HttpMessageConverter
				.getObjectMapper();

		if (queryWithViews && !viewDefinition.getViews().isEmpty()) {
			Set<String> views = viewDefinition.getViews();
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
				jsonIgnore.forEach(views::remove);

				Set<String> fetches = viewDefinition.getFetches();

			/*
				remove views with @JsonIgnore and without fetch definition in FilterDefinition or JoinDefinition
			 */
				views = views.stream()
						.filter(view -> {
							int idx = view.indexOf(".");
							if (idx == -1) {
								return true;
							} else {
								try {
									boolean dynamicView;
									String fieldName = view.substring(0, idx);
									Field field = entityClass.getDeclaredField(fieldName);
									if (field.getGenericType() instanceof ParameterizedType) {
										ParameterizedType pType = (ParameterizedType) field.getGenericType();
										Class<?> type = ((Class<?>) pType.getActualTypeArguments()[0]);
										dynamicView = springApiToolsProperties.getQueryWithViews() == QueryView.all
												|| (springApiToolsProperties.getQueryWithViews() == QueryView.entity
												&& type.isAnnotationPresent(DynamicView.class));

										field = type.getDeclaredField(view.substring(idx + 1));
									} else {
										dynamicView = springApiToolsProperties.getQueryWithViews() == QueryView.all
												|| (springApiToolsProperties.getQueryWithViews() == QueryView.entity
												&& field.getType().isAnnotationPresent(DynamicView.class));

										field = field.getType().getDeclaredField(view.substring(idx + 1));
									}

									boolean fieldWithJsonIgnore = field.isAnnotationPresent(JsonIgnore.class);
									boolean fieldWithFetch = fetches.contains(fieldName);
									boolean showField = !fieldWithJsonIgnore && fieldWithFetch && dynamicView;
									if (!showField) {
										log.debug(String.format("ignore field %s to query view in entity %s." +
														" annotation JsonIgnore: %s, fetch: %s, dynamicView: %s",
												view, entityClass.getName(), fieldWithJsonIgnore, fieldWithFetch, dynamicView));
									}
									return showField;
								} catch (NoSuchFieldException e) {
									log.error(String.format("field %s to query view in entity %s. %s",
											view, entityClass.getName(), e.getMessage()), e);
								}
								return false;
							}
						}).collect(Collectors.toSet());

				if (result instanceof Page) {
					Page<Entity> page = (Page<Entity>) result;
					List<Entity> content = page.getContent();
					page = new PageImpl<>(new ArrayList<>(), page.getPageable(), page.getTotalElements());

					ObjectNode rootNode = objectMapper.createObjectNode();
					JsonView<?> jsonView = JsonView.with(content)
							.onClass(entityClass,
									Match.match()
											.exclude("*")
											.include(views.toArray(new String[0])));
					rootNode.putPOJO("content", jsonView);
					rootNode.putPOJO("pageable", page.getPageable());
					rootNode.put("total", page.getTotalElements());

					return objectMapper
							.writeValueAsString(rootNode);

				} else {
					JsonView<?> jsonView = JsonView.with(result)
							.onClass(entityClass,
									Match.match()
											.exclude("*")
											.include(views.toArray(new String[0])));

					return objectMapper.writeValueAsString(jsonView);
				}
			} catch (JsonProcessingException e) {
				log.error(e.getMessage(), e);
				throw new RuntimeException(e);
			}
		} else {
			try {
				return objectMapper.writeValueAsString(result);
			} catch (JsonProcessingException e) {
				log.error(e.getMessage(), e);
				throw new RuntimeException(e);
			}
		}
	}

}
