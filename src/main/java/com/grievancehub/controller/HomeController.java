package com.grievancehub.controller;

import com.grievancehub.entity.User;
import com.grievancehub.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.security.Principal;

@Controller
public class HomeController {

    @Autowired
    private UserService userService;

    @GetMapping("/")
    public String index(Principal principal, Model model) {
        loadUser(principal, model);
        return "home";
    }

    @GetMapping("/home")
    public String home(Principal principal, Model model) {
        loadUser(principal, model);
        return "home";
    }

    private void loadUser(Principal principal, Model model) {
        if (principal != null) {
            String email = principal.getName();
            if (principal instanceof OAuth2AuthenticationToken oauth) {
                email = oauth.getPrincipal().getAttributes().get("email").toString();
            }
            User user = userService.findByEmail(email);
            if (user != null) {
                model.addAttribute("loggedUser", user);
            }
        }
    }
}
