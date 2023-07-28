package com.eoscode.springapitools.resource;

import com.eoscode.springapitools.config.QueryView;
import com.eoscode.springapitools.config.SpringApiToolsProperties;
import com.eoscode.springapitools.data.domain.DynamicView;
import com.eoscode.springapitools.data.filter.ViewDefinition;
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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.stereotype.Component;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class DefaultViewToJson implements ViewToJson {

    protected final Log LOG = LogFactory.getLog(DefaultViewToJson.class);

    private final SpringApiToolsProperties springApiToolsProperties;
    private final MappingJackson2HttpMessageConverter jackson2HttpMessageConverter;

    public DefaultViewToJson(SpringApiToolsProperties springApiToolsProperties,
                             MappingJackson2HttpMessageConverter jackson2HttpMessageConverter) {
        this.springApiToolsProperties = springApiToolsProperties;
        this.jackson2HttpMessageConverter = jackson2HttpMessageConverter;
    }

    /**
     * Serialize Object to Json with ViewDefinition supported
     *
     * @param viewDefinition views
     * @param result Object
     * @return json
     */
    public <T> String toJson(ViewDefinition viewDefinition, T result) {

        ObjectMapper objectMapper = jackson2HttpMessageConverter
                .getObjectMapper();

        Class<T> entityClass = (Class<T>) result.getClass();

        if (!viewDefinition.getViews().isEmpty()) {
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
                                        LOG.debug(String.format("ignore field %s to query view in entity %s." +
                                                        " annotation JsonIgnore: %s, fetch: %s, dynamicView: %s",
                                                view, entityClass.getName(), fieldWithJsonIgnore, fieldWithFetch, dynamicView));
                                    }
                                    return showField;
                                } catch (NoSuchFieldException e) {
                                    LOG.error(String.format("field %s to query view in entity %s. %s",
                                            view, entityClass.getName(), e.getMessage()), e);
                                }
                                return false;
                            }
                        }).collect(Collectors.toSet());

                if (result instanceof Page) {
                    Page<T> page = (Page<T>) result;
                    List<T> content = page.getContent();
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
                LOG.error(e.getMessage(), e);
                throw new RuntimeException(e);
            }
        } else {
            try {
                return objectMapper.writeValueAsString(result);
            } catch (JsonProcessingException e) {
                LOG.error(e.getMessage(), e);
                throw new RuntimeException(e);
            }
        }
    }

}
