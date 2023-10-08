package com.eoscode.springapitools.service;

public interface MessageResolver {
    String getMessage(String messageId);

    String getMessage(String messageId, Object[] args);
}
