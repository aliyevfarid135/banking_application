package com.blog.azerbaijani.controller;

import com.blog.azerbaijani.entity.User;
import com.blog.azerbaijani.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/auth")
public class AuthController {

    @Autowired
    private UserRepository userRepository;

    @GetMapping("/login")
    public String loginPage(Model model) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || authentication instanceof AnonymousAuthenticationToken) {
            model.addAttribute("alreadyLoggedIn", false);
        } else {
            model.addAttribute("alreadyLoggedIn", true);
        }
        return "login";
    }

    @GetMapping("/register")
    public String showRegistrationForm(Model model) {
        model.addAttribute("user", new User());
        return "register";
    }

    @PostMapping("/register")
    public String doRegister(@ModelAttribute User user, Model model) {
        for (User u : userRepository.findAll()) {
            if (u.getUsername().equals(user.getUsername())) {
                model.addAttribute("usernameError", "Username already exists");
                return "register";
            } else if (u.getEmail().equals(user.getEmail())) {
                model.addAttribute("emailError", "Email already exists");
                return "register";
            }
        }
        user.setIsPayment(false);
        userRepository.save(user);
        return "redirect:register?success";
    }

    @GetMapping("/logout-success")
    public String logout() {
        return "logout-success";
    }

    @GetMapping("/checkLoginStatus")
    @ResponseBody
    public boolean checkLoginStatus(Authentication authentication) {
        return authentication != null && authentication.isAuthenticated();
    }
}
