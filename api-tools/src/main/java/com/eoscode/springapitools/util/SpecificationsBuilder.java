package com.eoscode.springapitools.util;

import com.eoscode.springapitools.data.domain.filter.DefaultSpecification;
import com.eoscode.springapitools.data.domain.filter.FilterCriteria;
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
public class SpecificationsBuilder<T> {

    private final List<FilterCriteria> criteries;
    private final Map<String, List<FilterCriteria>> joinCriteries;

    private Specification<T> result = null;

    public SpecificationsBuilder() {
        criteries = new ArrayList<>();
        joinCriteries = new HashMap<>();
    }

    public SpecificationsBuilder with(String field, String operation, Object value) {
        with(new FilterCriteria(field, operation, value));
        return this;
    }

    public SpecificationsBuilder with(FilterCriteria filterCriteria) {
        String[] fields = filterCriteria.getField().split("\\.");
        if (fields.length == 2) {
            filterCriteria.setField(fields[1]);

            List<FilterCriteria> filters = joinCriteries.computeIfAbsent(fields[0], k -> new ArrayList<>());
            filters.add(filterCriteria);
            joinCriteries.put(fields[0], filters);
        } else {
            criteries.add(filterCriteria);
        }
        return this;
    }

    public Specification<T> build() {
        if (criteries.size() == 0 && joinCriteries.size() == 0) {
            return null;
        }

        List<Specification<T>> specs = criteries.stream()
                .map(DefaultSpecification<T>::new)
                .collect(Collectors.toList());

        if (criteries.size() > 0) {
            result = specs.get(0);
        }

        for (int i = 1; i < criteries.size(); i++) {
            result = Specification.where(result).and(specs.get(i));
        }
        //result = Specification.where(where(criteries));

        joinCriteries.forEach((key, filters) -> {
            if (result == null) {
                result = Specification.where(join(key, filters));
            } else {
                result = Specification.where(result).and(join(key, filters));
            }
        });

        return result;
    }

    @SuppressWarnings("unchecked")
    Specification<T> where(List<FilterCriteria> filters) {
        return (root, query, builder) -> {
            List<Specification> specs = filters.stream()
                    .map(DefaultSpecification::new)
                    .collect(Collectors.toList());

            return builder.and(specs.stream().map(item -> item.toPredicate(root, query, builder)).toArray(Predicate[]::new));
        };
    }

    @SuppressWarnings("unchecked")
    Specification<T> join(String field, List<FilterCriteria> filters) {
        return (root, query, builder) -> {
            final Join join = root.join(field, JoinType.LEFT);
            List<Specification> specs = filters.stream()
                    .map(filter -> new DefaultSpecification(join, filter))
                    .collect(Collectors.toList());

            return builder.and(specs.stream().map(item -> item.toPredicate(root, query, builder)).toArray(Predicate[]::new));
        };
    }

/*    Predicate[] where(List<FilterCriteria> filters) {
        return (root, query, builder) -> {
            List<Specification> specs = filters.stream()
                    .map(DefaultSpecification::new)
                    .collect(Collectors.toList());

            List<Predicate> predicates = new ArrayList<>();
            specs.forEach(spec -> {
                predicates.add(spec.toPredicate(root, query, builder));
            });
            return predicates;
        };
    }*/

}
