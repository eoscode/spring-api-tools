package com.eoscode.springapitools.data.filter;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class QueryDefinition implements ViewDefinition {

    private Boolean distinct;
    private Set<String> views = new HashSet<>();
    private List<FilterDefinition> filters = new ArrayList<>();
    private List<SortDefinition> sorts = new ArrayList<>();
    private List<JoinDefinition> joins = new ArrayList<>();
    private String operator;

    public QueryDefinition() {}

    public QueryDefinition(Set<String> views) {
        this.views = views;
    }

    public QueryDefinition(Set<String> views, List<JoinDefinition> joins) {
        this.views = views;
        this.joins = joins;
    }

    public boolean isDistinct() {
        if (distinct == null) {
            return true;
        }
        return distinct;
    }

    public void setDistinct(boolean distinct) {
        this.distinct = distinct;
    }

    public Set<String> getViews() {
        return views;
    }

    public void setViews(Set<String> views) {
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

    @Override
    public Set<String> getFetches() {
        Set<String> fetches = new HashSet<>();
        if (getFilters() != null && !getFilters().isEmpty()) {
            getFilters().forEach(filterDefinition -> {
                if (filterDefinition.isJoin() && filterDefinition.isFetch()) {
                    fetches.add(filterDefinition.getPathJoin());
                }
            });
        }
        if (getJoins() != null && !getJoins().isEmpty()) {
            getJoins().forEach(joinDefinition -> {
                if (joinDefinition.isFetch()) {
                    fetches.add(joinDefinition.getField());
                }
            });
        }
        return fetches;
    }
}
