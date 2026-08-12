package com.gym.storage;

import com.gym.model.AttendanceRecord;
import com.gym.model.Member;
import com.gym.model.MembershipType;
import com.gym.model.Payment;
import com.gym.model.PaymentMethod;
import com.gym.util.CsvUtil;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

/**
 * Central in-memory data store backed by simple CSV files on disk
 * (data/members.csv, data/payments.csv, data/attendance.csv).
 * Every mutating operation persists immediately, so the app has no
 * "unsaved changes" state to worry about.
 */
public class DataStore {

    private final File dataDir;
    private final File membersFile;
    private final File paymentsFile;
    private final File attendanceFile;

    private final List<Member> members = new ArrayList<>();
    private final List<Payment> payments = new ArrayList<>();
    private final List<AttendanceRecord> attendanceRecords = new ArrayList<>();

    private int memberCounter = 0;
    private int paymentCounter = 0;
    private int attendanceCounter = 0;

    public DataStore(String dataDirPath) {
        this.dataDir = new File(dataDirPath);
        if (!dataDir.exists()) {
            dataDir.mkdirs();
        }
        this.membersFile = new File(dataDir, "members.csv");
        this.paymentsFile = new File(dataDir, "payments.csv");
        this.attendanceFile = new File(dataDir, "attendance.csv");
        loadAll();
    }

    // ---------------------------------------------------------------
    // Loading
    // ---------------------------------------------------------------

    private void loadAll() {
        loadMembers();
        loadPayments();
        loadAttendance();
    }

    private void loadMembers() {
        if (!membersFile.exists()) {
            return;
        }
        try (BufferedReader reader = new BufferedReader(new FileReader(membersFile))) {
            String line = reader.readLine(); // header
            while ((line = reader.readLine()) != null) {
                if (line.isBlank()) {
                    continue;
                }
                List<String> f = CsvUtil.parseLine(line);
                Member m = new Member(
                        f.get(0),
                        f.get(1),
                        f.get(2),
                        f.get(3),
                        MembershipType.valueOf(f.get(4)),
                        LocalDate.parse(f.get(5)),
                        Boolean.parseBoolean(f.get(6)),
                        CsvUtil.emptyToNull(f.get(7)) == null ? null : LocalDate.parse(f.get(7))
                );
                members.add(m);
                updateCounterFromId(m.getId(), "M");
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to load members.csv: " + e.getMessage(), e);
        }
    }

    private void loadPayments() {
        if (!paymentsFile.exists()) {
            return;
        }
        try (BufferedReader reader = new BufferedReader(new FileReader(paymentsFile))) {
            String line = reader.readLine(); // header
            while ((line = reader.readLine()) != null) {
                if (line.isBlank()) {
                    continue;
                }
                List<String> f = CsvUtil.parseLine(line);
                Payment p = new Payment(
                        f.get(0),
                        f.get(1),
                        Double.parseDouble(f.get(2)),
                        LocalDate.parse(f.get(3)),
                        PaymentMethod.valueOf(f.get(4)),
                        Integer.parseInt(f.get(5)),
                        f.size() > 6 ? f.get(6) : ""
                );
                payments.add(p);
                updateCounterFromId(p.getId(), "P");
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to load payments.csv: " + e.getMessage(), e);
        }
    }

    private void loadAttendance() {
        if (!attendanceFile.exists()) {
            return;
        }
        try (BufferedReader reader = new BufferedReader(new FileReader(attendanceFile))) {
            String line = reader.readLine(); // header
            while ((line = reader.readLine()) != null) {
                if (line.isBlank()) {
                    continue;
                }
                List<String> f = CsvUtil.parseLine(line);
                String coStr = CsvUtil.emptyToNull(f.get(4));
                AttendanceRecord a = new AttendanceRecord(
                        f.get(0),
                        f.get(1),
                        LocalDate.parse(f.get(2)),
                        LocalTime.parse(f.get(3)),
                        coStr == null ? null : LocalTime.parse(coStr)
                );
                attendanceRecords.add(a);
                updateCounterFromId(a.getId(), "A");
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to load attendance.csv: " + e.getMessage(), e);
        }
    }

    private void updateCounterFromId(String id, String prefix) {
        try {
            int n = Integer.parseInt(id.substring(prefix.length()));
            switch (prefix) {
                case "M" -> memberCounter = Math.max(memberCounter, n);
                case "P" -> paymentCounter = Math.max(paymentCounter, n);
                case "A" -> attendanceCounter = Math.max(attendanceCounter, n);
                default -> { }
            }
        } catch (Exception ignored) {
            // ignore malformed ids
        }
    }

    // ---------------------------------------------------------------
    // Saving
    // ---------------------------------------------------------------

    private void saveMembers() {
        try (BufferedWriter w = new BufferedWriter(new FileWriter(membersFile))) {
            w.write("id,name,phone,email,membershipType,joinDate,active,expiryDate");
            w.newLine();
            for (Member m : members) {
                List<String> row = List.of(
                        m.getId(),
                        m.getName(),
                        CsvUtil.nullToEmpty(m.getPhone()),
                        CsvUtil.nullToEmpty(m.getEmail()),
                        m.getMembershipType().name(),
                        m.getJoinDate().toString(),
                        String.valueOf(m.isActive()),
                        m.getExpiryDate() == null ? "" : m.getExpiryDate().toString()
                );
                w.write(CsvUtil.join(row));
                w.newLine();
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to save members.csv: " + e.getMessage(), e);
        }
    }

    private void savePayments() {
        try (BufferedWriter w = new BufferedWriter(new FileWriter(paymentsFile))) {
            w.write("id,memberId,amount,date,method,monthsCovered,note");
            w.newLine();
            for (Payment p : payments) {
                List<String> row = List.of(
                        p.getId(),
                        p.getMemberId(),
                        String.valueOf(p.getAmount()),
                        p.getDate().toString(),
                        p.getMethod().name(),
                        String.valueOf(p.getMonthsCovered()),
                        p.getNote()
                );
                w.write(CsvUtil.join(row));
                w.newLine();
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to save payments.csv: " + e.getMessage(), e);
        }
    }

    private void saveAttendance() {
        try (BufferedWriter w = new BufferedWriter(new FileWriter(attendanceFile))) {
            w.write("id,memberId,date,checkInTime,checkOutTime");
            w.newLine();
            for (AttendanceRecord a : attendanceRecords) {
                List<String> row = List.of(
                        a.getId(),
                        a.getMemberId(),
                        a.getDate().toString(),
                        a.getCheckInTime().toString(),
                        a.getCheckOutTime() == null ? "" : a.getCheckOutTime().toString()
                );
                w.write(CsvUtil.join(row));
                w.newLine();
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to save attendance.csv: " + e.getMessage(), e);
        }
    }

    // ---------------------------------------------------------------
    // Member operations
    // ---------------------------------------------------------------

    public List<Member> getMembers() {
        List<Member> copy = new ArrayList<>(members);
        copy.sort(Comparator.comparing(Member::getId));
        return copy;
    }

    public Optional<Member> findMember(String id) {
        return members.stream().filter(m -> m.getId().equals(id)).findFirst();
    }

    public Member addMember(String name, String phone, String email,
                             MembershipType type, LocalDate joinDate) {
        String id = "M" + String.format("%04d", ++memberCounter);
        Member m = new Member(id, name, phone, email, type, joinDate, true, null);
        members.add(m);
        saveMembers();
        return m;
    }

    public void updateMember(Member updated) {
        members.removeIf(m -> m.getId().equals(updated.getId()));
        members.add(updated);
        saveMembers();
    }

    public boolean deleteMember(String id) {
        boolean removed = members.removeIf(m -> m.getId().equals(id));
        if (removed) {
            saveMembers();
        }
        return removed;
    }

    public void setMemberActive(String id, boolean active) {
        findMember(id).ifPresent(m -> {
            m.setActive(active);
            saveMembers();
        });
    }

    // ---------------------------------------------------------------
    // Payment operations
    // ---------------------------------------------------------------

    public List<Payment> getPayments() {
        List<Payment> copy = new ArrayList<>(payments);
        copy.sort(Comparator.comparing(Payment::getDate).reversed());
        return copy;
    }

    public List<Payment> getPaymentsForMember(String memberId) {
        List<Payment> result = new ArrayList<>();
        for (Payment p : payments) {
            if (p.getMemberId().equals(memberId)) {
                result.add(p);
            }
        }
        result.sort(Comparator.comparing(Payment::getDate).reversed());
        return result;
    }

    /**
     * Records a payment and extends the member's membership expiry date.
     * Extension starts from the later of "today" or the member's current
     * expiry date, so early renewals stack instead of being wasted.
     */
    public Payment addPayment(String memberId, double amount, LocalDate date,
                               PaymentMethod method, int monthsCovered, String note) {
        String id = "P" + String.format("%04d", ++paymentCounter);
        Payment p = new Payment(id, memberId, amount, date, method, monthsCovered, note);
        payments.add(p);
        savePayments();

        findMember(memberId).ifPresent(m -> {
            LocalDate base = m.getExpiryDate() == null || m.getExpiryDate().isBefore(date)
                    ? date
                    : m.getExpiryDate();
            m.setExpiryDate(base.plusMonths(monthsCovered));
            saveMembers();
        });
        return p;
    }

    public double getTotalRevenue() {
        double total = 0;
        for (Payment p : payments) {
            total += p.getAmount();
        }
        return total;
    }

    public double getRevenueForMonth(int year, int month) {
        double total = 0;
        for (Payment p : payments) {
            if (p.getDate().getYear() == year && p.getDate().getMonthValue() == month) {
                total += p.getAmount();
            }
        }
        return total;
    }

    public List<Member> getMembersWithPaymentDue() {
        List<Member> due = new ArrayList<>();
        for (Member m : members) {
            if (m.isActive() && m.isPaymentDue()) {
                due.add(m);
            }
        }
        due.sort(Comparator.comparing(Member::getId));
        return due;
    }

    // ---------------------------------------------------------------
    // Attendance operations
    // ---------------------------------------------------------------

    public List<AttendanceRecord> getAttendanceRecords() {
        List<AttendanceRecord> copy = new ArrayList<>(attendanceRecords);
        copy.sort(Comparator.comparing(AttendanceRecord::getDate)
                .thenComparing(AttendanceRecord::getCheckInTime)
                .reversed());
        return copy;
    }

    public List<AttendanceRecord> getAttendanceForMember(String memberId) {
        List<AttendanceRecord> result = new ArrayList<>();
        for (AttendanceRecord a : attendanceRecords) {
            if (a.getMemberId().equals(memberId)) {
                result.add(a);
            }
        }
        result.sort(Comparator.comparing(AttendanceRecord::getDate)
                .thenComparing(AttendanceRecord::getCheckInTime)
                .reversed());
        return result;
    }

    public List<AttendanceRecord> getAttendanceForDate(LocalDate date) {
        List<AttendanceRecord> result = new ArrayList<>();
        for (AttendanceRecord a : attendanceRecords) {
            if (a.getDate().equals(date)) {
                result.add(a);
            }
        }
        result.sort(Comparator.comparing(AttendanceRecord::getCheckInTime));
        return result;
    }

    /** Returns true if the member already has an open (not checked-out) session today. */
    public boolean hasOpenSessionToday(String memberId) {
        LocalDate today = LocalDate.now();
        for (AttendanceRecord a : attendanceRecords) {
            if (a.getMemberId().equals(memberId) && a.getDate().equals(today) && !a.isCheckedOut()) {
                return true;
            }
        }
        return false;
    }

    public AttendanceRecord checkIn(String memberId) {
        String id = "A" + String.format("%05d", ++attendanceCounter);
        AttendanceRecord a = new AttendanceRecord(id, memberId, LocalDate.now(), LocalTime.now().withNano(0), null);
        attendanceRecords.add(a);
        saveAttendance();
        return a;
    }

    public boolean checkOut(String attendanceId) {
        for (AttendanceRecord a : attendanceRecords) {
            if (a.getId().equals(attendanceId) && !a.isCheckedOut()) {
                a.setCheckOutTime(LocalTime.now().withNano(0));
                saveAttendance();
                return true;
            }
        }
        return false;
    }

    public int getTodayAttendanceCount() {
        return getAttendanceForDate(LocalDate.now()).size();
    }
}
