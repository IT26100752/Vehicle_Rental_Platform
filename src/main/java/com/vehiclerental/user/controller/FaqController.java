package com.vehiclerental.user.controller;

import com.vehiclerental.booking.service.BookingService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

/** The public FAQ page. The numbers in the answers come from application.properties, so they never go out of date. */
@Controller
public class FaqController {

    private final String driverFee;
    private final String oneWayFee;
    private final int verifyMinutes;
    private final String companyEmail;
    private final String companyPhone;

    public FaqController(@Value("${app.driver.fee-per-day:3500}") String driverFee,
                         @Value("${app.booking.one-way-fee:2500}") String oneWayFee,
                         @Value("${app.verify.code-minutes:15}") int verifyMinutes,
                         @Value("${app.company.email:}") String companyEmail,
                         @Value("${app.company.phone:}") String companyPhone) {
        this.driverFee = driverFee;
        this.oneWayFee = oneWayFee;
        this.verifyMinutes = verifyMinutes;
        this.companyEmail = companyEmail;
        this.companyPhone = companyPhone;
    }

    @GetMapping("/faq")
    public String faq(Model model) {
        model.addAttribute("driverFee", driverFee);
        model.addAttribute("oneWayFee", oneWayFee);
        model.addAttribute("verifyMinutes", verifyMinutes);
        model.addAttribute("maxRentalDays", BookingService.MAX_RENTAL_DAYS);
        model.addAttribute("companyEmail", companyEmail);
        model.addAttribute("companyPhone", companyPhone);
        return "faq";
    }
}
