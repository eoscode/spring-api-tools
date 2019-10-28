package com.eoscode.springapitools.data.domain;

import java.io.Serializable;

public interface Identifier<ID> extends Serializable {
    ID getId();
    void setId(ID id);
}
