package com.eoscode.springapitools.resource;

import com.eoscode.springapitools.config.QueryView;
import com.eoscode.springapitools.config.SpringApiToolsProperties;
import com.eoscode.springapitools.data.domain.DynamicView;
import com.eoscode.springapitools.data.domain.Identifier;
import com.eoscode.springapitools.service.AbstractService;
import com.eoscode.springapitools.util.ObjectUtils;
import jakarta.annotation.PostConstruct;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.data.util.ReflectionUtils;
import org.springframework.http.HttpMethod;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

@SuppressWarnings({"Duplicates", "unchecked"})
public abstract class BaseResource<Service extends AbstractService<?, Entity, ID>, Entity, ID> {

    protected final Log log = LogFactory.getLog(this.getClass());

    @Autowired
    protected ApplicationContext applicationContext;

    @Autowired
    protected SpringApiToolsProperties springApiToolsProperties;

    @Autowired
    protected MappingJackson2HttpMessageConverter jackson2HttpMessageConverter;

    private Service service;
    protected final Set<org.springframework.http.HttpMethod> methodNotAllowed = new HashSet<>();
    protected final Set<ResourceMethod> resourceMethodNotAllowed = new HashSet<>();

    private final Type serviceType;
    private final Type entityType;
    private final Type identifierType;
    private final Class<Entity> entityClass;
    protected boolean queryWithViews = false;

    public BaseResource() {
        Type type = getClass().getGenericSuperclass();
        ParameterizedType pType = (ParameterizedType) type;

        serviceType = pType.getActualTypeArguments()[0];
        entityType = pType.getActualTypeArguments()[1];
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
            for (int i=0; i<methodNotAllowedAnnotation.methods().length; i++) {
                methodNotAllowed.add(HttpMethod.valueOf(methodNotAllowedAnnotation.methods()[i]));
            }
        }

        if (getClass().isAnnotationPresent(ResourceMethodNotAllowed.class)) {
            ResourceMethodNotAllowed resourceMethod = getClass().getAnnotation(ResourceMethodNotAllowed.class);
            resourceMethodNotAllowed.addAll(Arrays.stream(resourceMethod.resources()).collect(Collectors.toSet()));
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

    protected ID getIdentifierValue(Entity entity) throws IllegalAccessException {
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

    protected void defineIdentifierValue(Entity entity, ID id) throws IllegalAccessException {
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

    protected boolean isDefaultPageable(Boolean pageable) {
        if (pageable != null) {
            return pageable;
        }
        return springApiToolsProperties.isEnableDefaultPageable();
    }

    protected int getListDefaultSize(Integer size) {
        if (size != null && springApiToolsProperties.isListDefaultSizeOverride()) {
            return size;
        }
        return springApiToolsProperties.getListDefaultSize();
    }
}
