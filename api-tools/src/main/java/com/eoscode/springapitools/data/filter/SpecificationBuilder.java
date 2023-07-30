package com.eoscode.springapitools.data.filter;

import com.eoscode.springapitools.config.StringCaseSensitive;
import org.springframework.data.jpa.domain.Specification;

import javax.persistence.criteria.*;
import java.util.*;
import java.util.stream.Collectors;

@SuppressWarnings({"rawtypes", "unused", "MismatchedQueryAndUpdateOfCollection", "UnusedReturnValue"})
public class SpecificationBuilder<T> {

    private Boolean distinct;
    //private final Map<String, List<FilterDefinition>> filters;
    private final List<FilterDefinition> filters;
    private final Set<SortDefinition> sorts;
    private final Set<JoinDefinition> joins;
    private Operation operation;
    private StringCaseSensitive stringCaseSensitive;

    private Specification<T> result = null;
    private final Map<String, Join> joinMap = new Hashtable<>();

    public SpecificationBuilder() {
        filters = new ArrayList<>();
        sorts = new HashSet<>();
        joins = new HashSet<>();
        operation = Operation.AND;
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
        this.operation = Operation.OR;
        return this;
    }

    public SpecificationBuilder withAnd() {
        this.operation = Operation.AND;
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

    public SpecificationBuilder filter(String field, Operation operation, Object value) {
        filter(field, operation.getValue(), value);
        return this;
    }

    public SpecificationBuilder filter(FilterDefinition filter) {
        String[] fields = filter.getField().split("\\.");
        if (filter.isJoin()) {
            filter.setField(fields[1]);
        }
        filters.add(filter);
        return this;
    }

    public SpecificationBuilder filters(List<FilterDefinition> filters) {
        filters.forEach(this::filter);
        return this;
    }

    public SpecificationBuilder join(JoinDefinition joinDefinition) {
        if (joinDefinition != null) {
            this.joins.add(joinDefinition);
        }
        return this;
    }

    public SpecificationBuilder joins(JoinDefinition[] joinDefinitions) {
        if (joins != null) {
            this.joins.addAll(Arrays.asList(joinDefinitions));
        }
        return this;
    }

    public SpecificationBuilder joins(List<JoinDefinition> joinDefinitions) {
        if (joins != null) {
            this.joins.addAll(joinDefinitions);
        }
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
                .joins(queryDefinition.getJoins())
                .filters(queryDefinition.getFilters())
                .sorts(queryDefinition.getSorts());
        if ("or".equalsIgnoreCase(Operation.OR.getValue())) {
            withOr();
        }
        return build();
    }

    public Specification<T> build() {
        filters.forEach(filterDefinition -> {
                if (filterDefinition.isFetch() || filterDefinition.isJoin()) {
                    boolean isPresent = joins.stream().anyMatch(joinDefinition -> joinDefinition.getField().equals(filterDefinition.getPathJoin()));
                    if (!isPresent) {
                        joins.add(new JoinDefinition(filterDefinition.getPathJoin(), filterDefinition.isFetch()));
                    }
                }
            });

            if (operation == Operation.OR) {
                result = Specification.where(result).or(joinAndWhere(joins, filters));
            } else {
                result = Specification.where(result).and(joinAndWhere(joins, filters));
            }

        return result;
    }

    Specification<T> joinAndWhere(Set<JoinDefinition> joins, List<FilterDefinition> filters) {
        return (root, query, builder) -> {
            if (distinct != null) {
                query.distinct(distinct);
            }

            if (currentQueryIsCountRecords(query)) {
                joinMap.clear();
            }

            joins.forEach(joinDefinition -> {
                Join join;
                JoinType joinType;
                if (joinDefinition.getType() == JoinDefinition.JoinType.INNER) {
                    joinType = JoinType.INNER;
                } else {
                    joinType = JoinType.LEFT;
                }

                if (!currentQueryIsCountRecords(query) && joinDefinition.isFetch()) {
                    join = (Join) root.fetch(joinDefinition.getField(), joinType);
                } else {
                    join = root.join(joinDefinition.getField(), joinType);
                }
                joinMap.putIfAbsent(joinDefinition.getField(), join);
            });

            List<Specification> specs = filters.stream()
                    .map(filterDefinition -> {
                        if (filterDefinition.isJoin()) {
                            Join join = joinMap.get(filterDefinition.getPathJoin());
                            DefaultSpecification defaultSpecification = new DefaultSpecification(join, filterDefinition);
                            defaultSpecification.withStringIgnoreCase(stringCaseSensitive);
                            return defaultSpecification;
                        } else {
                            DefaultSpecification defaultSpecification = new DefaultSpecification(filterDefinition);
                            defaultSpecification.withStringIgnoreCase(stringCaseSensitive);
                            return defaultSpecification;
                        }
                    })
                .collect(Collectors.toList());

            Predicate[] predicates = build(specs, root, query, builder);
            if (operation == Operation.OR) {
                return builder.or(predicates);
            } else {
                return builder.and(predicates);
            }
        };
    }

    public void prepareJoins(Set<JoinDefinition> joins, Root root, CriteriaQuery query, CriteriaBuilder builder) {

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

    private boolean currentQueryIsCountRecords(CriteriaQuery<?> criteriaQuery) {
        return criteriaQuery.getResultType() == Long.class || criteriaQuery.getResultType() == long.class;
    }

}
