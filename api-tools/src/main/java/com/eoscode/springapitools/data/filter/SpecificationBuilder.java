package com.eoscode.springapitools.data.filter;

import com.eoscode.springapitools.config.StringCaseSensitive;
import org.springframework.data.jpa.domain.Specification;

import javax.persistence.criteria.*;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

@SuppressWarnings({"rawtypes", "unused", "MismatchedQueryAndUpdateOfCollection", "UnusedReturnValue"})
public class SpecificationBuilder<T> {

    private Boolean distinct;
    private final Map<String, List<FilterDefinition>> filters;
    private final Set<SortDefinition> sorts;
    private final Set<JoinDefinition> joins;
    private DefaultSpecification.Operator operator;
    private StringCaseSensitive stringCaseSensitive;

    private Specification<T> result = null;
    private final Map<String, Join> joinMap = new Hashtable<>();

    public SpecificationBuilder() {
        filters = new HashMap<>();
        sorts = new HashSet<>();
        joins = new HashSet<>();
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
        if (filter.isJoin()) {
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
            if (filters.size() > 0) {
                filters.forEach(filterDefinition -> {
                    if (filterDefinition.isFetch() || filterDefinition.isJoin()) {
                        boolean isPresent = joins.stream().anyMatch(joinDefinition -> joinDefinition.getField().equals(key));
                        if (!isPresent) {
                            joins.add(new JoinDefinition(key, filterDefinition.isFetch()));
                        }
                    }
                });
            }
        });

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

            if (currentQueryIsCountRecords(query)) {
                joinMap.clear();
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
                AtomicReference<Join> joinReference = new AtomicReference<>();

                Join join = joinMap.get(field);
                if (join == null) {
                    Optional<JoinDefinition> optional = joins.stream()
                            .filter(joinDefinition -> joinDefinition.getField().equalsIgnoreCase(field))
                            .findFirst();

                    if (!currentQueryIsCountRecords(query) && optional.isPresent()) {
                        JoinDefinition joinDefinition = optional.get();

                        JoinType joinType;
                        if (joinDefinition.getType() == JoinDefinition.JoinType.INNER) {
                            joinType = JoinType.INNER;
                        } else {
                            joinType = JoinType.LEFT;
                        }

                        if (joinDefinition.isFetch()) {
                            join = (Join) root.fetch(field, joinType);
                        } else {
                            join = root.join(field, joinType);
                        }
                    } else {
                        join = root.join(field, JoinType.LEFT);
                    }
                    joinMap.putIfAbsent(field, join);
                }
                joinReference.set(join);

                specs = filters.stream()
                        .map(filterDefinition -> {
                            DefaultSpecification defaultSpecification = new DefaultSpecification(joinReference.get(), filterDefinition);
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

    private boolean currentQueryIsCountRecords(CriteriaQuery<?> criteriaQuery) {
        return criteriaQuery.getResultType() == Long.class || criteriaQuery.getResultType() == long.class;
    }

}
