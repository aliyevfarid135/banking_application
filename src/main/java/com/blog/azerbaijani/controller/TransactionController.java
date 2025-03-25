package com.blog.azerbaijani.controller;

import com.blog.azerbaijani.entity.*;
import com.blog.azerbaijani.repository.AccountRepository;
import com.blog.azerbaijani.repository.TransactionRepository;
import com.blog.azerbaijani.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.Optional;

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
        Optional<Account> optionalAccount = accountRepository.findByAccountNumber(cardNumber);
        if (optionalAccount.isPresent()) {
            Account account = optionalAccount.get();
            double amount = Double.parseDouble(money);
            account.setBalance(account.getBalance() + amount);
            accountRepository.save(account);
            createTransaction(account.getId(), amount, userRepository.getById(2L));
            return "redirect:terminal?success";
        } else {
            return "error_account_not_found";
        }
    }

    @GetMapping("/transfer")
    public String getTransferPage() {
        return "transfer";
    }

    @PostMapping("/transfer")
    public String doTransfer(@RequestParam(name = "cardNumber") String cardNumber,
                             @RequestParam(name = "money") String money) {
        String authentication = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByUsername(authentication);
        
        Optional<Account> optionalReceiverAccount = accountRepository.findByAccountNumber(cardNumber);
        if (optionalReceiverAccount.isPresent() && user.getDefaultAccount() != null) {
            Account receiverAccount = optionalReceiverAccount.get();
            double amount = Double.parseDouble(money);
            double senderBalance = user.getDefaultAccount().getBalance();

            if (senderBalance >= amount) {
                receiverAccount.setBalance(receiverAccount.getBalance() + amount);
                user.getDefaultAccount().setBalance(senderBalance - amount);

                accountRepository.save(receiverAccount);
                accountRepository.save(user.getDefaultAccount());
                
                createTransaction(receiverAccount.getId(), amount, user);
                return "redirect:transfer?success";
            } else {
                return "error_insufficient_balance";
            }
        } else {
            return "error_account_not_found";
        }
    }

    private void createTransaction(long receiverAccountId, double amount, User sender) {
        Transaction transaction = new Transaction();
        
        Optional<Account> receiverAccountOptional = accountRepository.findById(receiverAccountId);
        if (receiverAccountOptional.isPresent()) {
            User receiver = receiverAccountOptional.get().getUser();
            transaction.setReceiver(receiver);
            transaction.setSender(sender);
            transaction.setAmount(amount);
            transaction.setTransactionDate(LocalDate.now());

            sender.getSentTransactions().add(transaction);
            receiver.getReceivedTransactions().add(transaction);

            userRepository.save(sender);
            userRepository.save(receiver);
            transactionRepository.save(transaction);
        }
    }
}
