package com.gym.model;

import java.time.LocalDate;
import java.time.LocalTime;

/**
 * Represents a single attendance (check-in / check-out) event for a member.
 */
public class AttendanceRecord {

    private String id;
    private String memberId;
    private LocalDate date;
    private LocalTime checkInTime;
    private LocalTime checkOutTime; // nullable, set on check-out

    public AttendanceRecord(String id, String memberId, LocalDate date,
                             LocalTime checkInTime, LocalTime checkOutTime) {
        this.id = id;
        this.memberId = memberId;
        this.date = date;
        this.checkInTime = checkInTime;
        this.checkOutTime = checkOutTime;
    }

    public String getId() {
        return id;
    }

    public String getMemberId() {
        return memberId;
    }

    public LocalDate getDate() {
        return date;
    }

    public LocalTime getCheckInTime() {
        return checkInTime;
    }

    public LocalTime getCheckOutTime() {
        return checkOutTime;
    }

    public void setCheckOutTime(LocalTime checkOutTime) {
        this.checkOutTime = checkOutTime;
    }

    public boolean isCheckedOut() {
        return checkOutTime != null;
    }
}
