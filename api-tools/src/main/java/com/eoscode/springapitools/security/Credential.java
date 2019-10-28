package com.eoscode.springapitools.security;

import java.io.Serializable;

public class Credential implements Serializable {

    private String identifier;
    private String password;

    public Credential() {}

    public String getIdentifier() {
        return identifier;
    }

    public void setIdentifier(String identifier) {
        this.identifier = identifier;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

}
