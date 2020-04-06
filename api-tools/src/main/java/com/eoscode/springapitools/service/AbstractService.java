package com.eoscode.springapitools.service;

import com.eoscode.springapitools.data.domain.Find;
import com.eoscode.springapitools.data.domain.FindAttribute;
import com.eoscode.springapitools.data.domain.Identifier;
import com.eoscode.springapitools.data.domain.NoDelete;
import com.eoscode.springapitools.data.filter.FilterDefinition;
import com.eoscode.springapitools.data.filter.QueryDefinition;
import com.eoscode.springapitools.data.filter.SortDefinition;
import com.eoscode.springapitools.data.filter.SpecificationBuilder;
import com.eoscode.springapitools.data.repository.CustomDeleteByIdRepository;
import com.eoscode.springapitools.data.repository.CustomFindByIdRepository;
import com.eoscode.springapitools.service.exceptions.EntityNotFoundException;
import com.eoscode.springapitools.util.NullAwareBeanUtilsBean;
import com.eoscode.springapitools.util.ReflectionUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.PostConstruct;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.reflections.ReflectionUtils.*;

@SuppressWarnings({"unchecked", "rawtypes"})
public abstract class AbstractService<Repository extends com.eoscode.springapitools.data.repository.Repository<Entity, ID>, Entity, ID> {

    private final Log log = LogFactory.getLog(this.getClass());

    @Autowired
    private CustomFindByIdRepository customFindByIdRepository;

    @Autowired
    private CustomDeleteByIdRepository customDeleteByIdRepository;

    @Autowired
    private ApplicationContext applicationContext;

    private Repository repository;

    private Type repositoryType;
    private Type entityType;
    private Type identifierType;
    private Class<Entity> entityClass;

    private Set<Field> findAttributeAnnotations = new HashSet<>();

    public AbstractService() {
        Type type = getClass().getGenericSuperclass();
        ParameterizedType pType = (ParameterizedType) type;

        repositoryType = pType.getActualTypeArguments()[0];
        entityType =  pType.getActualTypeArguments()[1];
        identifierType = pType.getActualTypeArguments()[2];

        entityClass = (Class<Entity>) entityType;
    }

    public AbstractService(ApplicationContext applicationContext,
                           Type repositoryType, Type entityType, Type identifierType) {
        this.applicationContext = applicationContext;
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

    private Class<Entity> getEntityClass() {
        return entityClass;
    }

    protected Repository getRepository() {
        return repository;
    }

    @PostConstruct
    private void metaData() {

        findAttributeAnnotations = getAllFields(entityClass, withAnnotation(FindAttribute.class));

        if (applicationContext != null) {

            // if not repository, get default repository in context
            if (getRepository() == null) {
                repository = applicationContext.getBean((Class<Repository>) getRepositoryType());
            }

            if (customFindByIdRepository == null) {
                customFindByIdRepository = applicationContext.getBean(CustomFindByIdRepository.class);
            }

            if (customDeleteByIdRepository == null) {
                customDeleteByIdRepository = applicationContext.getBean(CustomDeleteByIdRepository.class);
            }
        }

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
            return getRepository().save(entityOld);
        } else {
            return getRepository().save(entity);
        }

    }

    public Entity findById(ID id) throws EntityNotFoundException {

        EntityNotFoundException objectNotFound = new EntityNotFoundException(
                "Object not found! Id: " + id + ", Type: " + getEntityType().getTypeName ());

        Optional<Entity> result = customFindByIdRepository.findCustomById(getEntityClass(), id);
        return result.orElse(getRepository().findById(id).orElseThrow(() -> objectNotFound));

    }

    public Entity findDetailById(ID id) throws EntityNotFoundException {
        Class<Entity> classType = (Class<Entity>) entityType;
        Optional<Entity> result = customFindByIdRepository.findDetailById(classType, id);
        return result.orElseGet(() -> findById(id));
    }

    public boolean existsById(ID id) {
        return getRepository().existsById(id);
    }

    @Transactional
    public void deleteById(ID id) {
        Entity entity = findById(id);
        Class<Entity> classEntity = (Class<Entity>) entityType;
        if (classEntity.isAnnotationPresent(NoDelete.class)) {
           customDeleteByIdRepository.deleteById(classEntity, id);
        } else {
            getRepository().deleteById(id);
        }
    }

    @Transactional
    public void delete(Entity entity) {
        Class<Entity> classEntity = (Class<Entity>) entityType;
        if (classEntity.isAnnotationPresent(NoDelete.class)) {
            ID id =  ((Identifier<ID>) entity).getId();
            customDeleteByIdRepository.deleteById(classEntity, id);
        } else {
            getRepository().delete(entity);
        }
    }

    public List<Entity> find(Entity filterBy) {
        Example<Entity> example = getDefaultExample(filterBy);
        return getRepository().findAll(example);
    }

    public List<Entity> find(Entity filterBy, Sort sort) {
        Example<Entity> example = getDefaultExample(filterBy);
        if (sort != null) {
            return getRepository().findAll(example, sort);
        } else {
            return getRepository().findAll(example);
        }
    }

    public Page<Entity> find(Entity filterBy, Pageable pageable) {
        Example<Entity> example = getDefaultExample(filterBy);
        return getRepository().findAll(example, pageable);
    }

    public Page<Entity> query(String query, Pageable pageable, Boolean distinct) {
        List<FilterDefinition> criteria = new ArrayList<>();
        Pattern pattern = Pattern.compile(
                "(\\w+.?\\w*[^><!=])(>=|<=|=|!=|>|<|\\$like|\\$notLike|\\$isNull|\\$isNotNull)([\\w]{8}(-[\\w]{4}){3}-[\\w]{12}|\\w+-?\\w*),",
                Pattern.UNICODE_CHARACTER_CLASS);
        Matcher matcher = pattern.matcher(query + ",");

        while (matcher.find()) {
            criteria.add(new FilterDefinition(matcher.group(1),
                    matcher.group(2), matcher.group(3)));
        }
        QueryDefinition queryDefinition = new QueryDefinition();
        queryDefinition.setDistinct(distinct);
        queryDefinition.setFilters(criteria);
        return query(queryDefinition, pageable);
    }

    public List<Entity> query(QueryDefinition queryDefinition) {
        return getRepository().findAll(getDefaultSpecification(queryDefinition));
    }

    public Page<Entity> query(QueryDefinition queryDefinition, Pageable pageable) {
        pageable = getDefaultSortAndPageRequest(queryDefinition.getSorts(), pageable);
        return getRepository().findAll(getDefaultSpecification(queryDefinition), pageable);
    }

    @SuppressWarnings("Duplicates")
    public List<Entity> findAll() {
        return findAll(Sort.unsorted());
    }

    public List<Entity> findAll(Sort sort) {
        Specification specification = null;
        if (getEntityClass().isAnnotationPresent(NoDelete.class)) {
            NoDelete noDelete = getEntityClass().getAnnotation(NoDelete.class);
            try {
                Field field = getEntityClass().getDeclaredField(noDelete.field());
                specification = Specification.where(hasField(noDelete.field(),
                        ReflectionUtils.getObject(field, noDelete.defaultValue())));
            } catch (Exception e) {
                throw new IllegalArgumentException("error in identify noDeleteEntity field for findAll", e);
            }
        }

        return getRepository().findAll(specification, sort);

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

    Specification<Entity> getDefaultSpecification(QueryDefinition queryDefinition) {
        List<FilterDefinition> criteria = queryDefinition.getFilters();

        SpecificationBuilder<Entity> builder = new SpecificationBuilder<>();
        builder.distinct(queryDefinition.isDistinct());
        builder.sorts(queryDefinition.getSorts());
        criteria.forEach(builder::filter);

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
                .withStringMatcher(ExampleMatcher.StringMatcher.EXACT)
                .withIgnoreCase();

        Set<String> ignores = new HashSet<>();
        Set<String> supportedDefaultValue = new HashSet<>();
        boolean ignoreNoDeleteAnnotation = false;
        boolean ignoreDefaultValue = true;

        if (getEntityClass().isAnnotationPresent(Find.class)) {
            Find findAnnotation = getEntityClass().getAnnotation(Find.class);
            ignoreNoDeleteAnnotation = findAnnotation.ignoreNoDeleteAnnotation();
            ignoreDefaultValue = findAnnotation.ignoreDefaultValue();
            supportedDefaultValue.addAll(Arrays.asList(findAnnotation.supportedDefaultValueForAttributes()));

            if (matcher.getIgnoredPaths() != null && !matcher.getIgnoredPaths().isEmpty()) {
                ignores.addAll(matcher.getIgnoredPaths());
            }

            if (findAnnotation.ignoreAttributes().length > 0) {
                ignores.addAll(Arrays.asList(findAnnotation.ignoreAttributes()));
            }
        }

        if (!findAttributeAnnotations.isEmpty()) {
            findAttributeAnnotations.forEach(field -> {
                FindAttribute findAttributeAnnotation = field.getAnnotation(FindAttribute.class);
                if (findAttributeAnnotation.ignore()) {
                    ignores.add(field.getName());
                } else {
                    if (!findAttributeAnnotation.supportedDefaultValue()) {
                        supportedDefaultValue.add(field.getName());
                    }
                }
            });
        }

        // ignore default value
        if (ignoreDefaultValue) {
            matcher = matcher.withIgnoreNullValues();
            getFields(entityClass, field -> (field.getType() == int.class
                    || field.getType() == long.class || field.getType() == boolean.class))
                .forEach(field -> {
                    field.setAccessible(true);
                    try {
                        if (field.getType() == int.class) {
                            if (((int) field.get(entity)) == 0) {
                                ignores.add(field.getName());
                            }
                        } else if (field.getType() == long.class) {
                            if (((long) field.get(entity)) == 0) {
                                ignores.add(field.getName());
                            }
                        } else if (field.getType() == boolean.class) {
                            if (((long) field.get(entity)) == 0) {
                                ignores.add(field.getName());
                            }
                        }
                    } catch (IllegalAccessException e) {
                        log.error(e.getMessage());
                    }
                });
        }

        ignores.removeAll(supportedDefaultValue);
        if (!ignores.isEmpty()) {
            matcher = matcher.withIgnorePaths(ignores.toArray(new String[0]));
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

    private List<Sort.Order> getDefaultSort(List<SortDefinition> sorts) {
        List<Sort.Order> orders = new ArrayList<>();
        if (sorts != null && sorts.size() > 0) {
            sorts.forEach(sortDefinition -> orders.add(Sort.Order.asc(sortDefinition.getField())));
        }
        return orders;
    }

    private Pageable getDefaultSortAndPageRequest(List<SortDefinition> sorts, Pageable pageable) {
        if (pageable == null) {
            return null;
        }
        List<Sort.Order> orders = getDefaultSort(sorts);
        return PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), Sort.by(orders));
    }

}
