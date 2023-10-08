package com.eoscode.springapitools.data.filter;

import com.eoscode.springapitools.config.StringCaseSensitive;
import com.eoscode.springapitools.util.ObjectUtils;

import javax.persistence.criteria.*;

@SuppressWarnings("rawtypes")
public class DefaultSpecification<T> implements org.springframework.data.jpa.domain.Specification<T> {

    private Join join;
    private final FilterDefinition criteria;
    private StringCaseSensitive stringCaseSensitive;

    @SuppressWarnings("NullableProblems")
    @Override
    public Predicate toPredicate(Root<T> root, CriteriaQuery<?> criteriaQuery, CriteriaBuilder criteriaBuilder) {
        return build(criteria, root, criteriaQuery, criteriaBuilder);
    }

    public DefaultSpecification(FilterDefinition filterCriteria) {
        this.criteria = filterCriteria;
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

    public void withStringIgnoreCase(StringCaseSensitive stringCaseSensitive) {
        this.stringCaseSensitive = stringCaseSensitive;
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

        if (criteria.getOperator().equalsIgnoreCase(Operation.EQ.getValue())) {
            if (path.getJavaType() == String.class) {
                return criteriaBuilder.equal(getStringIgnoreCase(criteriaBuilder, path),
                        (Comparable) ObjectUtils.getObject(javaType, criteria.getValue()));
            } else {
                return criteriaBuilder.equal(path, (Comparable) ObjectUtils.getObject(javaType, criteria.getValue()));
            }
        } else if (criteria.getOperator().equalsIgnoreCase(Operation.NE.getValue())) {
            if (path.getJavaType() == String.class) {
                return criteriaBuilder.notEqual(getStringIgnoreCase(criteriaBuilder, path),
                        (Comparable) ObjectUtils.getObject(javaType, criteria.getValue()));
            } else {
                return criteriaBuilder.notEqual(path, (Comparable) ObjectUtils.getObject(javaType, criteria.getValue()));
            }
        } else if (criteria.getOperator().equalsIgnoreCase(Operation.GT.getValue())) {
            if (path.getJavaType() == String.class) {
                return criteriaBuilder.greaterThan(getStringIgnoreCase(criteriaBuilder, path),
                        (Comparable) ObjectUtils.getObject(javaType, criteria.getValue()));
            } else {
                return criteriaBuilder.greaterThan(path, (Comparable) ObjectUtils.getObject(javaType, criteria.getValue()));
            }
        } else if (criteria.getOperator().equalsIgnoreCase(Operation.GTE.getValue())) {
            if (path.getJavaType() == String.class) {
                return criteriaBuilder.greaterThanOrEqualTo(getStringIgnoreCase(criteriaBuilder, path),
                        (Comparable) ObjectUtils.getObject(javaType, criteria.getValue()));
            } else {
                return criteriaBuilder.greaterThanOrEqualTo(path, (Comparable) ObjectUtils.getObject(javaType, criteria.getValue()));
            }
        } else if (criteria.getOperator().equalsIgnoreCase(Operation.LT.getValue())) {
            if (path.getJavaType() == String.class) {
                return criteriaBuilder.lessThan(getStringIgnoreCase(criteriaBuilder, path),
                        (Comparable) ObjectUtils.getObject(javaType, criteria.getValue()));
            } else {
                return criteriaBuilder.lessThan(path, (Comparable) ObjectUtils.getObject(javaType, criteria.getValue()));
            }
        } else if (criteria.getOperator().equalsIgnoreCase(Operation.LTE.getValue())) {
            if (path.getJavaType() == String.class) {
                return criteriaBuilder.lessThanOrEqualTo(getStringIgnoreCase(criteriaBuilder, path),
                        (Comparable) ObjectUtils.getObject(javaType, criteria.getValue()));
            } else {
                return criteriaBuilder.lessThanOrEqualTo(path, (Comparable) ObjectUtils.getObject(javaType, criteria.getValue()));
            }
        } else if (criteria.getOperator().equalsIgnoreCase(Operation.LIKE.getValue())) {
            if (path.getJavaType() == String.class) {
                return criteriaBuilder.like(getStringIgnoreCase(criteriaBuilder, path),
                        "%" + ObjectUtils.getObject(javaType, criteria.getValue()) + "%");
            } else {
                return criteriaBuilder.equal(path, (Comparable) ObjectUtils.getObject(javaType, criteria.getValue()));
            }
        } else if (criteria.getOperator().equalsIgnoreCase(Operation.NOT_LIKE.getValue())) {
            if (path.getJavaType() == String.class) {
                return criteriaBuilder.notLike(getStringIgnoreCase(criteriaBuilder, path),
                        "%" + ObjectUtils.getObject(javaType, criteria.getValue()) + "%");
            } else {
                return criteriaBuilder.notEqual(path, (Comparable) ObjectUtils.getObject(javaType, criteria.getValue()));
            }
        } else if (criteria.getOperator().equalsIgnoreCase(Operation.STARTS_WITH.getValue())) {
            return criteriaBuilder.like(getStringIgnoreCase(criteriaBuilder, path), ObjectUtils.getObject(javaType, criteria.getValue()) + "%");
        } else if (criteria.getOperator().equalsIgnoreCase(Operation.ENDS_WITH.getValue())) {
            return criteriaBuilder.like(getStringIgnoreCase(criteriaBuilder, path), "%" + ObjectUtils.getObject(javaType, criteria.getValue()));
        } else if (criteria.getOperator().equalsIgnoreCase(Operation.IS_NULL.getValue())) {
            return criteriaBuilder.isNull(path);
        } else if (criteria.getOperator().equalsIgnoreCase(Operation.IS_NOT_NULL.getValue())) {
            return criteriaBuilder.isNotNull(path);
        } else if (criteria.getOperator().equalsIgnoreCase(Operation.BTW.getValue())) {
            String[] values = criteria.getValue().toString().split(";");
            return criteriaBuilder.between(path,
                    (Comparable) ObjectUtils.getObject(path.getJavaType(), (Object) values[0]),
                     ObjectUtils.getObject(path.getJavaType(), (Object) values[1]));
        } else if (criteria.getOperator().equalsIgnoreCase(Operation.IN.getValue())) {
            String[] values = criteria.getValue().toString().split(";");
            CriteriaBuilder.In inClause = criteriaBuilder.in(path);
            for (Object in : values) {
                inClause.value((Comparable) ObjectUtils.getObject(path.getJavaType(), in));
            }
            return inClause;
        } else if (criteria.getOperator().equalsIgnoreCase(Operation.SIZE.getValue())) {
            String[] values = criteria.getValue().toString().split(";");
            Expression expression = criteriaBuilder.size(path);
            return buildExpression(values[0], expression, values[1], criteriaBuilder);
        } else if (criteria.getOperator().equalsIgnoreCase(Operation.IS_EMPTY.getValue())) {
            return criteriaBuilder.isEmpty(path);
        } else if (criteria.getOperator().equalsIgnoreCase(Operation.IS_NOT_EMPTY.getValue())) {
            return criteriaBuilder.isNotEmpty(path);
        }
        return null;
    }

    @SuppressWarnings("unchecked")
    private Predicate buildExpression(String operation, Expression expression, String value, CriteriaBuilder criteriaBuilder) {
        if (operation.equalsIgnoreCase(Operation.EQ.getValue())) {
            return criteriaBuilder.equal(expression, value);
        } else if (operation.equalsIgnoreCase(Operation.NE.getValue())) {
            return criteriaBuilder.notEqual(expression, value);
        } else if (operation.equalsIgnoreCase(Operation.GT.getValue())) {
            return criteriaBuilder.greaterThan(expression, value);
        } else if (operation.equalsIgnoreCase(Operation.GTE.getValue())) {
            return criteriaBuilder.greaterThanOrEqualTo(expression, value);
        } else if (operation.equalsIgnoreCase(Operation.LT.getValue())) {
            return criteriaBuilder.lessThan(expression, value);
        } else if (operation.equalsIgnoreCase(Operation.LTE.getValue())) {
            return criteriaBuilder.lessThanOrEqualTo(expression, value);
        }
        return null;
    }

    private Expression<String> getStringIgnoreCase(CriteriaBuilder criteriaBuilder, Expression<String> expression) {
        switch (stringCaseSensitive) {
            case lowerCase: return criteriaBuilder.lower(expression);
            case upperCase: return criteriaBuilder.upper(expression);
            default: return expression;
        }
    }

}
