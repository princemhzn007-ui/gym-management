package com.gym.ui;

import com.gym.model.Member;
import com.gym.model.MembershipType;
import com.gym.storage.DataStore;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.table.DefaultTableModel;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.util.List;

public class MembersPanel extends JPanel {

    private final DataStore dataStore;
    private final Runnable onDataChanged;

    private final DefaultTableModel tableModel;
    private final JTable table;
    private final JTextField searchField = new JTextField(18);

    public MembersPanel(DataStore dataStore, Runnable onDataChanged) {
        this.dataStore = dataStore;
        this.onDataChanged = onDataChanged;

        setLayout(new BorderLayout(8, 8));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        tableModel = new DefaultTableModel(
                new Object[]{"ID", "Name", "Phone", "Email", "Type", "Join Date", "Status", "Expiry"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        table = new JTable(tableModel);
        table.setRowHeight(24);
        table.getSelectionModel().setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);

        add(buildTopBar(), BorderLayout.NORTH);
        add(new JScrollPane(table), BorderLayout.CENTER);
        add(buildActionBar(), BorderLayout.SOUTH);

        refresh();
    }

    private JPanel buildTopBar() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        panel.add(new JLabel("Search:"));
        panel.add(searchField);
        JButton searchBtn = new JButton("Filter");
        JButton clearBtn = new JButton("Clear");
        searchBtn.addActionListener(e -> refresh());
        clearBtn.addActionListener(e -> {
            searchField.setText("");
            refresh();
        });
        panel.add(searchBtn);
        panel.add(clearBtn);
        return panel;
    }

    private JPanel buildActionBar() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT));

        JButton addBtn = new JButton("Add Member");
        JButton editBtn = new JButton("Edit Member");
        JButton activateBtn = new JButton("Activate");
        JButton deactivateBtn = new JButton("Deactivate");
        JButton deleteBtn = new JButton("Delete");

        addBtn.addActionListener(e -> onAdd());
        editBtn.addActionListener(e -> onEdit());
        activateBtn.addActionListener(e -> onSetActive(true));
        deactivateBtn.addActionListener(e -> onSetActive(false));
        deleteBtn.addActionListener(e -> onDelete());

        panel.add(addBtn);
        panel.add(editBtn);
        panel.add(activateBtn);
        panel.add(deactivateBtn);
        panel.add(deleteBtn);
        return panel;
    }

    private void onAdd() {
        MemberFormDialog dialog = new MemberFormDialog(
                (java.awt.Frame) SwingUtilities.getWindowAncestor(this), null);
        dialog.setVisible(true);
        if (dialog.isConfirmed()) {
            dataStore.addMember(dialog.getMemberName(), dialog.getPhone(), dialog.getEmail(),
                    dialog.getMembershipType(), dialog.getJoinDate());
            refreshAndNotify();
        }
    }

    private void onEdit() {
        Member selected = getSelectedMember();
        if (selected == null) {
            showNoSelectionWarning();
            return;
        }
        MemberFormDialog dialog = new MemberFormDialog(
                (java.awt.Frame) SwingUtilities.getWindowAncestor(this), selected);
        dialog.setVisible(true);
        if (dialog.isConfirmed()) {
            selected.setName(dialog.getMemberName());
            selected.setPhone(dialog.getPhone());
            selected.setEmail(dialog.getEmail());
            selected.setMembershipType(dialog.getMembershipType());
            dataStore.updateMember(selected);
            refreshAndNotify();
        }
    }

    private void onSetActive(boolean active) {
        Member selected = getSelectedMember();
        if (selected == null) {
            showNoSelectionWarning();
            return;
        }
        dataStore.setMemberActive(selected.getId(), active);
        refreshAndNotify();
    }

    private void onDelete() {
        Member selected = getSelectedMember();
        if (selected == null) {
            showNoSelectionWarning();
            return;
        }
        int confirm = JOptionPane.showConfirmDialog(this,
                "Delete member \"" + selected.getName() + "\" (" + selected.getId() + ")?\n"
                        + "Their attendance and payment history will be kept but no longer linked in this view.",
                "Confirm Delete", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (confirm == JOptionPane.YES_OPTION) {
            dataStore.deleteMember(selected.getId());
            refreshAndNotify();
        }
    }

    private Member getSelectedMember() {
        int row = table.getSelectedRow();
        if (row < 0) {
            return null;
        }
        String id = (String) tableModel.getValueAt(row, 0);
        return dataStore.findMember(id).orElse(null);
    }

    private void showNoSelectionWarning() {
        JOptionPane.showMessageDialog(this, "Please select a member first.",
                "No Selection", JOptionPane.WARNING_MESSAGE);
    }

    private void refreshAndNotify() {
        refresh();
        if (onDataChanged != null) {
            onDataChanged.run();
        }
    }

    public void refresh() {
        String query = searchField.getText().trim().toLowerCase();
        tableModel.setRowCount(0);
        List<Member> members = dataStore.getMembers();
        for (Member m : members) {
            if (!query.isEmpty()
                    && !m.getName().toLowerCase().contains(query)
                    && !m.getId().toLowerCase().contains(query)) {
                continue;
            }
            tableModel.addRow(new Object[]{
                    m.getId(),
                    m.getName(),
                    m.getPhone(),
                    m.getEmail(),
                    m.getMembershipType().getLabel(),
                    m.getJoinDate().toString(),
                    m.getStatusLabel(),
                    m.getExpiryDate() == null ? "-" : m.getExpiryDate().toString()
            });
        }
    }
}
