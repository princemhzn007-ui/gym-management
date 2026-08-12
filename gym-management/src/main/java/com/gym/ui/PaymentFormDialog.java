package com.gym.ui;

import com.gym.model.Member;
import com.gym.model.MembershipType;
import com.gym.model.PaymentMethod;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import java.awt.BorderLayout;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;

public class PaymentFormDialog extends JDialog {

    private final Member member;
    private final JTextField amountField = new JTextField(10);
    private final JTextField dateField = new JTextField(10);
    private final JComboBox<PaymentMethod> methodCombo = new JComboBox<>(PaymentMethod.values());
    private final JSpinner monthsSpinner = new JSpinner(new SpinnerNumberModel(1, 1, 24, 1));
    private final JTextField noteField = new JTextField(20);

    private boolean confirmed = false;

    public PaymentFormDialog(Frame owner, Member member) {
        super(owner, "Record Payment - " + member.getName(), true);
        this.member = member;

        setLayout(new BorderLayout(10, 10));
        setResizable(false);

        MembershipType type = member.getMembershipType();
        amountField.setText(String.valueOf(type.getMonthlyRate()));
        dateField.setText(LocalDate.now().toString());

        add(buildFormPanel(), BorderLayout.CENTER);
        add(buildButtonPanel(), BorderLayout.SOUTH);

        monthsSpinner.addChangeListener(e -> updateSuggestedAmount());

        pack();
        setLocationRelativeTo(owner);
    }

    private void updateSuggestedAmount() {
        int months = (Integer) monthsSpinner.getValue();
        double suggested = member.getMembershipType().getMonthlyRate() * months;
        amountField.setText(String.valueOf(suggested));
    }

    private JPanel buildFormPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(javax.swing.BorderFactory.createEmptyBorder(15, 15, 5, 15));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 6, 6, 6);
        gbc.anchor = GridBagConstraints.WEST;

        int row = 0;
        addRow(panel, gbc, row++, "Member:", new JLabel(member.getId() + " - " + member.getName()));
        addRow(panel, gbc, row++, "Months Covered:", monthsSpinner);
        addRow(panel, gbc, row++, "Amount ($):", amountField);
        addRow(panel, gbc, row++, "Date (yyyy-mm-dd):", dateField);
        addRow(panel, gbc, row++, "Method:", methodCombo);
        addRow(panel, gbc, row++, "Note (optional):", noteField);

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
        JButton saveButton = new JButton("Record Payment");
        JButton cancelButton = new JButton("Cancel");

        saveButton.addActionListener(e -> onSave());
        cancelButton.addActionListener(e -> dispose());

        panel.add(saveButton);
        panel.add(cancelButton);
        return panel;
    }

    private void onSave() {
        double amount;
        try {
            amount = Double.parseDouble(amountField.getText().trim());
            if (amount <= 0) {
                throw new NumberFormatException();
            }
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Enter a valid positive amount.",
                    "Validation Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            LocalDate.parse(dateField.getText().trim());
        } catch (DateTimeParseException ex) {
            JOptionPane.showMessageDialog(this, "Enter a valid date in yyyy-mm-dd format.",
                    "Validation Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        confirmed = true;
        dispose();
    }

    public boolean isConfirmed() {
        return confirmed;
    }

    public double getAmount() {
        return Double.parseDouble(amountField.getText().trim());
    }

    public LocalDate getDate() {
        return LocalDate.parse(dateField.getText().trim());
    }

    public PaymentMethod getMethod() {
        return (PaymentMethod) methodCombo.getSelectedItem();
    }

    public int getMonthsCovered() {
        return (Integer) monthsSpinner.getValue();
    }

    public String getNote() {
        return noteField.getText().trim();
    }
}
