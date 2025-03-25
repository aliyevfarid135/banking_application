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
    public String doPayment(@RequestParam(name = "type_id") Long typeId,
                           @RequestParam(name = "money") Long money) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByUsername(username);

        if (user.getDefaultAccount() == null) {
            return "error_no_default_account";
        }

        Account userAccount = user.getDefaultAccount();

        Account targetAccount = accountRepository.findById(typeId).orElse(null);

        if (targetAccount == null || !targetAccount.getUser().getIsPayment()) {
            return "error_invalid_account";
        }

        if (userAccount.getBalance() < money) {
            return "error_insufficient_balance";
        }

        targetAccount.setBalance(targetAccount.getBalance() + money);
        userAccount.setBalance(userAccount.getBalance() - money);

        accountRepository.save(targetAccount);
        accountRepository.save(userAccount);

        return "redirect:payment?success";
    }
}
