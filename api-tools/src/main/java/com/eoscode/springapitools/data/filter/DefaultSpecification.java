package com.eoscode.springapitools.data.filter;

import javax.persistence.criteria.*;
import java.math.BigDecimal;

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
        } else if (criteria.getOperator().equalsIgnoreCase(Operator.STARTS_WITH.getValue())) {
            return criteriaBuilder.like(path, criteria.getValue() + "%");
        } else if (criteria.getOperator().equalsIgnoreCase(Operator.ENDS_WITH.getValue())) {
            return criteriaBuilder.like(path, "%" + criteria.getValue());
        } else if (criteria.getOperator().equalsIgnoreCase(Operator.IS_NULL.getValue())) {
            return criteriaBuilder.isNull(path);
        } else if (criteria.getOperator().equalsIgnoreCase(Operator.IS_NOT_NULL.getValue())) {
            return criteriaBuilder.isNotNull(path);
        } else if (criteria.getOperator().equalsIgnoreCase(Operator.BTW.getValue())) {
            String[] values = criteria.getValue().toString().split(";");
            if (path.getJavaType() == int.class || path.getJavaType() == Integer.class) {
                return criteriaBuilder.between(path, Integer.parseInt(values[0]),
                        Integer.parseInt(values[1]));
            } else if (path.getJavaType() == long.class || path.getJavaType() == Long.class) {
                return criteriaBuilder.between(path, Long.parseLong(values[0]),
                        Long.parseLong(values[1]));
            } else if (path.getJavaType() == double.class || path.getJavaType() == Double.class) {
                return criteriaBuilder.between(path, Double.parseDouble(values[0]),
                        Double.parseDouble(values[1]));
            } else if (path.getJavaType() == BigDecimal.class) {
                return criteriaBuilder.between(path, new BigDecimal(values[0]),
                        new BigDecimal(values[1]));
            } else {
                return criteriaBuilder.between(path, values[0], values[1]);
            }
        } else if (criteria.getOperator().equalsIgnoreCase(Operator.IN.getValue())) {
            String[] values = criteria.getValue().toString().split(";");
            if (path.getJavaType() == int.class || path.getJavaType() == Integer.class) {
                CriteriaBuilder.In<Integer> inClause = criteriaBuilder.in(path);
                for (String value : values) {
                    inClause.value(Integer.parseInt(value));
                }
                return inClause;
            } else if (path.getJavaType() == long.class || path.getJavaType() == Long.class) {
                CriteriaBuilder.In<Long> inClause = criteriaBuilder.in(path);
                for (String value : values) {
                    inClause.value(Long.parseLong(value));
                }
                return inClause;
            } else if (path.getJavaType() == double.class || path.getJavaType() == Double.class) {
                CriteriaBuilder.In<Double> inClause = criteriaBuilder.in(path);
                for (String value : values) {
                    inClause.value(Double.parseDouble(value));
                }
                return inClause;
            } else if (path.getJavaType() == BigDecimal.class) {
                CriteriaBuilder.In<BigDecimal> inClause = criteriaBuilder.in(path);
                for (String value : values) {
                    inClause.value(new BigDecimal(value));
                }
                return inClause;
            } else if (path.getJavaType() == String.class) {
                CriteriaBuilder.In<String> inClause = criteriaBuilder.in(path);
                for (String value : values) {
                    inClause.value(value);
                }
                return inClause;
            }
        } else if (criteria.getOperator().equalsIgnoreCase(Operator.SIZE.getValue())) {
            String[] values = criteria.getValue().toString().split(";");
            Expression expression = criteriaBuilder.size(path);
            return build(values[0], expression, values[1], criteriaBuilder);
        } else if (criteria.getOperator().equalsIgnoreCase(Operator.IS_EMPTY.getValue())) {
            return criteriaBuilder.isEmpty(path);
        } else if (criteria.getOperator().equalsIgnoreCase(Operator.IS_NOT_EMPTY.getValue())) {
            return criteriaBuilder.isNotEmpty(path);
        }
        return null;
    }

    @SuppressWarnings("unchecked")
    private Predicate build(String operator, Expression expression, String value, CriteriaBuilder criteriaBuilder) {
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
