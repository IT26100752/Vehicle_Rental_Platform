package user.controller;

import com.vehiclerental.booking.service.BookingService;
import com.vehiclerental.common.util.SessionUtil;
import com.vehiclerental.review.service.ReviewService;
import com.vehiclerental.user.dto.PasswordChangeRequest;
import com.vehiclerental.user.dto.ProfileUpdateRequest;
import com.vehiclerental.user.entity.CustomerUser;
import com.vehiclerental.user.entity.User;
import com.vehiclerental.user.service.UserService;

import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.stream.Collectors;

/**
 * MEMBER 1 - the customer area: dashboard, profile, password.
 */
@Controller
public class UserController {

    private final UserService userService;
    private final BookingService bookingService;
    private final ReviewService reviewService;

    public UserController(UserService userService, BookingService bookingService,
                          ReviewService reviewService) {
        this.userService = userService;
        this.bookingService = bookingService;
        this.reviewService = reviewService;
    }

    /* --------------------------- dashboard -------------------------- */
    @GetMapping("/dashboard")
    public String dashboard(HttpSession session, Model model) {
        User user = SessionUtil.currentUser(session);
        var bookings = bookingService.bookingsOf(user.getId());
        model.addAttribute("bookings", bookings);
        model.addAttribute("activeCount", bookings.stream().filter(b -> b.isActive()).count());
        model.addAttribute("completedCount", bookings.size() - bookings.stream()
                .filter(b -> b.isActive()).count());
        model.addAttribute("reviewCount", reviewService.byUser(user.getId()).size());
        return "user/dashboard";
    }

    /* ---------------------------- profile --------------------------- */
    @GetMapping("/profile")
    public String profile(HttpSession session, Model model) {
        model.addAttribute("user", SessionUtil.currentUser(session));
        return "user/profile";
    }

    @PostMapping("/profile/update")
    public String updateProfile(@ModelAttribute ProfileUpdateRequest form,
                                HttpSession session,
                                RedirectAttributes redirect) {
        User user = SessionUtil.currentUser(session);
        user.setFullName(form.getFullName());
        user.setEmail(form.getEmail());
        user.setPhone(form.getPhone());
        if (user instanceof CustomerUser c) {
            c.setNic(form.getNic());
            c.setAddress(form.getAddress());
        }
        userService.update(user);
        session.setAttribute(SessionUtil.SESSION_KEY, user);   // refresh the session copy
        redirect.addFlashAttribute("success", "Profile updated");
        return "redirect:/profile";
    }

    /* --------------------------- password --------------------------- */
    @GetMapping("/password")
    public String passwordPage() {
        return "user/password";
    }

    @PostMapping("/password/change")
    public String changePassword(@ModelAttribute PasswordChangeRequest form,
                                 HttpSession session,
                                 RedirectAttributes redirect) {
        User user = SessionUtil.currentUser(session);
        boolean ok = userService.changePassword(user.getId(), form.getCurrentPassword(), form.getNewPassword());
        if (ok) {
            redirect.addFlashAttribute("success", "Password changed");
        } else {
            redirect.addFlashAttribute("error", "Current password is wrong");
        }
        return "redirect:/password";
    }
}
