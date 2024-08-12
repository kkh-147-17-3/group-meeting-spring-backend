package com.sideprj.groupmeeting.dto;

import com.sideprj.groupmeeting.entity.User;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Collections;

public class DefaultUserDetails implements UserDetails {

    @Getter
    private final Long id;
    private final String userNickname;
    private final boolean active;
    private final Collection<? extends GrantedAuthority> authorities;


    public DefaultUserDetails(User user){
        this.id = user.getId();
        this.userNickname = user.getNickname();
        this.active = user.isActive();
        this.authorities = Collections.singletonList(new SimpleGrantedAuthority("USER"));
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public String getPassword() {
        return null;
    }

    @Override
    public boolean isEnabled(){
        return active;
    }

    @Override
    public String getUsername() {
        return userNickname;
    }

}
