package com.gym.model;

import java.time.LocalDate;

/**
 * Represents a gym member.
 */
public class Member {

    private String id;
    private String name;
    private String phone;
    private String email;
    private MembershipType membershipType;
    private LocalDate joinDate;
    private boolean active;
    private LocalDate expiryDate; // null = never paid

    public Member(String id, String name, String phone, String email,
                  MembershipType membershipType, LocalDate joinDate,
                  boolean active, LocalDate expiryDate) {
        this.id = id;
        this.name = name;
        this.phone = phone;
        this.email = email;
        this.membershipType = membershipType;
        this.joinDate = joinDate;
        this.active = active;
        this.expiryDate = expiryDate;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public MembershipType getMembershipType() {
        return membershipType;
    }

    public void setMembershipType(MembershipType membershipType) {
        this.membershipType = membershipType;
    }

    public LocalDate getJoinDate() {
        return joinDate;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public LocalDate getExpiryDate() {
        return expiryDate;
    }

    public void setExpiryDate(LocalDate expiryDate) {
        this.expiryDate = expiryDate;
    }

    /** True if the membership payment has lapsed (expiry date before today). */
    public boolean isPaymentDue() {
        return expiryDate == null || expiryDate.isBefore(LocalDate.now());
    }

    public String getStatusLabel() {
        if (!active) {
            return "Inactive";
        }
        return isPaymentDue() ? "Active (Payment Due)" : "Active";
    }
}
