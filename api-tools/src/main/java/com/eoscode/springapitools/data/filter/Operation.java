package com.eoscode.springapitools.data.filter;

public enum Operation {
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

    Operation(String value) {
        this.value = value;
    }

    private final String value;
    String getValue() {
        return value;
    }

}
