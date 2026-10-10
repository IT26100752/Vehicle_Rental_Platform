package com.vehiclerental.vehicle.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * Root URL. The new UI uses the vehicle catalogue (hero + search + fleet cards)
 * as its landing page, so "/" simply forwards there.
 */
@Controller
public class HomeController {

    @GetMapping("/")
    public String home() {
        return "redirect:/vehicles";
    }
}
