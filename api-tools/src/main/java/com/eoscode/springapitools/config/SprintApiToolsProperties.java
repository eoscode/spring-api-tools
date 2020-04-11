package com.eoscode.springapitools.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties("spring-api-tools")
public class SprintApiToolsProperties {

    private boolean enableDefaultPageable = false;

    public SprintApiToolsProperties() {}

    public boolean isEnableDefaultPageable() {
        return enableDefaultPageable;
    }

    public void setEnableDefaultPageable(boolean enableDefaultPageable) {
        this.enableDefaultPageable = enableDefaultPageable;
    }

}
