package com.gym.ui;

import com.gym.model.AttendanceRecord;
import com.gym.model.Member;
import com.gym.storage.DataStore;

import javax.swing.BorderFactory;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.table.DefaultTableModel;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;

public class AttendancePanel extends JPanel {

    private final DataStore dataStore;
    private final Runnable onDataChanged;

    private final JComboBox<MemberItem> memberCombo = new JComboBox<>();
    private final JTextField dateField = new JTextField(10);
    private final DefaultTableModel tableModel;
    private final JTable table;
    private final JLabel countLabel = new JLabel();

    public AttendancePanel(DataStore dataStore, Runnable onDataChanged) {
        this.dataStore = dataStore;
        this.onDataChanged = onDataChanged;

        setLayout(new BorderLayout(8, 8));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        tableModel = new DefaultTableModel(
                new Object[]{"Record ID", "Member ID", "Name", "Date", "Check-In", "Check-Out"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        table = new JTable(tableModel);
        table.setRowHeight(24);

        dateField.setText(LocalDate.now().toString());

        add(buildCheckInBar(), BorderLayout.NORTH);
        add(new JScrollPane(table), BorderLayout.CENTER);
        add(buildBottomBar(), BorderLayout.SOUTH);

        refresh();
    }

    private JPanel buildCheckInBar() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        panel.add(new JLabel("Member:"));
        panel.add(memberCombo);
        JButton checkInBtn = new JButton("Check In");
        checkInBtn.addActionListener(e -> onCheckIn());
        panel.add(checkInBtn);
        return panel;
    }

    private JPanel buildBottomBar() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        panel.add(new JLabel("Date (yyyy-mm-dd):"));
        panel.add(dateField);
        JButton showBtn = new JButton("Show Date");
        JButton todayBtn = new JButton("Today");
        JButton allBtn = new JButton("Show All");
        JButton checkOutBtn = new JButton("Check Out Selected");

        showBtn.addActionListener(e -> refresh());
        todayBtn.addActionListener(e -> {
            dateField.setText(LocalDate.now().toString());
            refresh();
        });
        allBtn.addActionListener(e -> {
            dateField.setText("");
            refresh();
        });
        checkOutBtn.addActionListener(e -> onCheckOut());

        panel.add(showBtn);
        panel.add(todayBtn);
        panel.add(allBtn);
        panel.add(checkOutBtn);
        panel.add(countLabel);
        return panel;
    }

    private void onCheckIn() {
        MemberItem item = (MemberItem) memberCombo.getSelectedItem();
        if (item == null) {
            JOptionPane.showMessageDialog(this, "No active members available to check in.",
                    "No Members", JOptionPane.WARNING_MESSAGE);
            return;
        }
        if (dataStore.hasOpenSessionToday(item.member.getId())) {
            JOptionPane.showMessageDialog(this,
                    item.member.getName() + " already has an open check-in today.",
                    "Already Checked In", JOptionPane.WARNING_MESSAGE);
            return;
        }
        dataStore.checkIn(item.member.getId());
        refreshAndNotify();
    }

    private void onCheckOut() {
        int row = table.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Please select an attendance record first.",
                    "No Selection", JOptionPane.WARNING_MESSAGE);
            return;
        }
        String recordId = (String) tableModel.getValueAt(row, 0);
        String checkOutVal = (String) tableModel.getValueAt(row, 5);
        if (!"-".equals(checkOutVal)) {
            JOptionPane.showMessageDialog(this, "This record is already checked out.",
                    "Already Checked Out", JOptionPane.WARNING_MESSAGE);
            return;
        }
        dataStore.checkOut(recordId);
        refreshAndNotify();
    }

    private void refreshAndNotify() {
        refresh();
        if (onDataChanged != null) {
            onDataChanged.run();
        }
    }

    public void refresh() {
        refreshMemberCombo();
        refreshTable();
    }

    private void refreshMemberCombo() {
        MemberItem previouslySelected = (MemberItem) memberCombo.getSelectedItem();
        DefaultComboBoxModel<MemberItem> model = new DefaultComboBoxModel<>();
        for (Member m : dataStore.getMembers()) {
            if (m.isActive()) {
                model.addElement(new MemberItem(m));
            }
        }
        memberCombo.setModel(model);
        if (previouslySelected != null) {
            for (int i = 0; i < model.getSize(); i++) {
                if (model.getElementAt(i).member.getId().equals(previouslySelected.member.getId())) {
                    memberCombo.setSelectedIndex(i);
                    break;
                }
            }
        }
    }

    private void refreshTable() {
        tableModel.setRowCount(0);
        String dateText = dateField.getText().trim();
        List<AttendanceRecord> records;
        if (dateText.isEmpty()) {
            records = dataStore.getAttendanceRecords();
        } else {
            try {
                records = dataStore.getAttendanceForDate(LocalDate.parse(dateText));
            } catch (DateTimeParseException ex) {
                records = dataStore.getAttendanceRecords();
            }
        }

        for (AttendanceRecord a : records) {
            String name = dataStore.findMember(a.getMemberId())
                    .map(Member::getName)
                    .orElse("(deleted member)");
            tableModel.addRow(new Object[]{
                    a.getId(),
                    a.getMemberId(),
                    name,
                    a.getDate().toString(),
                    a.getCheckInTime().toString(),
                    a.getCheckOutTime() == null ? "-" : a.getCheckOutTime().toString()
            });
        }
        countLabel.setText("  " + records.size() + " record(s)");
    }

    /** Wraps a Member so the combo box displays "ID - Name" while keeping the object reference. */
    private static class MemberItem {
        final Member member;

        MemberItem(Member member) {
            this.member = member;
        }

        @Override
        public String toString() {
            return member.getId() + " - " + member.getName();
        }
    }
}
