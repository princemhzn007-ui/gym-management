package com.gym.model;

public enum MembershipType {
    BASIC("Basic", 20.0),
    STANDARD("Standard", 35.0),
    PREMIUM("Premium", 55.0);

    private final String label;
    private final double monthlyRate;

    MembershipType(String label, double monthlyRate) {
        this.label = label;
        this.monthlyRate = monthlyRate;
    }

    public String getLabel() {
        return label;
    }

    public double getMonthlyRate() {
        return monthlyRate;
    }

    @Override
    public String toString() {
        return label;
    }
}
