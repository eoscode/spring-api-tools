package com.eoscode.springapitools.data.filter;


import com.fasterxml.jackson.annotation.JsonSetter;

public class FilterDefinition {
    private String field;
    private String operator;
    private Object value;
    private boolean fetch;

    private boolean join;
    private String pathJoin = "";

    private int order;

    public FilterDefinition() {}

    public FilterDefinition(String field, String operation, Object value) {
        this.field = field;
        this.operator = operation;
        this.value = value;
        defineJoin(field);
    }

    public FilterDefinition(String field, String operation, Object value, boolean fetch) {
        this(field, operation, value);
        this.fetch = fetch;
    }

    private void defineJoin(String field) {
        int idx = field.indexOf(".");
        if (idx >= 0) {
            join = true;
            pathJoin = field.substring(0, idx);
        }
    }

    public String getField() {
        return field;
    }

    @JsonSetter
    public void setField(String field) {
        this.field = field;
        defineJoin(field);
    }

    public String getOperator() {
        return operator;
    }

    public void setOperator(String operator) {
        this.operator = operator;
    }

    public Object getValue() {
        return value;
    }

    public void setValue(Object value) {
        this.value = value;
    }

    public boolean isFetch() {
        return fetch;
    }

    public void setFetch(boolean fetch) {
        this.fetch = fetch;
    }

    public boolean isJoin() {
        return join;
    }

    public String getPathJoin() {
        return pathJoin;
    }

    public int getOrder() {
        return order;
    }

    protected void setOrder(int order) {
        this.order = order;
    }

}
