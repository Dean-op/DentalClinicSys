package com.dentalclinic.security;

import com.dentalclinic.domain.AccountStatus;
import com.dentalclinic.domain.Role;
import com.dentalclinic.entity.UserAccount;
import java.util.Collection;
import java.util.List;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

public class UserPrincipal implements UserDetails {
    public final Long id;
    private final String username;
    private final String password;
    public final Role role;
    private final AccountStatus status;

    public UserPrincipal(UserAccount account) {
        this.id = account.id;
        this.username = account.username;
        this.password = account.passwordHash;
        this.role = account.role;
        this.status = account.status;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_" + role.name()));
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
        return status == AccountStatus.ENABLED;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return status == AccountStatus.ENABLED;
    }
}
