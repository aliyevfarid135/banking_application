package com.blog.azerbaijani.controller;

import com.blog.azerbaijani.entity.Account;
import com.blog.azerbaijani.entity.User;
import com.blog.azerbaijani.repository.AccountRepository;
import com.blog.azerbaijani.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/payment")
public class PaymentController {

    @Autowired
    private AccountRepository accountRepository;
    
    @Autowired
    private UserRepository userRepository;

    @GetMapping
    public String getPayment() {
        return "payment";
    }

    @PostMapping
    public String doPayment(@RequestParam(name = "type_id") Long type_id,
                           @RequestParam(name = "money") Long money) {
        String authentication = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByUsername(authentication);
        if (user.getDefaultAccount() == null) {
            return "error_no_default_account";
        }

        Account userAccount = user.getDefaultAccount();
        List<Account> list = accountRepository.findAll();
        for (Account a : list) {
            if (a.getUser().getIsPayment() && a.getId().equals(type_id)) {
                Double balance = a.getBalance();
                balance += money;
                a.setBalance(balance);

                Double userBalance = userAccount.getBalance();
                userBalance -= money;
                userAccount.setBalance(userBalance);
                accountRepository.save(userAccount);
            }
        }
        return "redirect:payment?success";
    }
}
