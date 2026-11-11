package com.vehiclerental.driver.controller;

import com.vehiclerental.common.Web;
import com.vehiclerental.driver.entity.DriverAssignment;
import com.vehiclerental.driver.service.DriverService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * The page a driver opens from the email link. The long random token in the address is the only "login":
 * it shows one trip and lets the driver confirm it. No other data is reachable.
 */
@Controller
public class DriverTripController {

    private final DriverService drivers;

    public DriverTripController(DriverService drivers) {
        this.drivers = drivers;
    }

    @GetMapping("/driver/trip/{token}")
    public String trip(@PathVariable String token, Model model) {
        DriverAssignment a = drivers.findByToken(token);
        model.addAttribute("assignment", a);
        model.addAttribute("driver", drivers.driverOf(a));
        model.addAttribute("booking", drivers.bookingOf(a));
        return "driver/trip";
    }

    @PostMapping("/driver/trip/{token}/acknowledge")
    public String acknowledge(@PathVariable String token, RedirectAttributes redirect) {
        drivers.acknowledge(token);
        Web.success(redirect, "Thank you. The office has been told you have seen this trip.");
        return "redirect:/driver/trip/" + token;
    }
}
