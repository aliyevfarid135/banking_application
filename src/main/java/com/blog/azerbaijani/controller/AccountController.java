package com.blog.azerbaijani.controller;

import com.blog.azerbaijani.entity.Account;
import com.blog.azerbaijani.entity.User;
import com.blog.azerbaijani.repository.AccountRepository;
import com.blog.azerbaijani.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Objects;
import java.util.Random;

@Controller
@RequestMapping("/account")
public class AccountController {

    @Autowired
    private AccountRepository accountRepository;
    
    @Autowired
    private UserRepository userRepository;

    private final Random random = new Random();

    private User getAuthenticatedUser() {
        String authentication = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByUsername(authentication);
    }

    @GetMapping("/create")
    public String createAccount() {
        return "createAccount";
    }

    @PostMapping("/create")
    public String createAccount(Account account) {
        User user = getAuthenticatedUser();
        account.setUser(user);

        account.setAccountNumber(generateRandomNumber(16));
        account.setCvvNumber(generateRandomNumber(3));
        account.setDate(generateRandomDate());
        account.setBalance(0.0); 

        accountRepository.save(account);

        user.setDefaultAccount(account);
        userRepository.save(user);

        return "redirect:create?success";
    }

    @GetMapping("/default")
    public String getDefaultCardPage(Model model) {
        User user = getAuthenticatedUser();
        model.addAttribute("user", user);
        model.addAttribute("account", user.getDefaultAccount());
        return "card";
    }

    @PostMapping("/default")
    public String chooseDefaultCard(@RequestParam Long id) {
        User user = getAuthenticatedUser();
        user.getAccountList().stream()
            .filter(account -> Objects.equals(account.getId(), id))
            .findFirst()
            .ifPresent(user::setDefaultAccount);

        userRepository.save(user);
        return "redirect:default?success";
    }

    @GetMapping("/info")
    public String getCardInfo(Model model) {
        User user = getAuthenticatedUser();
        model.addAttribute("account", user.getDefaultAccount());
        return "card";
    }

    private String generateRandomNumber(int length) {
        return String.format("%0" + length + "d", random.nextInt((int) Math.pow(10, length)));
    }

    private String generateRandomDate() {
        LocalDate date = LocalDate.of(random.nextInt(10) + 2023, random.nextInt(12) + 1, random.nextInt(28) + 1);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM/yyyy");
        return date.format(formatter);
    }
}
