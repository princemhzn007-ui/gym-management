package com.gym.ui;

import com.gym.model.Member;
import com.gym.model.Payment;
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
import javax.swing.SwingUtilities;
import javax.swing.table.DefaultTableModel;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Font;
import java.util.List;
import java.util.Locale;

public class PaymentsPanel extends JPanel {

    private final DataStore dataStore;
    private final Runnable onDataChanged;

    private final JComboBox<MemberItem> memberCombo = new JComboBox<>();
    private final DefaultTableModel tableModel;
    private final JTable table;
    private final JLabel totalRevenueLabel = new JLabel();

    public PaymentsPanel(DataStore dataStore, Runnable onDataChanged) {
        this.dataStore = dataStore;
        this.onDataChanged = onDataChanged;

        setLayout(new BorderLayout(8, 8));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        tableModel = new DefaultTableModel(
                new Object[]{"Payment ID", "Member ID", "Name", "Amount", "Date", "Method", "Months", "Note"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        table = new JTable(tableModel);
        table.setRowHeight(24);

        add(buildTopBar(), BorderLayout.NORTH);
        add(new JScrollPane(table), BorderLayout.CENTER);
        add(buildBottomBar(), BorderLayout.SOUTH);

        refresh();
    }

    private JPanel buildTopBar() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        panel.add(new JLabel("Member:"));
        panel.add(memberCombo);
        JButton recordBtn = new JButton("Record Payment");
        recordBtn.addActionListener(e -> onRecordPayment());
        panel.add(recordBtn);
        return panel;
    }

    private JPanel buildBottomBar() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        totalRevenueLabel.setFont(totalRevenueLabel.getFont().deriveFont(Font.BOLD, 13f));
        panel.add(totalRevenueLabel);
        return panel;
    }

    private void onRecordPayment() {
        MemberItem item = (MemberItem) memberCombo.getSelectedItem();
        if (item == null) {
            JOptionPane.showMessageDialog(this, "No members available. Add a member first.",
                    "No Members", JOptionPane.WARNING_MESSAGE);
            return;
        }
        PaymentFormDialog dialog = new PaymentFormDialog(
                (java.awt.Frame) SwingUtilities.getWindowAncestor(this), item.member);
        dialog.setVisible(true);
        if (dialog.isConfirmed()) {
            dataStore.addPayment(item.member.getId(), dialog.getAmount(), dialog.getDate(),
                    dialog.getMethod(), dialog.getMonthsCovered(), dialog.getNote());
            refreshAndNotify();
        }
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
            model.addElement(new MemberItem(m));
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
        List<Payment> payments = dataStore.getPayments();
        for (Payment p : payments) {
            String name = dataStore.findMember(p.getMemberId())
                    .map(Member::getName)
                    .orElse("(deleted member)");
            tableModel.addRow(new Object[]{
                    p.getId(),
                    p.getMemberId(),
                    name,
                    String.format(Locale.US, "$%.2f", p.getAmount()),
                    p.getDate().toString(),
                    p.getMethod().getLabel(),
                    p.getMonthsCovered(),
                    p.getNote()
            });
        }
        totalRevenueLabel.setText(String.format(Locale.US, "Total Revenue: $%.2f", dataStore.getTotalRevenue()));
    }

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
