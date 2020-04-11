package com.eoscode.springapitools.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.ComponentScan;

@ComponentScan(basePackages = {
        "com.eoscode.springapitools"
})
@EnableConfigurationProperties
public class SpringApiToolsScan {
}
