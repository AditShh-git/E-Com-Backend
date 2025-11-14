package com.one.security;

/**
 * ========================================================================
 * LoggedUserContext
 * ========================================================================
 *
 * WHY THIS CLASS EXISTS:
 * ----------------------
 * This class is a simple "global request-scoped storage" for storing
 * the ID and ROLE of the currently logged-in user.
 *
 * After authentication (JWT login), the system extracts:
 *     - userId
 *     - userRole
 *
 * and stores them inside LoggedUserContext using ThreadLocal.
 *
 * Then ANY SERVICE or CONTROLLER in the application can access:
 *
 *     LoggedUserContext.getLoggedUserId();
 *     LoggedUserContext.getLoggedUserRole();
 *
 * without needing to pass userId manually in every method.
 *
 *
 * FUTURE GOAL:
 * ------------
 * Replace this ThreadLocal-based context with Spring Security’s
 * AuthenticationPrincipal (or SecurityContext), but this class allows
 * a clean transitional approach until a full migration is done.
 *
 *
 * BENEFITS:
 * ---------
 * 1. Centralized user access
 *      → Every service can fetch current user ID instantly.
 *
 * 2. Cleaner service methods
 *      → No need to pass userId or role everywhere manually.
 *
 * 3. Secure invoice access
 *      → InvoiceController checks user role from here.
 *
 * 4. Useful for multi-role systems
 *      → USER, SELLER, VENDOR, ADMIN flows work consistently.
 *
 * 5. Easy replacement
 *      → When switching to full Spring Security later, only
 *        this class needs to change. Other services remain untouched.
 *
 *
 * WHEN IT CLEARS AUTOMATICALLY:
 * -----------------------------
 * finishRequest() must be called at the end of every web request
 * (in a servlet filter), so ThreadLocal does not leak between users.
 *
 * ========================================================================
 */

public class LoggedUserContext {

    private static final ThreadLocal<Long> userId = new ThreadLocal<>();
    private static final ThreadLocal<String> role = new ThreadLocal<>();

    // --------------------------------------------------------------------
    // SETTERS
    // --------------------------------------------------------------------
    public static void setLoggedUserId(Long id) {
        userId.set(id);
    }

    public static void setLoggedUserRole(String r) {
        role.set(r);
    }

    // --------------------------------------------------------------------
    // GETTERS
    // --------------------------------------------------------------------
    public static Long getLoggedUserId() {
        return userId.get();
    }

    public static String getLoggedUserRole() {
        return role.get();
    }

    // --------------------------------------------------------------------
    // CLEAR (VERY IMPORTANT)
    // --------------------------------------------------------------------
    public static void clear() {
        userId.remove();
        role.remove();
    }
}