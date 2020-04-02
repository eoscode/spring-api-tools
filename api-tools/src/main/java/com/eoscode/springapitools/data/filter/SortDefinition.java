package com.eoscode.springapitools.data.filter;

public class SortDefinition {

    private String field;
    private Direction direction;

    public SortDefinition() {}

    public SortDefinition(String field, Direction direction) {
        this.field = field;
        this.direction = direction;
    }

    public String getField() {
        return field;
    }

    public void setField(String field) {
        this.field = field;
    }

    public Direction getDirection() {
        return direction;
    }

    public void setDirection(Direction direction) {
        this.direction = direction;
    }

    public enum Direction {
        ASC,
        DESC
    }

}
