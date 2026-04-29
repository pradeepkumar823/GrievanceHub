package com.grievancehub.service;

import com.grievancehub.entity.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

@Service
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    @Autowired
    private UserService userService;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        // Load the user details from Google
        OAuth2User oAuth2User = super.loadUser(userRequest);
        
        String email = oAuth2User.getAttribute("email");
        String name = oAuth2User.getAttribute("name");
        
        // Link with existing DB user
        User user = userService.findByEmail(email);
        if (user == null) {
            // Requirement: Prevent Google login if the user has not registered yet
            throw new OAuth2AuthenticationException("unregistered_account");
        }
        
        // Block disabled accounts from logging in via Google
        if (Boolean.FALSE.equals(user.getEnabled())) {
            throw new OAuth2AuthenticationException("account_disabled");
        }
        // Sync display name if it changed in Google
        if (name != null && !name.equals(user.getName())) {
            user.setName(name);
            userService.save(user);
        }
        
        // Ensure Spring Security assigns them their exact DB role (USER or ADMIN)
        List<SimpleGrantedAuthority> authorities = Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + user.getRole()));
        
        // Return a fully synchronized Spring Security Object
        return new DefaultOAuth2User(
                authorities,
                oAuth2User.getAttributes(),
                "email" // Defines the 'name' attribute which getName() will return
        );
    }
}
