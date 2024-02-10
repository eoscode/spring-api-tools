package com.eoscode.springapitools.data.filter;

import org.springframework.data.jpa.domain.Specification;

public class SpecificationValue extends SpecificationFilter<Specification<?>> {

    public SpecificationValue(Specification<?> specification) {
        super(specification);
    }

    @Override
    public Specification<?> getFilter() {
        return super.getFilter();
    }
}
