package com.example.demo.dto;

public class DashboardDTO {

    private long totalUsers;
    private long activeUsers;
    private long inactiveUsers;
    private long premiumUsers;

    public DashboardDTO(long totalUsers,
                        long activeUsers,
                        long inactiveUsers,
                        long premiumUsers) {
        this.totalUsers = totalUsers;
        this.activeUsers = activeUsers;
        this.inactiveUsers = inactiveUsers;
        this.premiumUsers = premiumUsers;
    }

    public long getTotalUsers() {
        return totalUsers;
    }

    public long getActiveUsers() {
        return activeUsers;
    }

    public long getInactiveUsers() {
        return inactiveUsers;
    }

    public long getPremiumUsers() {
        return premiumUsers;
    }
}