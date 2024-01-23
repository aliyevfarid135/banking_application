package com.blog.azerbaijani.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@Entity
@Data//LOMBOK
@NoArgsConstructor//LOMBOK
@AllArgsConstructor//LOMBOK
public class Account {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "account_number", unique = true, nullable = false)
    private String accountNumber;

    @Column(name = "cvv_number", nullable = false)
    private String cvvNumber;

    @Column(name = "date", nullable = false)
    private String date;

    @Column(name = "balance", nullable = false)
    private Double balance;

}
