package com.eoscode.springapitools.data.domain;

import com.eoscode.springapitools.data.domain.filter.FilterCriteria;

import java.util.List;

public class QueryDefinition {

    private Boolean distinct;
    private List<FilterCriteria> filters;

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

    public List<FilterCriteria> getFilters() {
        return filters;
    }

    public void setFilters(List<FilterCriteria> filters) {
        this.filters = filters;
    }

}
