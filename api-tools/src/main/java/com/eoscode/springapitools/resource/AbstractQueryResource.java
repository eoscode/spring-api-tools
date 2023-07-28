package com.eoscode.springapitools.resource;

import com.eoscode.springapitools.data.filter.QueryDefinition;
import com.eoscode.springapitools.data.filter.QueryParameter;
import com.eoscode.springapitools.resource.exception.MethodNotAllowedException;
import com.eoscode.springapitools.service.AbstractService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Set;

public abstract class AbstractQueryResource<Service extends AbstractService<?, Entity, ID>, Entity, ID>
        extends BaseResource<Service, Entity, ID> {

    public AbstractQueryResource() {
        super();
    }

    @GetMapping(value = "/query/page", produces = MediaType.APPLICATION_JSON_VALUE)
    public <T> T queryWithPage(@RequestParam(value = "opt", required = false, defaultValue = "") String query,
                               @RequestParam(value = "views", required = false, defaultValue = "") Set<String> views,
                               @PageableDefault Pageable pageable,
                               QueryParameter queryParameter) {

        if (methodNotAllowed.contains(org.springframework.http.HttpMethod.GET)) {
            throw new MethodNotAllowedException(org.springframework.http.HttpMethod.GET.name());
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

        if (methodNotAllowed.contains(org.springframework.http.HttpMethod.GET)) {
            throw new MethodNotAllowedException(org.springframework.http.HttpMethod.GET.name());
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

        if (methodNotAllowed.contains(org.springframework.http.HttpMethod.POST)) {
            throw new MethodNotAllowedException(org.springframework.http.HttpMethod.POST.name());
        }

        T result = (T) getService().query(queryDefinition, pageable);

        if (queryWithViews) {
            return (T) ResponseEntity.ok(viewToJson.toJson(queryDefinition, result));
        } else {
            return (T) ResponseEntity.ok(result);
        }
    }

    @PostMapping(value = "/query", produces = MediaType.APPLICATION_JSON_VALUE)
    public <T> T query(@RequestBody(required = false) QueryDefinition queryDefinition,
                       @RequestParam(value = "pageable", required = false) Boolean page,
                       @PageableDefault Pageable pageable) {

        if (methodNotAllowed.contains(org.springframework.http.HttpMethod.POST)) {
            throw new MethodNotAllowedException(org.springframework.http.HttpMethod.POST.name());
        }

        T result;
        if (isDefaultPageable(page)) {
            result = (T) getService().query(queryDefinition, pageable);
        } else {
            result = (T) getService().query(queryDefinition, pageable.getSort());
        }

        if (queryWithViews) {
            return (T) ResponseEntity.ok(viewToJson.toJson(queryDefinition, result));
        } else {
            return (T) ResponseEntity.ok(result);
        }
    }

}
