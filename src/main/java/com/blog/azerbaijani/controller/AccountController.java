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

import java.util.Random;

@Controller
@RequestMapping("/account")
public class AccountController {

    @Autowired
    private AccountRepository accountRepository;
    
    @Autowired
    private UserRepository userRepository;

    @GetMapping("/create")
    public String createAccount() {
        return "createAccount";
    }

    @PostMapping("/create")
    public String createAccount(Account account) {
        String authentication = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByUsername(authentication);
        account.setUser(user);

        String accountNumber = generateRandomNumber(16);
        String cvvNumber = generateRandomNumber(3);
        String date = generateRandomDate();
        Double balance = 0.0;
        account.setBalance(balance);
        account.setAccountNumber(accountNumber);
        account.setCvvNumber(cvvNumber);
        account.setDate(date);
        accountRepository.save(account);
        user.setDefaultAccount(account);
        userRepository.save(user);

        return "redirect:create?success";
    }

    @GetMapping("/default")
    public String getDefaultCardPage(Model model) {
        String authentication = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByUsername(authentication);
        model.addAttribute("user", user);
        model.addAttribute("account", user.getDefaultAccount());
        return "card";
    }

    @PostMapping("/default")
    public String chooseDefaultCard(@RequestParam Long id) {
        String authentication = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByUsername(authentication);
        for (Account a : user.getAccountList()) {
            if (Objects.equals(a.getId(), id)) {
                user.setDefaultAccount(a);
            }
        }
        userRepository.save(user);
        return "redirect:default?success";
    }

    @GetMapping("/info")
    public String getCardInfo(Model model) {
        String authentication = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByUsername(authentication);
        model.addAttribute(user.getDefaultAccount());
        return "card";
    }

    private String generateRandomNumber(int length) {
        Random random = new Random();
        StringBuilder result = new StringBuilder();

        for (int i = 0; i < length; i++) {
            result.append(random.nextInt(10));
        }

        return result.toString();
    }

    private String generateRandomDate() {
        Random random = new Random();
        int year = random.nextInt(10) + 2023;
        int month = random.nextInt(12) + 1;
        int day = random.nextInt(28) + 1;

        SimpleDateFormat sdf = new SimpleDateFormat("MM/yyyy");
        Date date = new Date(year - 1900, month - 1, day);

        return sdf.format(date);
    }
}
