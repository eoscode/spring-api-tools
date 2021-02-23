package com.eoscode.springapitools.data.filter;

import java.util.ArrayList;
import java.util.List;

public class QueryDefinition {

    private Boolean distinct;
    private List<String> views = new ArrayList<>();
    private List<FilterDefinition> filters = new ArrayList<>();
    private List<SortDefinition> sorts = new ArrayList<>();
    private List<JoinDefinition> joins = new ArrayList<>();
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

    public List<String> getViews() {
        return views;
    }

    public void setViews(List<String> views) {
        this.views = views;
    }

    public List<FilterDefinition> getFilters() {
        return filters;
    }

    public void setFilters(List<FilterDefinition> filters) {
        this.filters = filters;
    }

    public List<JoinDefinition> getJoins() {
        return joins;
    }

    public void setJoins(List<JoinDefinition> joins) {
        this.joins = joins;
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
