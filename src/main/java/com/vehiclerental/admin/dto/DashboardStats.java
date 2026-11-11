package com.vehiclerental.admin.dto;

import com.vehiclerental.booking.dto.VehicleRentalCount;

import java.math.BigDecimal;
import java.util.List;

/** Everything the admin dashboard shows, computed in one go by DashboardService. */
public class DashboardStats {

    private long pendingTransfers;
    private long openComplaints;
    private long unassignedDrivers;
    private long lowReviews;
    private long totalUsers;
    private long totalVehicles;
    private long availableVehicles;
    private long reservedVehicles;
    private long rentedVehicles;
    private long maintenanceVehicles;
    private long pendingBookings;
    private long confirmedBookings;
    private long activeBookings;
    private long fineDueBookings;
    private long completedBookings;
    private long cancelledBookings;
    private BigDecimal revenueToday = BigDecimal.ZERO;
    private BigDecimal revenueThisMonth = BigDecimal.ZERO;
    private BigDecimal revenueTotal = BigDecimal.ZERO;
    private BigDecimal revenueFromCompleted = BigDecimal.ZERO;
    private List<RevenuePoint> dailyRevenue = List.of();
    private List<RevenuePoint> monthlyRevenue = List.of();
    private List<VehicleRentalCount> mostRented = List.of();

    public long getTotalUsers() { return totalUsers; }
    public void setTotalUsers(long totalUsers) { this.totalUsers = totalUsers; }
    public long getTotalVehicles() { return totalVehicles; }
    public void setTotalVehicles(long totalVehicles) { this.totalVehicles = totalVehicles; }
    public long getAvailableVehicles() { return availableVehicles; }
    public void setAvailableVehicles(long availableVehicles) { this.availableVehicles = availableVehicles; }
    public long getReservedVehicles() { return reservedVehicles; }
    public void setReservedVehicles(long reservedVehicles) { this.reservedVehicles = reservedVehicles; }
    public long getRentedVehicles() { return rentedVehicles; }
    public void setRentedVehicles(long rentedVehicles) { this.rentedVehicles = rentedVehicles; }
    public long getMaintenanceVehicles() { return maintenanceVehicles; }
    public void setMaintenanceVehicles(long maintenanceVehicles) { this.maintenanceVehicles = maintenanceVehicles; }
    public long getPendingBookings() { return pendingBookings; }
    public void setPendingBookings(long pendingBookings) { this.pendingBookings = pendingBookings; }
    public long getConfirmedBookings() { return confirmedBookings; }
    public void setConfirmedBookings(long confirmedBookings) { this.confirmedBookings = confirmedBookings; }
    public long getActiveBookings() { return activeBookings; }
    public void setActiveBookings(long activeBookings) { this.activeBookings = activeBookings; }
    public long getFineDueBookings() { return fineDueBookings; }
    public void setFineDueBookings(long fineDueBookings) { this.fineDueBookings = fineDueBookings; }
    public long getCompletedBookings() { return completedBookings; }
    public void setCompletedBookings(long completedBookings) { this.completedBookings = completedBookings; }
    public long getCancelledBookings() { return cancelledBookings; }
    public void setCancelledBookings(long cancelledBookings) { this.cancelledBookings = cancelledBookings; }
    public BigDecimal getRevenueToday() { return revenueToday; }
    public void setRevenueToday(BigDecimal revenueToday) { this.revenueToday = revenueToday; }
    public BigDecimal getRevenueThisMonth() { return revenueThisMonth; }
    public void setRevenueThisMonth(BigDecimal revenueThisMonth) { this.revenueThisMonth = revenueThisMonth; }
    public BigDecimal getRevenueTotal() { return revenueTotal; }
    public void setRevenueTotal(BigDecimal revenueTotal) { this.revenueTotal = revenueTotal; }
    public BigDecimal getRevenueFromCompleted() { return revenueFromCompleted; }
    public void setRevenueFromCompleted(BigDecimal revenueFromCompleted) { this.revenueFromCompleted = revenueFromCompleted; }
    public List<RevenuePoint> getDailyRevenue() { return dailyRevenue; }
    public void setDailyRevenue(List<RevenuePoint> dailyRevenue) { this.dailyRevenue = dailyRevenue; }
    public List<RevenuePoint> getMonthlyRevenue() { return monthlyRevenue; }
    public void setMonthlyRevenue(List<RevenuePoint> monthlyRevenue) { this.monthlyRevenue = monthlyRevenue; }
    public List<VehicleRentalCount> getMostRented() { return mostRented; }
    public void setMostRented(List<VehicleRentalCount> mostRented) { this.mostRented = mostRented; }
    public long getPendingTransfers() { return pendingTransfers; }
    public void setPendingTransfers(long pendingTransfers) { this.pendingTransfers = pendingTransfers; }
    public long getOpenComplaints() { return openComplaints; }
    public void setOpenComplaints(long openComplaints) { this.openComplaints = openComplaints; }
    public long getUnassignedDrivers() { return unassignedDrivers; }
    public void setUnassignedDrivers(long unassignedDrivers) { this.unassignedDrivers = unassignedDrivers; }
    public long getLowReviews() { return lowReviews; }
    public void setLowReviews(long lowReviews) { this.lowReviews = lowReviews; }
}
