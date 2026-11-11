package com.vehiclerental.admin.controller;

import com.vehiclerental.admin.service.ActivityService;
import com.vehiclerental.common.Web;
import com.vehiclerental.config.Auth;
import com.vehiclerental.exception.BusinessRuleException;
import com.vehiclerental.promo.entity.PromoCode;
import com.vehiclerental.promo.service.PromoService;
import jakarta.servlet.http.HttpSession;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.time.LocalDate;

/** Admin: create, edit, switch on / off and delete promo codes. */
@Controller
public class AdminPromoController {

    private final PromoService promos;
    private final ActivityService activity;

    public AdminPromoController(PromoService promos, ActivityService activity) {
        this.promos = promos;
        this.activity = activity;
    }

    @GetMapping("/admin/promos")
    public String list(@RequestParam(name = "edit", required = false) Long edit, Model model) {
        model.addAttribute("promos", promos.findAll());
        model.addAttribute("today", LocalDate.now());
        PromoCode editing = null;
        if (edit != null) {
            try {
                editing = promos.findById(edit);
            } catch (BusinessRuleException ignored) {
                // an old link: just show the empty form
            }
        }
        model.addAttribute("editing", editing);
        return "admin/promos";
    }

    @PostMapping("/admin/promos")
    public String save(@RequestParam(required = false) Long id, @RequestParam String code,
                       @RequestParam(required = false) String description, @RequestParam int percentOff,
                       @RequestParam(required = false) BigDecimal maxDiscount,
                       @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate validFrom,
                       @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate validTo,
                       @RequestParam(defaultValue = "0") int usageLimit,
                       @RequestParam(defaultValue = "false") boolean premiumOnly,
                       @RequestParam(defaultValue = "false") boolean active,
                       HttpSession session, RedirectAttributes redirect) {
        try {
            PromoCode saved = promos.save(id, code, description, percentOff,
                    maxDiscount == null ? BigDecimal.ZERO : maxDiscount, validFrom, validTo, usageLimit, premiumOnly, active);
            activity.log(Auth.name(session), id == null ? "Created promo code" : "Updated promo code", saved.getCode());
            Web.success(redirect, "Promo code " + saved.getCode() + " saved.");
            return "redirect:/admin/promos";
        } catch (BusinessRuleException ex) {
            Web.error(redirect, ex.getMessage());
            return "redirect:/admin/promos" + (id == null ? "" : "?edit=" + id);
        }
    }

    @PostMapping("/admin/promos/{id}/toggle")
    public String toggle(@PathVariable Long id, @RequestParam boolean active, HttpSession session, RedirectAttributes redirect) {
        try {
            promos.setActive(id, active);
            activity.log(Auth.name(session), active ? "Activated promo code" : "Paused promo code", "#" + id);
            Web.success(redirect, active ? "Promo code activated." : "Promo code paused.");
        } catch (BusinessRuleException ex) {
            Web.error(redirect, ex.getMessage());
        }
        return "redirect:/admin/promos";
    }

    @PostMapping("/admin/promos/{id}/delete")
    public String delete(@PathVariable Long id, HttpSession session, RedirectAttributes redirect) {
        promos.delete(id);
        activity.log(Auth.name(session), "Deleted promo code", "#" + id);
        Web.success(redirect, "Promo code deleted.");
        return "redirect:/admin/promos";
    }
}
