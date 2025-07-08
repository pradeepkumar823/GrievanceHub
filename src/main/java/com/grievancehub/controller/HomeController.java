package com.grievancehub.controller;

import com.grievancehub.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.security.Principal;

@Controller
public class HomeController {

    @Autowired
    private UserService userService;

    @GetMapping("/home")
    public String showHome(Model model, Principal principal) {
        String email = principal.getName(); // Logged-in user's email
        String name = userService.findByEmail(email).getName(); // Fetch full name
        model.addAttribute("username", name); // Send name to UI
        return "home";
    }
}