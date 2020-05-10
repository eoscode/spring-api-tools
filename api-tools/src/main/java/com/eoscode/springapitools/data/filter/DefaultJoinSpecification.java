package com.eoscode.springapitools.data.filter;

import javax.persistence.criteria.*;

@SuppressWarnings("rawtypes")
public class DefaultJoinSpecification<T> implements org.springframework.data.jpa.domain.Specification<T> {

    private Join join;

    public DefaultJoinSpecification(Join join) {
        this.join = join;
    }

    @SuppressWarnings("NullableProblems")
    @Override
    public Predicate toPredicate(Root<T> root, CriteriaQuery<?> criteriaQuery, CriteriaBuilder criteriaBuilder) {
        return join.getOn();
    }

}
