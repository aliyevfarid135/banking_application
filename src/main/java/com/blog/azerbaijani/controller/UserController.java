package com.blog.azerbaijani.controller;


import com.blog.azerbaijani.entity.*;
import com.blog.azerbaijani.repository.AccountRepository;
import com.blog.azerbaijani.repository.TransactionRepository;
import com.blog.azerbaijani.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.Random;

@Controller
public class UserController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TransactionRepository transactionRepository;

    @Autowired
    private AccountRepository accountRepository;

    @GetMapping("/terminal")
    public String terminalPage(){
        return "terminal";
    }

    @RequestMapping(value = "/terminal", method = RequestMethod.POST)
    public String increaseBalance(@RequestParam(name = "cardNumber") String cardNumber,
                                  @RequestParam(name = "money") String money){
        long n = 0;
        for (Account a : accountRepository.findAll()) {
            if (a.getAccountNumber().equals(cardNumber)) {
                Double balance = a.getBalance();
                balance+=Double.valueOf(money);
                a.setBalance(balance);
                n = a.getId();
                accountRepository.save(a);//database
            }
        }
        Transaction transaction = new Transaction();
        transaction.setReceiver(accountRepository.getById(n).getUser());
        transaction.setAmount(Double.valueOf(money));
        SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy");
        Date date;
        try {
            date = dateFormat.parse("01/01/2024");
        } catch (ParseException e) {
            e.printStackTrace();
            date = new Date();
        }
        transaction.setTransactionDate(date);
        transaction.setSender(userRepository.getById(2L));
        User user = userRepository.getById(transaction.getReceiver().getId());
        List<Transaction> list = user.getReceivedTransactions();
        list.add(transaction);
        user.setReceivedTransactions(list);
        userRepository.save(user);//database
        transactionRepository.save(transaction);//database
        return "redirect:terminal?success";
    }


    @GetMapping("/cardInfo")
    public String getCardInfo(Model model) {
        String authentication = SecurityContextHolder.getContext().getAuthentication().getName();
        User user =  userRepository.findByUsername(authentication);
        model.addAttribute(user.getDefaultAccount());
        return "card";
    }

    @GetMapping("/transfer")
    public String getKocurme(){
        return "transfer";
    }

    @PostMapping("/transfer")
    public String doKocurme(@RequestParam(name = "cardNumber") String cardNumber,
                                  @RequestParam(name = "money") String money){
        String authentication = SecurityContextHolder.getContext().getAuthentication().getName();
        User user =  userRepository.findByUsername(authentication);
        long n = 0;
        for (Account a : accountRepository.findAll()) {
            if (a.getAccountNumber().equals(cardNumber)) {
                Double balance = a.getBalance();
                balance+=Double.valueOf(money);
                a.setBalance(balance);
                n = a.getId();
                Double sender_balance = user.getDefaultAccount().getBalance();
                sender_balance-=Double.valueOf(money);
                user.getDefaultAccount().setBalance(sender_balance);
                accountRepository.save(a);//database
                accountRepository.save(user.getDefaultAccount());//database
            }
        }
        Transaction transaction = new Transaction();
        transaction.setReceiver(accountRepository.getById(n).getUser());
        transaction.setAmount(Double.valueOf(money));
        SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy");
        Date date;
        try {
            date = dateFormat.parse("01/01/2024");
        } catch (ParseException e) {
            e.printStackTrace();
            date = new Date();
        }
        transaction.setTransactionDate(date);
        transaction.setSender(user);
        List<Transaction> list = user.getSentTransactions();
        list.add(transaction);
        user.setSentTransactions(list);
        userRepository.save(user);//database
        transactionRepository.save(transaction);//database
        return "redirect:transfer?success";
    }

    @GetMapping("/payment")
    public String getPayment(){
        return "payment";
    }

    @PostMapping("/payment")
    public String doPayment(@RequestParam(name = "type_id") Long type_id,
                            @RequestParam(name = "money") Long money) {
        String authentication = SecurityContextHolder.getContext().getAuthentication().getName();
        User user =  userRepository.findByUsername(authentication);
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
                accountRepository.save(userAccount );//database
            }
        }
        return "redirect:payment?success";
    }

    @GetMapping("/register")
    public String showRegistrationForm(Model model) {
        model.addAttribute("user", new User());
        return "register";
    }


    @PostMapping("/register")
    public String doRegister(@ModelAttribute User user, Model model){
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
        userRepository.save(user);//database
        return "redirect:register?success";
    }

    @GetMapping("/createAccount")
    public String createAccount() {
        return "createAccount";
    }

    @PostMapping("/createAccount")
    public String createAccount(Account account) {
        String authentication = SecurityContextHolder.getContext().getAuthentication().getName();
        User user =  userRepository.findByUsername(authentication);
        account.setUser(user);

        String accountNumber = generateRandomNumber(16);
        String cvvNumber = generateRandomNumber(3);
        String date = generateRandomDate();
        Double balance = 0.0;
        account.setBalance(balance);
        account.setAccountNumber(accountNumber);
        account.setCvvNumber(cvvNumber);
        account.setDate(date);
        accountRepository.save(account);//database
        user.setDefaultAccount(account);
        userRepository.save(user);//database

        return "redirect:createAccount?success";
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

    @GetMapping("/login")
    public String loginPage(Model model){

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || authentication instanceof AnonymousAuthenticationToken) {
            model.addAttribute("alreadyLoggedIn", false);
        } else {
            model.addAttribute("alreadyLoggedIn", true);
        }
        return "login";
    }

    @GetMapping("/default")
    public String getDefaultCardPage(Model model){
        String authentication = SecurityContextHolder.getContext().getAuthentication().getName();
        User user =  userRepository.findByUsername(authentication);
        model.addAttribute("user", user);
        model.addAttribute("account", user.getDefaultAccount());
        return "card";
    }

    @PostMapping("/default")
    public String chooseDefaultCard(@RequestParam Long id){
        String authentication = SecurityContextHolder.getContext().getAuthentication().getName();
        User user =  userRepository.findByUsername(authentication);
        for (Account a : user.getAccountList()) {
            if (Objects.equals(a.getId(), id)){
                user.setDefaultAccount(a);
            }
        }
        userRepository.save(user);
        return "redirect:default?success";
    }

    @GetMapping("/logout-success")
    public String logout(){
        return "logout-success";
    }

    @GetMapping("/home")
    public String getHome(){
        return "home";
    }

    @GetMapping("/checkLoginStatus")
    @ResponseBody
    public boolean checkLoginStatus(Authentication authentication) {
        return authentication != null && authentication.isAuthenticated();
    }

}

