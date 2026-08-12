package com.gym.model;

import java.time.LocalDate;

/**
 * Represents a payment made by a member.
 */
public class Payment {

    private String id;
    private String memberId;
    private double amount;
    private LocalDate date;
    private PaymentMethod method;
    private int monthsCovered;
    private String note;

    public Payment(String id, String memberId, double amount, LocalDate date,
                    PaymentMethod method, int monthsCovered, String note) {
        this.id = id;
        this.memberId = memberId;
        this.amount = amount;
        this.date = date;
        this.method = method;
        this.monthsCovered = monthsCovered;
        this.note = note;
    }

    public String getId() {
        return id;
    }

    public String getMemberId() {
        return memberId;
    }

    public double getAmount() {
        return amount;
    }

    public LocalDate getDate() {
        return date;
    }

    public PaymentMethod getMethod() {
        return method;
    }

    public int getMonthsCovered() {
        return monthsCovered;
    }

    public String getNote() {
        return note == null ? "" : note;
    }
}
