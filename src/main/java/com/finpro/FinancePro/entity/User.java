package com.finpro.FinancePro.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String fullName;

    @Column(nullable = false)
    @JsonIgnore // Avoid exposing the password in JSON responses
    private String password;

    @Column(nullable = false)
    private String email;

    private Provider provider = Provider.SELF;    // GOOGLE, GITHUB

    @Embedded
    private TwoFactorAuth twoFactorAuth = new TwoFactorAuth();

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore // Prevent infinite recursion
    private List<Transaction> transactions;

    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore // Prevent infinite recursion
    private Goal goal;

    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore // Prevent infinite recursion
    private Budget budgets;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore // Prevent infinite recursion
    private List<Investment> investments;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Provider getProvider() {
        return provider;
    }

    public void setProvider(Provider provider) {
        this.provider = provider;
    }

    public TwoFactorAuth getTwoFactorAuth() {
        return twoFactorAuth;
    }

    public void setTwoFactorAuth(TwoFactorAuth twoFactorAuth) {
        this.twoFactorAuth = twoFactorAuth;
    }

    public List<Transaction> getTransactions() {
        return transactions;
    }

    public void setTransactions(List<Transaction> transactions) {
        this.transactions = transactions;
    }

    public Budget getBudgets() {
        return budgets;
    }

    public void setBudgets(Budget budgets) {
        this.budgets = budgets;
    }

    public List<Investment> getInvestments() {
        return investments;
    }

    public void setInvestments(List<Investment> investments) {
        this.investments = investments;
    }

    public Goal getGoal() {
        return goal;
    }

    public void setGoal(Goal goal) {
        this.goal = goal;
    }
}
