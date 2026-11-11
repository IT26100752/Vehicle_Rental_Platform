package com.vehiclerental.notification;

import com.vehiclerental.common.Web;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Comparator;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/** Admin "Email log": every email the system sent, with its status, and a retry button for failed ones. */
@Controller
public class AdminEmailController {

    private final EmailLogRepository logs;
    private final EmailDispatcher dispatcher;

    public AdminEmailController(EmailLogRepository logs, EmailDispatcher dispatcher) {
        this.logs = logs;
        this.dispatcher = dispatcher;
    }

    @GetMapping("/admin/emails")
    public String list(@RequestParam(name = "status", required = false) EmailStatus status,
                       @RequestParam(name = "q", required = false) String q, Model model) {
        String k = q == null ? "" : q.trim().toLowerCase();
        List<EmailLog> all = logs.findAll().stream()
                .sorted(Comparator.comparing(EmailLog::getCreatedAt).reversed()).toList();
        Map<EmailStatus, Long> counts = new EnumMap<>(EmailStatus.class);
        for (EmailStatus s : EmailStatus.values()) {
            counts.put(s, 0L);
        }
        for (EmailLog l : all) {
            counts.merge(l.getStatus(), 1L, Long::sum);
        }
        List<EmailLog> shown = all.stream()
                .filter(l -> status == null || l.getStatus() == status)
                .filter(l -> k.isEmpty()
                        || (l.getToAddress() != null && l.getToAddress().toLowerCase().contains(k))
                        || (l.getSubject() != null && l.getSubject().toLowerCase().contains(k)))
                .limit(300).toList();
        model.addAttribute("logs", shown);
        model.addAttribute("counts", counts);
        model.addAttribute("total", all.size());
        model.addAttribute("statuses", EmailStatus.values());
        model.addAttribute("status", status);
        model.addAttribute("q", q);
        model.addAttribute("realMail", dispatcher.isDeliveringRealMail());
        return "admin/emails";
    }

    @PostMapping("/admin/emails/{id}/retry")
    public String retry(@PathVariable Long id, RedirectAttributes redirect) {
        EmailStatus result = dispatcher.resend(id);
        if (result == EmailStatus.FAILED) {
            Web.error(redirect, "The email could not be sent again. Check the mail settings in application.properties.");
        } else {
            Web.success(redirect, "Email sent again (" + result.getLabel().toLowerCase() + ").");
        }
        return "redirect:/admin/emails";
    }
}
