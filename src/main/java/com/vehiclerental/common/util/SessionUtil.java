package com.vehiclerental.common.util;

import com.vehiclerental.user.entity.User;

import jakarta.servlet.http.HttpSession;

/**
 * Tiny helper that hides HOW the logged-in user is kept between requests
 * (HttpSession attribute). Controllers never touch the session key string.
 */
public final class SessionUtil {

    public static final String SESSION_KEY = "loggedInUser";

    private SessionUtil() {
    }

    public static void login(HttpSession session, User user) {
        session.setAttribute(SESSION_KEY, user);
    }

    public static User currentUser(HttpSession session) {
        return session == null ? null : (User) session.getAttribute(SESSION_KEY);
    }

    public static void logout(HttpSession session) {
        session.invalidate();
    }
}
