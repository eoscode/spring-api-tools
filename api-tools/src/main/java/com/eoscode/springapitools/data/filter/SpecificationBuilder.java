package com.eoscode.springapitools.data.filter;

import com.eoscode.springapitools.config.StringCaseSensitive;
import org.springframework.data.jpa.domain.Specification;

import javax.persistence.criteria.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@SuppressWarnings({"rawtypes", "unused", "MismatchedQueryAndUpdateOfCollection", "UnusedReturnValue"})
public class SpecificationBuilder<T> {

    private Boolean distinct;
    private final Map<String, List<FilterDefinition>> filters;
    private final List<SortDefinition> sorts;
    private DefaultSpecification.Operator operator;
    private StringCaseSensitive stringCaseSensitive;

    private Specification<T> result = null;

    public SpecificationBuilder() {
        filters = new HashMap<>();
        sorts = new ArrayList<>();
        operator = DefaultSpecification.Operator.AND;
    }

    public SpecificationBuilder(boolean distinct) {
        this();
        this.distinct = distinct;
    }

    public SpecificationBuilder distinct(boolean distinct) {
        this.distinct = distinct;
        return this;
    }

    public SpecificationBuilder withOr() {
        this.operator = DefaultSpecification.Operator.OR;
        return this;
    }

    public SpecificationBuilder withAnd() {
        this.operator = DefaultSpecification.Operator.AND;
        return this;
    }

    public SpecificationBuilder withStringIgnoreCase(StringCaseSensitive stringCaseSensitive) {
        this.stringCaseSensitive = stringCaseSensitive;
        return this;
    }

    public SpecificationBuilder filter(String field, String operation, Object value) {
        filter(new FilterDefinition(field, operation, value));
        return this;
    }

    public SpecificationBuilder filter(FilterDefinition filter) {
        String[] fields = filter.getField().split("\\.");
        String path = "";
        if (fields.length == 2) {
            path = fields[0];
            filter.setField(fields[1]);
        }

        List<FilterDefinition> filters = this.filters.computeIfAbsent(path, k -> new ArrayList<>());
        filters.add(filter);
        this.filters.put(path, filters);

        return this;
    }

    public SpecificationBuilder filters(List<FilterDefinition> filters) {
        filters.forEach(this::filter);
        return this;
    }

    public SpecificationBuilder sort(SortDefinition sort) {
        sorts.add(sort);
        return this;
    }

    public SpecificationBuilder sorts(List<SortDefinition> sorts) {
        if (sorts != null) {
            this.sorts.addAll(sorts);
        }
        return this;
    }

    public SpecificationBuilder sort(String field, SortDefinition.Direction direction) {
        this.sorts.add(new SortDefinition(field, direction));
        return this;
    }

    public Specification<T> build(QueryDefinition queryDefinition) {
        distinct(queryDefinition.isDistinct())
                .filters(queryDefinition.getFilters())
                .sorts(queryDefinition.getSorts());
        if ("or".equalsIgnoreCase(DefaultSpecification.Operator.OR.getValue())) {
            withOr();
        }
        return build();
    }

    public Specification<T> build() {
        if (filters.size() == 0) {
            return null;
        }

        filters.forEach((key, filters) -> {
            if (operator == DefaultSpecification.Operator.OR) {
                result = Specification.where(result).or(where(key, filters));
            } else {
                result = Specification.where(result).and(where(key, filters));
            }
        });

        return result;
    }

    Specification<T> where(String field, List<FilterDefinition> filters) {
        return (root, query, builder) -> {
            if (distinct != null) {
                query.distinct(distinct);
            }

            List<Specification> specs;
            if ("".equals(field)) {
                specs = filters.stream()
                        .map(filterDefinition -> {
                            DefaultSpecification defaultSpecification = new DefaultSpecification(filterDefinition);
                            defaultSpecification.withStringIgnoreCase(stringCaseSensitive);
                            return defaultSpecification;
                        })
                        .collect(Collectors.toList());
            } else {
                final Join join = root.join(field, JoinType.LEFT);
                specs = filters.stream()
                        .map(filterDefinition -> {
                            DefaultSpecification defaultSpecification = new DefaultSpecification(join, filterDefinition);
                            defaultSpecification.withStringIgnoreCase(stringCaseSensitive);
                            return defaultSpecification;
                        })
                        .collect(Collectors.toList());
            }

            Predicate[] predicates = build(specs, root, query, builder);
            if (operator == DefaultSpecification.Operator.OR) {
                return builder.or(predicates);
            } else {
                return builder.and(predicates);
            }
        };
    }

    @SuppressWarnings("unchecked")
    private Predicate[] build(List<Specification> specs, Root root, CriteriaQuery query, CriteriaBuilder builder) {
        return specs.stream().map(item -> {
            Predicate predicate = item.toPredicate(root, query, builder);
            if (predicate != null) {
                return predicate;
            } else {
                throw new SearchException(String.format("invalid filter for query, matcher for field '%s' not found.",
                        ((DefaultSpecification) item).getOriginalFieldName()));
            }
        }).toArray(Predicate[]::new);
    }

}
