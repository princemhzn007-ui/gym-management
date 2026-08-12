package com.gym.ui;

import com.gym.model.Member;
import com.gym.model.MembershipType;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import java.awt.BorderLayout;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.time.LocalDate;

/**
 * Modal dialog used for both "Add Member" and "Edit Member" flows.
 * If an existing Member is passed in, its fields pre-populate the form
 * and the id/join-date are preserved; otherwise a brand new member is created.
 */
public class MemberFormDialog extends JDialog {

    private final JTextField nameField = new JTextField(20);
    private final JTextField phoneField = new JTextField(20);
    private final JTextField emailField = new JTextField(20);
    private final JComboBox<MembershipType> typeCombo = new JComboBox<>(MembershipType.values());

    private boolean confirmed = false;
    private final Member existingMember; // null when adding

    public MemberFormDialog(Frame owner, Member existingMember) {
        super(owner, existingMember == null ? "Add Member" : "Edit Member", true);
        this.existingMember = existingMember;

        setLayout(new BorderLayout(10, 10));
        setResizable(false);

        add(buildFormPanel(), BorderLayout.CENTER);
        add(buildButtonPanel(), BorderLayout.SOUTH);

        if (existingMember != null) {
            nameField.setText(existingMember.getName());
            phoneField.setText(existingMember.getPhone());
            emailField.setText(existingMember.getEmail());
            typeCombo.setSelectedItem(existingMember.getMembershipType());
        }

        pack();
        setLocationRelativeTo(owner);
    }

    private JPanel buildFormPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(javax.swing.BorderFactory.createEmptyBorder(15, 15, 5, 15));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 6, 6, 6);
        gbc.anchor = GridBagConstraints.WEST;

        int row = 0;
        addRow(panel, gbc, row++, "Full Name:", nameField);
        addRow(panel, gbc, row++, "Phone:", phoneField);
        addRow(panel, gbc, row++, "Email:", emailField);
        addRow(panel, gbc, row++, "Membership Type:", typeCombo);

        return panel;
    }

    private void addRow(JPanel panel, GridBagConstraints gbc, int row, String label, java.awt.Component field) {
        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.weightx = 0;
        panel.add(new JLabel(label), gbc);

        gbc.gridx = 1;
        gbc.weightx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        panel.add(field, gbc);
        gbc.fill = GridBagConstraints.NONE;
    }

    private JPanel buildButtonPanel() {
        JPanel panel = new JPanel();
        JButton saveButton = new JButton(existingMember == null ? "Add Member" : "Save Changes");
        JButton cancelButton = new JButton("Cancel");

        saveButton.addActionListener(e -> onSave());
        cancelButton.addActionListener(e -> dispose());

        panel.add(saveButton);
        panel.add(cancelButton);
        return panel;
    }

    private void onSave() {
        String name = nameField.getText().trim();
        if (name.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Name is required.", "Validation Error", JOptionPane.WARNING_MESSAGE);
            return;
        }
        confirmed = true;
        dispose();
    }

    public boolean isConfirmed() {
        return confirmed;
    }

    public String getMemberName() {
        return nameField.getText().trim();
    }

    public String getPhone() {
        return phoneField.getText().trim();
    }

    public String getEmail() {
        return emailField.getText().trim();
    }

    public MembershipType getMembershipType() {
        return (MembershipType) typeCombo.getSelectedItem();
    }

    public LocalDate getJoinDate() {
        return existingMember == null ? LocalDate.now() : existingMember.getJoinDate();
    }
}
