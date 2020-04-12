package com.eoscode.springapitools.data.filter;

public class RequestParameter {

    private boolean distinct = true;
    private String operator = "and";
    private Boolean pageable = null;
    private String[] filters = null;

    public RequestParameter() {}

    public boolean isDistinct() {
        return distinct;
    }

    public void setDistinct(boolean distinct) {
        this.distinct = distinct;
    }

    public String getOperator() {
        return operator;
    }

    public void setOperator(String operator) {
        this.operator = operator;
    }

    public Boolean getPageable() {
        return pageable;
    }

    public void setPageable(Boolean pageable) {
        this.pageable = pageable;
    }

    public String[] getFilters() {
        return filters;
    }

    public void setFilters(String[] filters) {
        this.filters = filters;
    }
}
