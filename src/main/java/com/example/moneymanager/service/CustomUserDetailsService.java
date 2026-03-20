package com.example.moneymanager.service;

import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collections;

@Service
public class CustomUserDetailsService implements UserDetailsService {
    private final UserService userService;

    public CustomUserDetailsService(UserService userService) {
        this.userService = userService;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        com.example.moneymanager.entity.User myUser = this.userService.findUserByEmail(username);

        if (myUser == null) {
            throw new UsernameNotFoundException("user not found");
        }

        return new User(myUser.getEmail(), myUser.getPassword(),
                Collections.emptyList());

    }

}
