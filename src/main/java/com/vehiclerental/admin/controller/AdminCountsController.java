package com.vehiclerental.admin.controller;

import com.vehiclerental.admin.dto.DashboardStats;
import com.vehiclerental.admin.service.DashboardService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Small numbers for the badges on the admin sidebar (transfers to approve, open complaints ...).
 * Lives under /admin/** so only a signed-in administrator can call it.
 */
@Controller
public class AdminCountsController {

    private final DashboardService dashboard;

    public AdminCountsController(DashboardService dashboard) {
        this.dashboard = dashboard;
    }

    @GetMapping("/admin/api/counts")
    @ResponseBody
    public Map<String, Object> counts() {
        DashboardStats stats = dashboard.build();
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("pendingTransfers", stats.getPendingTransfers());
        out.put("openComplaints", stats.getOpenComplaints());
        out.put("unassignedDrivers", stats.getUnassignedDrivers());
        out.put("fineDue", stats.getFineDueBookings());
        return out;
    }
}
