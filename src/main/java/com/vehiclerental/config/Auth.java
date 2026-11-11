package com.vehiclerental.config;

import com.vehiclerental.user.entity.Person;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

/** Tiny helper around the HTTP session: who is signed in and with which role. */
public final class Auth {

    public static final String USER_ID = "authUserId";
    public static final String ROLE = "authRole";
    public static final String NAME = "authName";
    public static final String LEVEL = "authLevel";
    public static final String SAVED_URL = "authSavedUrl";

    private Auth() {
    }

    public static void signIn(HttpServletRequest request, Person person) {
        request.getSession(true);
        request.changeSessionId();                       // protects against session fixation
        HttpSession session = request.getSession();
        session.setAttribute(USER_ID, person.getId());
        session.setAttribute(ROLE, person.getRole());
        session.setAttribute(NAME, person.getFullName());
        if (person instanceof com.vehiclerental.admin.entity.Admin admin) {
            session.setAttribute(LEVEL, admin.getLevel().name());
        }
    }

    public static void signOut(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session != null) {
            session.invalidate();
        }
    }

    public static Long userId(HttpSession session) {
        return (Long) session.getAttribute(USER_ID);
    }

    public static String name(HttpSession session) {
        return (String) session.getAttribute(NAME);
    }

    public static boolean isAdmin(HttpSession session) {
        return session != null && "ADMIN".equals(session.getAttribute(ROLE));
    }

    /** A manager admin may add and delete other administrators. */
    public static boolean isManager(HttpSession session) {
        return isAdmin(session) && "MANAGER".equals(session.getAttribute(LEVEL));
    }

    public static boolean isCustomer(HttpSession session) {
        return session != null && "CUSTOMER".equals(session.getAttribute(ROLE));
    }
}
