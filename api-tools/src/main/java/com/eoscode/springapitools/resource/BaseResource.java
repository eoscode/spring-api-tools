package com.eoscode.springapitools.resource;

import com.eoscode.springapitools.config.QueryView;
import com.eoscode.springapitools.config.SpringApiToolsProperties;
import com.eoscode.springapitools.data.domain.DynamicView;
import com.eoscode.springapitools.service.AbstractService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;

import javax.annotation.PostConstruct;
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

    @Autowired
    protected ViewToJson viewToJson;

    private Service service;
    protected final Set<org.springframework.http.HttpMethod> methodNotAllowed = new HashSet<>();

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
