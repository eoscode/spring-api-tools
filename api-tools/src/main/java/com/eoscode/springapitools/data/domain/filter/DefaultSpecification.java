package com.eoscode.springapitools.data.domain.filter;

import javax.persistence.criteria.*;

public class DefaultSpecification<T> implements org.springframework.data.jpa.domain.Specification<T> {

    private Join join;
    private FilterCriteria criteria;

    public DefaultSpecification(FilterCriteria filterCriteria) {
        this.criteria = filterCriteria;
    }

    public DefaultSpecification(Join join, FilterCriteria filterCriteria) {
        this.join = join;
        this.criteria = filterCriteria;
    }

    @SuppressWarnings("unchecked")
    @Override
    public Predicate toPredicate(Root<T> root, CriteriaQuery<?> criteriaQuery, CriteriaBuilder criteriaBuilder) {
        Path path;
        if (join != null) {
            path = join.get(criteria.getField());
        } else {
            path = root.get(criteria.getField());
        }
        Class<?> javaType = root.get(criteria.getField()).getJavaType();

        if (criteria.getOperator().equalsIgnoreCase(Operator.EQ.getValue())) {
            if (javaType == boolean.class || javaType == Boolean.class) {
                return criteriaBuilder.equal(path, Boolean.parseBoolean(criteria.getValue().toString()));
            } else {
                return criteriaBuilder.equal(path, criteria.getValue());
            }
        } else if (criteria.getOperator().equalsIgnoreCase(Operator.NE.getValue())) {
            return criteriaBuilder.notEqual(path, criteria.getValue().toString());
        } else if (criteria.getOperator().equalsIgnoreCase(Operator.GT.getValue())) {
            return criteriaBuilder.greaterThan(path, criteria.getValue().toString());
        } else if (criteria.getOperator().equalsIgnoreCase(Operator.GTE.getValue())) {
            return criteriaBuilder.greaterThanOrEqualTo(path, criteria.getValue().toString());
        } else if (criteria.getOperator().equalsIgnoreCase(Operator.LT.getValue())) {
            return criteriaBuilder.lessThan(path, criteria.getValue().toString());
        } else if (criteria.getOperator().equalsIgnoreCase(Operator.LTE.getValue())) {
            return criteriaBuilder.lessThanOrEqualTo(path, criteria.getValue().toString());
        } else if (criteria.getOperator().equalsIgnoreCase(Operator.LIKE.getValue())) {
            if (root.get(criteria.getField()).getJavaType() == String.class) {
                return criteriaBuilder.like(path, "%" + criteria.getValue() + "%");
            } else {
                return criteriaBuilder.equal(path, criteria.getValue());
            }
        } else if (criteria.getOperator().equalsIgnoreCase(Operator.NOT_LIKE.getValue())) {
            if (root.get(criteria.getField()).getJavaType() == String.class) {
                return criteriaBuilder.notLike(path, "%" + criteria.getValue() + "%");
            } else {
                return criteriaBuilder.notEqual(path, criteria.getValue());
            }
        } else if (criteria.getOperator().equalsIgnoreCase(Operator.IS_NULL.getValue())) {
            return criteriaBuilder.isNull(path);
        } else if (criteria.getOperator().equalsIgnoreCase(Operator.NOT_NULL.getValue())) {
            return criteriaBuilder.isNotNull(path);
        }
        return null;
    }

    enum Operator {
        EQ("="),
        NE("!="),
        GT(">"),
        LT("<"),
        GTE(">="),
        LTE("<="),
        LIKE("$like"),
        NOT_LIKE("$notLike"),
        IS_NULL("$isNull"),
        NOT_NULL("$isNotNull"),
        IN("$in"),
        BTW("$btw"),
        OR("$or"),
        AND("$and");

        Operator(String value) {
            this.value = value;
        }

        private String value;
        String getValue() {
            return value;
        }

    }

}
