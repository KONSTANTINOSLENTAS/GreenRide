package com.greenride.greenride.dto;

public class AdminStatsResponse {
    private long totalUsers;
    private long totalRoutes;
    private long activeRoutes;
    private double averageOccupancy;

    public AdminStatsResponse(long totalUsers, long totalRoutes, long activeRoutes, double averageOccupancy) {
        this.totalUsers = totalUsers;
        this.totalRoutes = totalRoutes;
        this.activeRoutes = activeRoutes;
        this.averageOccupancy = averageOccupancy;
    }

    public long getTotalUsers() { return totalUsers; }
    public long getTotalRoutes() { return totalRoutes; }
    public long getActiveRoutes() { return activeRoutes; }
    public double getAverageOccupancy() { return averageOccupancy; }
}