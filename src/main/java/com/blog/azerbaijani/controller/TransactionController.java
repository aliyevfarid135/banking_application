package com.blog.azerbaijani.controller;

import com.blog.azerbaijani.entity.*;
import com.blog.azerbaijani.repository.AccountRepository;
import com.blog.azerbaijani.repository.TransactionRepository;
import com.blog.azerbaijani.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

@Controller
@RequestMapping("/transaction")
public class TransactionController {

    @Autowired
    private TransactionRepository transactionRepository;
    
    @Autowired
    private AccountRepository accountRepository;
    
    @Autowired
    private UserRepository userRepository;

    @GetMapping("/terminal")
    public String terminalPage() {
        return "terminal";
    }

    @PostMapping("/terminal")
    public String increaseBalance(@RequestParam(name = "cardNumber") String cardNumber,
                                 @RequestParam(name = "money") String money) {
        long n = 0;
        for (Account a : accountRepository.findAll()) {
            if (a.getAccountNumber().equals(cardNumber)) {
                Double balance = a.getBalance();
                balance += Double.valueOf(money);
                a.setBalance(balance);
                n = a.getId();
                accountRepository.save(a);
            }
        }
        createTransaction(n, Double.valueOf(money), userRepository.getById(2L));
        return "redirect:terminal?success";
    }

    @GetMapping("/transfer")
    public String getKocurme() {
        return "transfer";
    }

    @PostMapping("/transfer")
    public String doKocurme(@RequestParam(name = "cardNumber") String cardNumber,
                           @RequestParam(name = "money") String money) {
        String authentication = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByUsername(authentication);
        long n = 0;
        for (Account a : accountRepository.findAll()) {
            if (a.getAccountNumber().equals(cardNumber)) {
                Double balance = a.getBalance();
                balance += Double.valueOf(money);
                a.setBalance(balance);
                n = a.getId();
                Double sender_balance = user.getDefaultAccount().getBalance();
                sender_balance -= Double.valueOf(money);
                user.getDefaultAccount().setBalance(sender_balance);
                accountRepository.save(a);
                accountRepository.save(user.getDefaultAccount());
            }
        }
        createTransaction(n, Double.valueOf(money), user);
        return "redirect:transfer?success";
    }

    private void createTransaction(long receiverAccountId, double amount, User sender) {
        Transaction transaction = new Transaction();
        transaction.setReceiver(accountRepository.getById(receiverAccountId).getUser());
        transaction.setAmount(amount);
        SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy");
        Date date;
        try {
            date = dateFormat.parse("01/01/2024");
        } catch (ParseException e) {
            e.printStackTrace();
            date = new Date();
        }
        transaction.setTransactionDate(date);
        transaction.setSender(sender);
        User receiver = transaction.getReceiver();
        List<Transaction> receivedList = receiver.getReceivedTransactions();
        receivedList.add(transaction);
        receiver.setReceivedTransactions(receivedList);
        
        List<Transaction> sentList = sender.getSentTransactions();
        sentList.add(transaction);
        sender.setSentTransactions(sentList);
        
        userRepository.save(receiver);
        userRepository.save(sender);
        transactionRepository.save(transaction);
    }
}
