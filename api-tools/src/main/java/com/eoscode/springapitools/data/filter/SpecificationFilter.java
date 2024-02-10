package com.eoscode.springapitools.data.filter;

abstract class SpecificationFilter<T> {
    private final T filter;

    public SpecificationFilter(T filter) {
        this.filter = filter;
    }

    public T getFilter() {
        return filter;
    }
}
