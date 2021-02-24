package com.eoscode.springapitools.data.filter;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public interface ViewDefinition {

    Set<String> getViews();
    Set<String> getFetches();

    static ViewDefinition create(Set<String> views) {
        List<JoinDefinition> joins = null;
        if (views != null && !views.isEmpty()) {
            joins = views.stream()
                    .filter(view -> view.contains("."))
                    .map(field -> {
                        int idx = field.indexOf(".");
                        return new JoinDefinition(field.substring(0, idx), true);
                    })
                    .collect(Collectors.toList());
        }
        return new QueryDefinition(views, joins);
    }

    default boolean isViews() {
        return getViews() != null && !getViews().isEmpty();
    }

}
