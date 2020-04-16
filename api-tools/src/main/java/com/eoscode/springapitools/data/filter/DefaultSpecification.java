package com.eoscode.springapitools.data.filter;

import com.eoscode.springapitools.util.ObjectUtils;

import javax.persistence.criteria.*;

@SuppressWarnings("rawtypes")
public class DefaultSpecification<T> implements org.springframework.data.jpa.domain.Specification<T> {

    private Join join;
    private final FilterDefinition criteria;

    public DefaultSpecification(FilterDefinition filterCriteria) {
        this.criteria = filterCriteria;
    }

    @SuppressWarnings("NullableProblems")
    @Override
    public Predicate toPredicate(Root<T> root, CriteriaQuery<?> criteriaQuery, CriteriaBuilder criteriaBuilder) {
        return build(criteria, root, criteriaQuery, criteriaBuilder);
    }

    public DefaultSpecification(Join join, FilterDefinition filterCriteria) {
        this.join = join;
        this.criteria = filterCriteria;
    }

    public String getOriginalFieldName() {
        if (join != null) {
            return join.getAttribute().getName() + "." + criteria.getField();
        } else {
            return criteria.getField();
        }
    }

    @SuppressWarnings("unchecked")
    private Predicate build(FilterDefinition criteria, Root<T> root, CriteriaQuery<?> criteriaQuery, CriteriaBuilder criteriaBuilder) {
        Path path;
        if (join != null) {
            path = join.get(criteria.getField());
        } else {
            path = root.get(criteria.getField());
        }

        Class<?> javaType = path.getJavaType();

        if (criteria.getOperator().equalsIgnoreCase(Operator.EQ.getValue())) {
            return criteriaBuilder.equal(path, (Comparable) ObjectUtils.getObject(javaType, criteria.getValue()));
        } else if (criteria.getOperator().equalsIgnoreCase(Operator.NE.getValue())) {
            return criteriaBuilder.notEqual(path, (Comparable) ObjectUtils.getObject(javaType, criteria.getValue()));
        } else if (criteria.getOperator().equalsIgnoreCase(Operator.GT.getValue())) {
            return criteriaBuilder.greaterThan(path, (Comparable) ObjectUtils.getObject(javaType, criteria.getValue()));
        } else if (criteria.getOperator().equalsIgnoreCase(Operator.GTE.getValue())) {
            return criteriaBuilder.greaterThanOrEqualTo(path, (Comparable) ObjectUtils.getObject(javaType, criteria.getValue()));
        } else if (criteria.getOperator().equalsIgnoreCase(Operator.LT.getValue())) {
            return criteriaBuilder.lessThan(path, (Comparable) ObjectUtils.getObject(javaType, criteria.getValue()));
        } else if (criteria.getOperator().equalsIgnoreCase(Operator.LTE.getValue())) {
            return criteriaBuilder.lessThanOrEqualTo(path, (Comparable) ObjectUtils.getObject(javaType, criteria.getValue()));
        } else if (criteria.getOperator().equalsIgnoreCase(Operator.LIKE.getValue())) {
            if (root.get(criteria.getField()).getJavaType() == String.class) {
                return criteriaBuilder.like(path, "%" + ObjectUtils.getObject(javaType, criteria.getValue()) + "%");
            } else {
                return criteriaBuilder.equal(path, (Comparable) ObjectUtils.getObject(javaType, criteria.getValue()));
            }
        } else if (criteria.getOperator().equalsIgnoreCase(Operator.NOT_LIKE.getValue())) {
            if (root.get(criteria.getField()).getJavaType() == String.class) {
                return criteriaBuilder.notLike(path, "%" + ObjectUtils.getObject(javaType, criteria.getValue()) + "%");
            } else {
                return criteriaBuilder.notEqual(path, (Comparable) ObjectUtils.getObject(javaType, criteria.getValue()));
            }
        } else if (criteria.getOperator().equalsIgnoreCase(Operator.STARTS_WITH.getValue())) {
            return criteriaBuilder.like(path, ObjectUtils.getObject(javaType, criteria.getValue()) + "%");
        } else if (criteria.getOperator().equalsIgnoreCase(Operator.ENDS_WITH.getValue())) {
            return criteriaBuilder.like(path, "%" + ObjectUtils.getObject(javaType, criteria.getValue()));
        } else if (criteria.getOperator().equalsIgnoreCase(Operator.IS_NULL.getValue())) {
            return criteriaBuilder.isNull(path);
        } else if (criteria.getOperator().equalsIgnoreCase(Operator.IS_NOT_NULL.getValue())) {
            return criteriaBuilder.isNotNull(path);
        } else if (criteria.getOperator().equalsIgnoreCase(Operator.BTW.getValue())) {
            String[] values = criteria.getValue().toString().split(";");
            return criteriaBuilder.between(path,
                    (Comparable) ObjectUtils.getObject(path.getJavaType(), (Object) values[0]),
                     ObjectUtils.getObject(path.getJavaType(), (Object) values[1]));
        } else if (criteria.getOperator().equalsIgnoreCase(Operator.IN.getValue())) {
            String[] values = criteria.getValue().toString().split(";");
            CriteriaBuilder.In inClause = criteriaBuilder.in(path);
            for (Object in : values) {
                inClause.value((Comparable) ObjectUtils.getObject(path.getJavaType(), in));
            }
            return inClause;
        } else if (criteria.getOperator().equalsIgnoreCase(Operator.SIZE.getValue())) {
            String[] values = criteria.getValue().toString().split(";");
            Expression expression = criteriaBuilder.size(path);
            return buildExpression(values[0], expression, values[1], criteriaBuilder);
        } else if (criteria.getOperator().equalsIgnoreCase(Operator.IS_EMPTY.getValue())) {
            return criteriaBuilder.isEmpty(path);
        } else if (criteria.getOperator().equalsIgnoreCase(Operator.IS_NOT_EMPTY.getValue())) {
            return criteriaBuilder.isNotEmpty(path);
        }
        return null;
    }

    @SuppressWarnings("unchecked")
    private Predicate buildExpression(String operator, Expression expression, String value, CriteriaBuilder criteriaBuilder) {
        if (operator.equalsIgnoreCase(Operator.EQ.getValue())) {
            return criteriaBuilder.equal(expression, value);
        } else if (operator.equalsIgnoreCase(Operator.NE.getValue())) {
            return criteriaBuilder.notEqual(expression, value);
        } else if (operator.equalsIgnoreCase(Operator.GT.getValue())) {
            return criteriaBuilder.greaterThan(expression, value);
        } else if (operator.equalsIgnoreCase(Operator.GTE.getValue())) {
            return criteriaBuilder.greaterThanOrEqualTo(expression, value);
        } else if (operator.equalsIgnoreCase(Operator.LT.getValue())) {
            return criteriaBuilder.lessThan(expression, value);
        } else if (operator.equalsIgnoreCase(Operator.LTE.getValue())) {
            return criteriaBuilder.lessThanOrEqualTo(expression, value);
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
        STARTS_WITH("$startsWith"),
        ENDS_WITH("$endsWith"),
        IS_NULL("$isNull"),
        IS_NOT_NULL("$isNotNull"),
        IS_EMPTY("$isEmpty"),
        IS_NOT_EMPTY("$isNotEmpty"),
        SIZE("$size"),
        IN("$in"),
        BTW("$btw"),
        OR("$or"),
        AND("$and");

        Operator(String value) {
            this.value = value;
        }

        private final String value;
        String getValue() {
            return value;
        }

    }

}
