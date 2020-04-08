package com.eoscode.springapitools.data.filter;

import java.util.List;

public class QueryDefinition {

    private Boolean distinct;
    private List<FilterDefinition> filters;
    private List<SortDefinition> sorts;
    private String operator;

    public QueryDefinition() {}

    public boolean isDistinct() {
        if (distinct == null) {
            return true;
        }
        return distinct;
    }

    public void setDistinct(boolean distinct) {
        this.distinct = distinct;
    }

    public List<FilterDefinition> getFilters() {
        return filters;
    }

    public void setFilters(List<FilterDefinition> filters) {
        this.filters = filters;
    }

    public List<SortDefinition> getSorts() {
        return sorts;
    }

    public void setSorts(List<SortDefinition> sorts) {
        this.sorts = sorts;
    }

    public String getOperator() {
        return operator;
    }

    public void setOperator(String operator) {
        this.operator = operator;
    }

}
