package com.eoscode.springapitools.resource;

import com.eoscode.springapitools.data.domain.Identifier;
import com.eoscode.springapitools.data.filter.QueryParameter;
import com.eoscode.springapitools.data.filter.ViewDefinition;
import com.eoscode.springapitools.exceptions.MappingStructureValidationException;
import com.eoscode.springapitools.resource.exception.MethodNotAllowedException;
import com.eoscode.springapitools.service.AbstractService;
import com.eoscode.springapitools.exceptions.ValidationException;
import com.eoscode.springapitools.util.ObjectUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.util.ReflectionUtils;
import org.springframework.data.web.PageableDefault;
import org.springframework.data.web.SortDefault;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import javax.validation.Valid;
import java.net.URI;
import java.util.List;
import java.util.Set;

@SuppressWarnings({"Duplicates", "unchecked"})
public abstract class AbstractResource<Service extends AbstractService<?, Entity, ID>, Entity, ID>
		extends AbstractQueryResource<Service, Entity, ID> {

	public AbstractResource() {
		super();
	}

	private ID getIdentifierValue(Entity entity) throws IllegalAccessException {
		if (entity instanceof Identifier<?>) {
			return ((Identifier<ID>) entity).getId();
		} else {
			try {
				Class<Entity> entityClass = (Class<Entity>) getEntityType();
				Object value = ReflectionUtils.getMethod(entityClass, "getId")
						.orElseThrow(NoSuchMethodException::new).invoke(entity);
				if (value != null) {
					return ObjectUtils.getObject(getIdentifierType().getClass(), value);
				}
			} catch (Exception e) {
				throw new IllegalAccessException("id value not defined in entity");
			}
		}
		return null;
	}

	private void defineIdentifierValue(Entity entity, ID id) throws IllegalAccessException {
		if (entity instanceof Identifier) {
			Identifier<ID> identifier = (Identifier<ID>) entity;
			identifier.setId(id);
		} else {
			try {
				Class<Entity> entityClass = (Class<Entity>) getEntityType();
				Class<ID> IdentifierClass = (Class<ID>) getIdentifierType();
				ReflectionUtils.findRequiredMethod(entityClass, "setId", IdentifierClass)
								.invoke(entity, id);
			} catch (Exception e) {
				throw new IllegalAccessException("id value not defined in entity");
			}
		}
	}

	@PostMapping(produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<Entity> save(@Valid @RequestBody Entity entity) {

		if (methodNotAllowed.contains(HttpMethod.POST)) {
			throw new MethodNotAllowedException(HttpMethod.POST.name());
		}

		entity = getService().save(entity);
		try {
			ID identifier = getIdentifierValue(entity);
			if (identifier != null) {
				URI uri = ServletUriComponentsBuilder.fromCurrentRequest()
						.path("/{id}").buildAndExpand(identifier).toUri();
				return ResponseEntity.created(uri).build();
			} else {
				return ResponseEntity.ok().body(entity);
			}
		} catch (IllegalAccessException e) {
			throw new MappingStructureValidationException(e.getMessage(), e);
		}
	}

	@GetMapping(value="/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
	public <T> T find(@PathVariable ID id,
					  @RequestParam(value = "views", required = false, defaultValue = "") Set<String> views) {

		if (methodNotAllowed.contains(HttpMethod.GET)) {
			throw new MethodNotAllowedException(HttpMethod.GET.name());
		}

		Entity entity = getService().findById(id);

		if (queryWithViews) {
			return (T) ResponseEntity.ok(viewToJson.toJson(ViewDefinition.create(views), entity));
		} else {
			return (T) ResponseEntity.ok(entity);
		}
	}

	@GetMapping(value="/detail/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
	public <T> T findDetail(@PathVariable ID id,
							@RequestParam(value = "views", required = false, defaultValue = "") Set<String> views) {

		if (methodNotAllowed.contains(HttpMethod.GET)) {
			throw new MethodNotAllowedException(HttpMethod.GET.name());
		}

		Entity entity = getService().findDetailById(id);

		ViewDefinition viewDefinition = ViewDefinition.create(views);

		if (queryWithViews) {
			return (T) ResponseEntity.ok(viewToJson.toJson(viewDefinition, entity));
		} else {
			return (T) ResponseEntity.ok(entity);
		}
	}

	@PutMapping(value="/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<Void> update(@Valid @RequestBody Entity entity, @PathVariable ID id) {

		if (methodNotAllowed.contains(HttpMethod.PUT)) {
			throw new MethodNotAllowedException(HttpMethod.PUT.name());
		}

		try {
			defineIdentifierValue(entity, id);
		} catch (IllegalAccessException e) {
			throw new MappingStructureValidationException(e.getMessage(), e);
		}

		getService().save(entity);
		return ResponseEntity.noContent().build();
	}

	@PatchMapping(value="/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<Void> patch(@Valid @RequestBody Entity entity, @PathVariable ID id) {

		if (methodNotAllowed.contains(HttpMethod.PATCH)) {
			throw new MethodNotAllowedException(HttpMethod.PATCH.name());
		}

		try {
			defineIdentifierValue(entity, id);
		} catch (IllegalAccessException e) {
			throw new MappingStructureValidationException(e.getMessage(), e);
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

	@GetMapping(value = {"","/find"}, produces = MediaType.APPLICATION_JSON_VALUE)
	public <T> T find(Entity filterBy, @PageableDefault Pageable pageable,
					  QueryParameter queryParameter) {

		if (methodNotAllowed.contains(org.springframework.http.HttpMethod.GET)) {
			throw new MethodNotAllowedException(org.springframework.http.HttpMethod.GET.name());
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

	@GetMapping(value = {"/find/page"}, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<Page<Entity>> findWithPage(Entity filterBy,
													 @PageableDefault Pageable pageable) {
		if (methodNotAllowed.contains(org.springframework.http.HttpMethod.GET)) {
			throw new MethodNotAllowedException(org.springframework.http.HttpMethod.GET.name());
		}

		Page<Entity> page = getService().find(filterBy, pageable);
		return ResponseEntity.ok(page);
	}

	@GetMapping(value = "/all", produces = MediaType.APPLICATION_JSON_VALUE)
	public <T> T findAll(@SortDefault Sort sort,
						 @RequestParam(value = "views", required = false, defaultValue = "") Set<String> views) {

		if (methodNotAllowed.contains(org.springframework.http.HttpMethod.GET)) {
			throw new MethodNotAllowedException(org.springframework.http.HttpMethod.GET.name());
		}

		List<Entity> list = getService().findAll(sort);

		if (queryWithViews) {
			ViewDefinition viewDefinition = ViewDefinition.create(views);
			return (T) ResponseEntity.ok(viewToJson.toJson(viewDefinition, list));
		} else {
			return (T) ResponseEntity.ok(list);
		}
	}

	@GetMapping(value = "/all/page", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<?> findAllPageAndSort(@PageableDefault Pageable pageable,
												@RequestParam(value = "views", required = false, defaultValue = "") Set<String> views) {

		if (methodNotAllowed.contains(org.springframework.http.HttpMethod.GET)) {
			throw new MethodNotAllowedException(HttpMethod.GET.name());
		}

		Page<Entity> page = getService().findAllWithPage(pageable);

		if (queryWithViews) {
			ViewDefinition viewDefinition = ViewDefinition.create(views);
			return ResponseEntity.ok(viewToJson.toJson(viewDefinition, page));
		} else {
			return ResponseEntity.ok(page);
		}
	}

}
