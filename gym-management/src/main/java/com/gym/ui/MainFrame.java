package com.gym.ui;

import com.gym.storage.DataStore;

import javax.swing.JFrame;
import javax.swing.JTabbedPane;
import java.awt.Dimension;

public class MainFrame extends JFrame {

    private final DataStore dataStore;
    private final DashboardPanel dashboardPanel;
    private final MembersPanel membersPanel;
    private final AttendancePanel attendancePanel;
    private final PaymentsPanel paymentsPanel;

    public MainFrame(DataStore dataStore) {
        super("Gym Management System");
        this.dataStore = dataStore;

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(950, 620));
        setSize(1050, 680);
        setLocationRelativeTo(null);

        dashboardPanel = new DashboardPanel(dataStore);
        membersPanel = new MembersPanel(dataStore, this::refreshAll);
        attendancePanel = new AttendancePanel(dataStore, this::refreshAll);
        paymentsPanel = new PaymentsPanel(dataStore, this::refreshAll);

        JTabbedPane tabs = new JTabbedPane();
        tabs.addTab("Dashboard", dashboardPanel);
        tabs.addTab("Members", membersPanel);
        tabs.addTab("Attendance", attendancePanel);
        tabs.addTab("Payments", paymentsPanel);

        tabs.addChangeListener(e -> refreshAll());

        add(tabs);
    }

    private void refreshAll() {
        dashboardPanel.refresh();
        membersPanel.refresh();
        attendancePanel.refresh();
        paymentsPanel.refresh();
    }
}
