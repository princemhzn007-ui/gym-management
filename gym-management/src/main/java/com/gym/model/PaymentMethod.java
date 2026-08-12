package com.gym.model;

public enum PaymentMethod {
    CASH("Cash"),
    CARD("Card"),
    UPI("UPI"),
    BANK_TRANSFER("Bank Transfer");

    private final String label;

    PaymentMethod(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }

    @Override
    public String toString() {
        return label;
    }
}
