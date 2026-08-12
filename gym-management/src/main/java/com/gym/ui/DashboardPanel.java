package com.gym.ui;

import com.gym.model.Member;
import com.gym.storage.DataStore;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableModel;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.GridLayout;
import java.time.LocalDate;
import java.util.List;
import java.util.Locale;

public class DashboardPanel extends JPanel {

    private final DataStore dataStore;

    private JLabel totalMembersValue;
    private JLabel activeMembersValue;
    private JLabel todayAttendanceValue;
    private JLabel monthRevenueValue;
    private JLabel paymentDueValue;

    private DefaultTableModel dueTableModel;

    public DashboardPanel(DataStore dataStore) {
        this.dataStore = dataStore;
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        add(buildStatsPanel(), BorderLayout.NORTH);
        add(buildDueTablePanel(), BorderLayout.CENTER);

        refresh();
    }

    private JPanel buildStatsPanel() {
        JPanel panel = new JPanel(new GridLayout(1, 5, 12, 0));

        totalMembersValue = new JLabel();
        activeMembersValue = new JLabel();
        todayAttendanceValue = new JLabel();
        monthRevenueValue = new JLabel();
        paymentDueValue = new JLabel();

        panel.add(statCard("Total Members", totalMembersValue, new Color(63, 81, 181)));
        panel.add(statCard("Active Members", activeMembersValue, new Color(76, 175, 80)));
        panel.add(statCard("Checked In Today", todayAttendanceValue, new Color(0, 150, 136)));
        panel.add(statCard("Revenue This Month", monthRevenueValue, new Color(255, 152, 0)));
        panel.add(statCard("Payments Due", paymentDueValue, new Color(244, 67, 54)));

        return panel;
    }

    private JPanel statCard(String title, JLabel valueLabel, Color accent) {
        JPanel card = new JPanel();
        card.setLayout(new BorderLayout());
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(accent, 2, true),
                BorderFactory.createEmptyBorder(12, 12, 12, 12)));

        JLabel titleLabel = new JLabel(title, SwingConstants.CENTER);
        titleLabel.setFont(titleLabel.getFont().deriveFont(Font.PLAIN, 12f));

        valueLabel.setHorizontalAlignment(SwingConstants.CENTER);
        valueLabel.setFont(valueLabel.getFont().deriveFont(Font.BOLD, 26f));
        valueLabel.setForeground(accent);

        card.add(titleLabel, BorderLayout.NORTH);
        card.add(valueLabel, BorderLayout.CENTER);
        return card;
    }

    private JPanel buildDueTablePanel() {
        JPanel panel = new JPanel(new BorderLayout(5, 5));
        JLabel header = new JLabel("Members with Payment Due");
        header.setFont(header.getFont().deriveFont(Font.BOLD, 15f));
        panel.add(header, BorderLayout.NORTH);

        dueTableModel = new DefaultTableModel(
                new Object[]{"ID", "Name", "Phone", "Membership", "Expiry Date"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        JTable table = new JTable(dueTableModel);
        table.setRowHeight(24);
        panel.add(new JScrollPane(table), BorderLayout.CENTER);
        return panel;
    }

    public void refresh() {
        List<Member> members = dataStore.getMembers();
        long activeCount = members.stream().filter(Member::isActive).count();

        totalMembersValue.setText(String.valueOf(members.size()));
        activeMembersValue.setText(String.valueOf(activeCount));
        todayAttendanceValue.setText(String.valueOf(dataStore.getTodayAttendanceCount()));

        LocalDate now = LocalDate.now();
        double monthRevenue = dataStore.getRevenueForMonth(now.getYear(), now.getMonthValue());
        monthRevenueValue.setText(String.format(Locale.US, "$%.2f", monthRevenue));

        List<Member> due = dataStore.getMembersWithPaymentDue();
        paymentDueValue.setText(String.valueOf(due.size()));

        dueTableModel.setRowCount(0);
        for (Member m : due) {
            dueTableModel.addRow(new Object[]{
                    m.getId(),
                    m.getName(),
                    m.getPhone(),
                    m.getMembershipType().getLabel(),
                    m.getExpiryDate() == null ? "Never paid" : m.getExpiryDate().toString()
            });
        }
    }
}
