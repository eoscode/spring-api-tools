package com.eoscode.springapitools.security;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.io.Serializable;
import java.util.Collection;
import java.util.Set;

public class Auth<T extends Serializable> implements UserDetails {
    private static final long serialVersionUID = 1L;

    private T id;
    private String username;
    private String password;
    private Collection<? extends GrantedAuthority> authorities;

    public Auth(T id, String username, String password, Set<? extends GrantedAuthority> authorities) {
        super();
        this.id = id;
        this.username = username;
        this.password = password;
        this.authorities = authorities;
                //perfis.stream().map(x -> new SimpleGrantedAuthority(x.getDescricao())).collect(Collectors.toList());
    }

    public T getId() {
        return id;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        return username;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }

    public <Grant extends GrantedAuthority> boolean hasRole(Grant grant) {
        return getAuthorities().contains(grant);
    }

}
