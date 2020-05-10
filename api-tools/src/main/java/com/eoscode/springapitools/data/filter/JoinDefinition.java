package com.eoscode.springapitools.data.filter;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Objects;

public class JoinDefinition {

    private String field;
    private JoinType type = JoinType.LEFT;
    private boolean fetch = false;

    public JoinDefinition() {}

    public JoinDefinition(String field, boolean fetch) {
        this.field = field;
        this.fetch = fetch;
    }

    public JoinDefinition(String field, JoinType type, boolean fetch) {
        this.field = field;
        this.type = type;
        this.fetch = fetch;
    }

    public String getField() {
        return field;
    }

    public void setField(String field) {
        this.field = field;
    }

    public JoinType getType() {
        return type;
    }

    public void setType(JoinType type) {
        this.type = type;
    }

    public boolean isFetch() {
        return fetch;
    }

    public void setFetch(boolean fetch) {
        this.fetch = fetch;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof JoinDefinition)) return false;
        JoinDefinition that = (JoinDefinition) o;
        return getField().equals(that.getField());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getField());
    }

    public enum JoinType {
        @JsonProperty("left")
        LEFT,
        @JsonProperty("inner")
        INNER
    }

}
