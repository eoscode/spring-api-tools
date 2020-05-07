package com.eoscode.springapitools.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties("spring-api-tools")
public class SpringApiToolsProperties {

    private boolean enableDefaultPageable = false;
    private int listDefaultSize = 0;
    private boolean listDefaultSizeOverride = true;
    private StringCaseSensitive stringCaseSensitive = StringCaseSensitive.none;

    public SpringApiToolsProperties() {}

    public boolean isEnableDefaultPageable() {
        return enableDefaultPageable;
    }

    public void setEnableDefaultPageable(boolean enableDefaultPageable) {
        this.enableDefaultPageable = enableDefaultPageable;
    }

    public int getListDefaultSize() {
        return listDefaultSize;
    }

    public void setListDefaultSize(int listDefaultSize) {
        this.listDefaultSize = listDefaultSize;
    }

    public boolean isListDefaultSizeOverride() {
        return listDefaultSizeOverride;
    }

    public void setListDefaultSizeOverride(boolean listDefaultSizeOverride) {
        this.listDefaultSizeOverride = listDefaultSizeOverride;
    }

    public StringCaseSensitive getStringCaseSensitive() {
        return stringCaseSensitive;
    }

    public void setStringCaseSensitive(StringCaseSensitive stringCaseSensitive) {
        this.stringCaseSensitive = stringCaseSensitive;
    }
}
