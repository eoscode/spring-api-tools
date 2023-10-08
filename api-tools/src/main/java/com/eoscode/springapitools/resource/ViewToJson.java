package com.eoscode.springapitools.resource;

import com.eoscode.springapitools.data.filter.ViewDefinition;

public interface ViewToJson {

    <T> String toJson(ViewDefinition viewDefinition, T result);

}
