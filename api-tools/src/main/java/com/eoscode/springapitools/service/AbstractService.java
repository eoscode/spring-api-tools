package com.eoscode.springapitools.service;

import com.eoscode.springapitools.data.domain.Find;
import com.eoscode.springapitools.data.domain.FindAttribute;
import com.eoscode.springapitools.data.domain.Identifier;
import com.eoscode.springapitools.data.domain.NoDelete;
import com.eoscode.springapitools.data.domain.filter.FilterCriteria;
import com.eoscode.springapitools.data.repository.FindByIdRepositoryCustom;
import com.eoscode.springapitools.data.repository.NoDeleteRepositoryCustom;
import com.eoscode.springapitools.service.exceptions.EntityNotFoundException;
import com.eoscode.springapitools.util.NullAwareBeanUtilsBean;
import com.eoscode.springapitools.util.ReflectionUtils;
import com.eoscode.springapitools.util.SpecificationsBuilder;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.ExampleMatcher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.transaction.annotation.Transactional;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static org.reflections.ReflectionUtils.getAllFields;
import static org.reflections.ReflectionUtils.withAnnotation;

@SuppressWarnings("unchecked")
public abstract class AbstractService<Repository extends com.eoscode.springapitools.data.repository.Repository<Entity, ID>, Entity, ID> {

    private final Log log = LogFactory.getLog(this.getClass());

    @Autowired
    private FindByIdRepositoryCustom findByIdRepositoryCustom;

    @Autowired
    private NoDeleteRepositoryCustom noDeleteRepositoryCustom;

    public abstract Repository getRepository();

    private Type repositoryType;
    private Type entityType;
    private Type identifierType;
    private Class<Entity> entityClass;

    private Set<Field> ignoreWithFindAttributeAnnotation;

    public AbstractService() {
        Type type = getClass().getGenericSuperclass();
        ParameterizedType pType = (ParameterizedType) type;

        repositoryType = pType.getActualTypeArguments()[0];
        entityType =  pType.getActualTypeArguments()[1];
        identifierType = pType.getActualTypeArguments()[2];
        entityClass = (Class<Entity>) entityType;
        metaData();
    }

    public AbstractService(Type repositoryType, Type entityType, Type identifierType) {
        this.repositoryType = repositoryType;
        this.entityType = entityType;
        this.identifierType = identifierType;
        this.entityClass = (Class<Entity>) entityType;
        metaData();
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
        return entityClass;
    }

    private void metaData() {
        ignoreWithFindAttributeAnnotation = getAllFields(entityClass, withAnnotation(new FindAttribute() {
            @Override
            public Class<? extends Annotation> annotationType() {
                return FindAttribute.class;
            }

            @Override
            public boolean ignore() {
                return true;
            }
        }));
    }

    @SuppressWarnings("Duplicates")
    @Transactional
    public Entity save(Entity entity) {
        ID id = null;
        if (entity instanceof Identifier<?>) {
            id = ((Identifier<ID>) entity).getId();
        }

        Class<Entity> classType = (Class<Entity>) entityType;
        if (id == null && classType.isAnnotationPresent(NoDelete.class)) {
            NoDelete noDelete = classType.getAnnotation(NoDelete.class);
            try {
                Field field = classType.getDeclaredField(noDelete.field());
                field.setAccessible(true);
                field.set(entity, ReflectionUtils.getObject(field, noDelete.defaultValue()));
            } catch (Exception e) {
                log.error(e.getMessage(), e);
            }
        }
        return getRepository().save(entity);
    }

    @Transactional
    public Entity update(Entity entity) throws EntityNotFoundException {

        Entity entityOld = null;
        if (entity instanceof Identifier<?>) {
            ID id =  ((Identifier<ID>) entity).getId();
            entityOld = findById(id);
        }

        if (entityOld != null) {
            try {
                NullAwareBeanUtilsBean.getInstance().copyProperties(entityOld, entity);
            } catch (IllegalAccessException | InvocationTargetException e) {
                log.error(e.getMessage(), e);
            }
        }

        return getRepository().save(entityOld);
    }

    public Entity findById(ID id) throws EntityNotFoundException {

        EntityNotFoundException objectNotFound = new EntityNotFoundException(
                "Object not found! Id: " + id + ", Type: " + getEntityType().getTypeName ());

        Optional<Entity> result = findByIdRepositoryCustom.findById(getEntityClass(), id);

        return result.orElse(getRepository().findById(id).orElseThrow(() -> objectNotFound));

    }

    public Entity findDetailById(ID id) throws EntityNotFoundException {
        @SuppressWarnings("unchecked")
        Class<Entity> classType = (Class<Entity>) entityType;
        Optional<Entity> result = findByIdRepositoryCustom.findDetailById(classType, id);
        if (result.isPresent()) {
            return result.get();
        } else {
            throw new EntityNotFoundException(
                    "Object not found! Id: " + id + ", Type: " + getEntityType().getTypeName ());
        }
    }

    public boolean existsById(ID id) {
        return getRepository().existsById(id);
    }

    @Transactional
    public void deleteById(ID id) {
        Entity entity = findById(id);
        Class<Entity> classEntity = (Class<Entity>) entityType;
        if (classEntity.isAnnotationPresent(NoDelete.class)) {
            noDeleteRepositoryCustom.noDeleteById(classEntity, id);
        } else {
            getRepository().deleteById(id);
        }
    }

    @Transactional
    public void delete(Entity entity) {
        Class<Entity> classEntity = (Class<Entity>) entityType;
        if (classEntity.isAnnotationPresent(NoDelete.class)) {
            ID id =  ((Identifier<ID>) entity).getId();
            noDeleteRepositoryCustom.noDeleteById(classEntity, id);
        } else {
            getRepository().delete(entity);
        }
    }

    public List<Entity> find(Entity filterBy) {
       if (getEntityClass().isAnnotationPresent(Find.class)) {
            Example<Entity> example = getDefaultExample(filterBy);
            return getRepository().findAll(example);
        } else {
           log.warn("entity not implement FindEntity annotation. dynamic find not supported");
           return getRepository().findAll();
        }
    }

    public Page<Entity> find(Entity filterBy, Pageable pageable) {
        if (getEntityClass().isAnnotationPresent(Find.class)) {
            Example<Entity> example = getDefaultExample(filterBy);
            return getRepository().findAll(example, pageable);
        } else {
            log.warn("entity not implement FindEntity annotation. dynamic find not supported");
            return getRepository().findAll(pageable);
        }
    }

    public Page<Entity> find(String query, Pageable pageable) {
        List<FilterCriteria> criteries = new ArrayList<>();
        Pattern pattern = Pattern.compile(
                "(\\w+.?\\w*)(>=|<=|=|!=|\\$like|\\$notLike|\\$isNull|\\$isNotNull)([\\w]{8}(-[\\w]{4}){3}-[\\w]{12}|\\w+-?\\w*),",
                Pattern.UNICODE_CHARACTER_CLASS);
        Matcher matcher = pattern.matcher(query + ",");

        while (matcher.find()) {
            criteries.add(new FilterCriteria(matcher.group(1),
                    matcher.group(2), matcher.group(3)));
        }
        return find(criteries, pageable);
    }

    public List<Entity> find(List<FilterCriteria> criteries) {
        return getRepository().findAll(getDefaultSpecification(criteries));
    }

    public Page<Entity> find(List<FilterCriteria> criteries, Pageable pageable) {
        return getRepository().findAll(getDefaultSpecification(criteries), pageable);
    }

    @SuppressWarnings("Duplicates")
    public List<Entity> findAll() {
        if (getEntityClass().isAnnotationPresent(NoDelete.class)) {
            NoDelete noDelete = getEntityClass().getAnnotation(NoDelete.class);

            Specification specification;
            try {
                Field field = getEntityClass().getDeclaredField(noDelete.field());
                specification = Specification.where(hasField(noDelete.field(),
                        ReflectionUtils.getObject(field, noDelete.defaultValue())));
            } catch (Exception e) {
                throw new IllegalArgumentException("error in identify noDeleteEntity field for findAll", e);
            }
            return getRepository().findAll(specification);
        } else {
            return getRepository().findAll();
        }
    }

    @SuppressWarnings("Duplicates")
    public Page<Entity> findAllPages(Pageable pageable) {
        if (getEntityClass().isAnnotationPresent(NoDelete.class)) {
            NoDelete noDelete = getEntityClass().getAnnotation(NoDelete.class);

            Specification specification;
            try {
                Field field = getEntityClass().getDeclaredField(noDelete.field());
                specification = Specification.where(hasField(noDelete.field(),
                        ReflectionUtils.getObject(field, noDelete.defaultValue())));
            } catch (Exception e) {
                throw new IllegalArgumentException("error in identify noDeleteEntity field for findAllPages", e);
            }
            return getRepository().findAll(specification, pageable);

        } else {
            return getRepository().findAll(pageable);
        }
    }

    Specification<Entity> hasField(String field, Object value) {
        return (root, cq, cb) -> cb.equal(root.get(field), value);
    }

    Specification<Entity> getDefaultSpecification(List<FilterCriteria> criteries) {
        SpecificationsBuilder<Entity> builder = new SpecificationsBuilder<>();
        criteries.forEach(builder::with);

        Specification<Entity> spec = builder.build();

        boolean ignoreNoDeleteAnnotation = false;
        if (getEntityClass().isAnnotationPresent(Find.class)) {
            Find find = getEntityClass().getAnnotation(Find.class);
            ignoreNoDeleteAnnotation = find.ignoreNoDeleteAnnotation();
        }

        if (!ignoreNoDeleteAnnotation && getEntityClass().isAnnotationPresent(NoDelete.class)) {
            NoDelete noDelete = getEntityClass().getAnnotation(NoDelete.class);
            try {
                Field field = getEntityClass().getDeclaredField(noDelete.field());
                spec = Specification.where(spec).and(hasField(noDelete.field(),
                        ReflectionUtils.getObject(field, noDelete.defaultValue())));
            } catch (Exception e) {
                log.error(e.getMessage(), e);
            }
        }
        return spec;
    }

    Example<Entity> getDefaultExample(Entity entity) {
        ExampleMatcher matcher = ExampleMatcher.matching()
                //.withIgnorePaths("id")
                .withIgnoreNullValues()
                .withStringMatcher(ExampleMatcher.StringMatcher.EXACT)
                .withIgnoreCase();

        boolean ignoreNoDeleteAnnotation = false;
        if (getEntityClass().isAnnotationPresent(Find.class)) {
            Find find = getEntityClass().getAnnotation(Find.class);
            ignoreNoDeleteAnnotation = find.ignoreNoDeleteAnnotation();

            Set<String> ignores = new HashSet<>();
            if (matcher.getIgnoredPaths() != null && !matcher.getIgnoredPaths().isEmpty()) {
                ignores.addAll(matcher.getIgnoredPaths());
            }

            if (find.ignoreAttributes().length > 0) {
                ignores.addAll(Arrays.asList(find.ignoreAttributes()));
            }

            if (!ignoreWithFindAttributeAnnotation.isEmpty()) {
                ignores.addAll(ignoreWithFindAttributeAnnotation
                        .stream()
                        .map(Field::getName)
                        .collect(Collectors.toList())
                );
            }

            if (!ignores.isEmpty()) {
                matcher = matcher.withIgnorePaths(ignores.toArray(new String[0]));
            }
        }

        if (!ignoreNoDeleteAnnotation && getEntityClass().isAnnotationPresent(NoDelete.class)) {
            NoDelete noDelete = getEntityClass().getAnnotation(NoDelete.class);
            try {

                Field field = getEntityClass().getDeclaredField(noDelete.field());
                field.setAccessible(true);
                field.set(entity, ReflectionUtils.getObject(field, noDelete.defaultValue()));

                matcher = matcher.withMatcher(noDelete.field(), ExampleMatcher.GenericPropertyMatchers.exact());
            } catch (Exception e) {
                log.error(e.getMessage(), e);
            }
        }
        return Example.of(entity, matcher);
    }

    ExampleMatcher getDefaultNoDeleteMatcher() {
        if (getEntityClass().isAnnotationPresent(NoDelete.class)) {
            NoDelete noDelete = getEntityClass().getAnnotation(NoDelete.class);
            try {
                Entity entity = getEntityClass().newInstance();
                Field field = getEntityClass().getDeclaredField(noDelete.field());
                field.setAccessible(true);
                field.set(entity, 1);

                String[] fields = Arrays.stream(getEntityClass().getDeclaredFields())
                        .filter(item -> !item.getName().equals(noDelete.field()))
                        .map(Field::getName).toArray(String[]::new);

                return ExampleMatcher.matching()
                        .withMatcher(noDelete.field(), ExampleMatcher.GenericPropertyMatchers.exact())
                        .withIgnorePaths(fields);
            } catch (Exception e) {
                log.error(e.getMessage(), e);
            }
        }
        return null;
    }

}
