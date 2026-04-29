package com.grievancehub.controller;

import com.grievancehub.entity.User;
import com.grievancehub.entity.Officer;
import com.grievancehub.service.UserService;
import com.grievancehub.service.OfficerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

@Controller
public class AuthController {

    @Autowired
    private UserService userService;

    @Autowired
    private OfficerService officerService;

    @GetMapping("/register")
    public String registerForm() {
        return "register";
    }

    @PostMapping("/register")
    public String register(@RequestParam String name,
                           @RequestParam String email,
                           @RequestParam String password,
                           @RequestParam(required = false) String mobileNumber,
                           Model model) {
        if (userService.emailExists(email) || officerService.emailExists(email)) {
            model.addAttribute("error", "Email already exists!");
            return "register";
        }
        userService.registerUser(name, email, password, mobileNumber);
        return "redirect:/login?registered";
    }

    @GetMapping("/login")
    public String loginPage() {
        return "login";
    }

    @GetMapping("/profile")
    public String profile(Principal principal, Model model) {
        if (principal != null) {
            String email = principal.getName();
            if (principal instanceof OAuth2AuthenticationToken oauth) {
                email = oauth.getPrincipal().getAttributes().get("email").toString();
            }
            
            User user = userService.findByEmail(email);
            if (user != null) {
                model.addAttribute("userProfile", user);
                model.addAttribute("userType", "CITIZEN");
            } else {
                Officer officer = officerService.findByEmail(email);
                if (officer != null) {
                    model.addAttribute("userProfile", officer);
                    model.addAttribute("userType", "OFFICER");
                }
            }
        }
        return "profile";
    }
}
