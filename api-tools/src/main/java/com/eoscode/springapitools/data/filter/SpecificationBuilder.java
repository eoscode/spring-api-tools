package com.eoscode.springapitools.data.filter;

import org.springframework.data.jpa.domain.Specification;

import javax.persistence.criteria.Join;
import javax.persistence.criteria.JoinType;
import javax.persistence.criteria.Predicate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@SuppressWarnings("rawtypes")
public class SpecificationBuilder<T> {

    private Boolean distinct;
    private final List<FilterDefinition> filters;
    private final Map<String, List<FilterDefinition>> joins;
    private final List<SortDefinition> sorts;
    private DefaultSpecification.Operator operator;

    private Specification<T> result = null;

    public SpecificationBuilder() {
        filters = new ArrayList<>();
        joins = new HashMap<>();
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

    public SpecificationBuilder filter(String field, String operation, Object value) {
        filter(new FilterDefinition(field, operation, value));
        return this;
    }

    public SpecificationBuilder filter(FilterDefinition filter) {
        String[] fields = filter.getField().split("\\.");
        if (fields.length == 2) {
            filter.setField(fields[1]);

            List<FilterDefinition> filters = joins.computeIfAbsent(fields[0], k -> new ArrayList<>());
            filters.add(filter);
            joins.put(fields[0], filters);
        } else {
            filters.add(filter);
        }
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
        if (filters.size() == 0 && joins.size() == 0) {
            return null;
        }

        if (filters.size() > 0) {
            result = where(filters);
        }

        joins.forEach((key, filters) -> {
            if (result == null) {
                result = Specification.where(join(key, filters));
            } else {
                if (operator == DefaultSpecification.Operator.OR) {
                    result = Specification.where(result).or(join(key, filters));
                } else {
                    result = Specification.where(result).and(join(key, filters));
                }
            }
        });

        return result;
    }

    @SuppressWarnings("unchecked")
    Specification<T> where(List<FilterDefinition> filters) {
        return (root, query, builder) -> {

            if (distinct != null) {
                query.distinct(distinct);
            }

            List<Specification> specs = filters.stream()
                    .map(DefaultSpecification::new)
                    .collect(Collectors.toList());

            Predicate[] predicates = specs.stream().map(item -> item.toPredicate(root, query, builder))
                    .toArray(Predicate[]::new);
            if (operator == DefaultSpecification.Operator.OR) {
                return builder.or(predicates);
            } else {
                return builder.and(predicates);
            }
        };
    }

    @SuppressWarnings("unchecked")
    Specification<T> join(String field, List<FilterDefinition> filters) {
        return (root, query, builder) -> {

            if (distinct != null) {
                query.distinct(distinct);
            }
            final Join join = root.join(field, JoinType.LEFT);
            List<Specification> specs = filters.stream()
                    .map(filter -> new DefaultSpecification(join, filter))
                    .collect(Collectors.toList());

            Predicate[] predicates = specs.stream().map(item -> item.toPredicate(root, query, builder))
                    .toArray(Predicate[]::new);
            if (operator == DefaultSpecification.Operator.OR) {
                return builder.or(predicates);
            } else {
                return builder.and(predicates);
            }
        };
    }

}
