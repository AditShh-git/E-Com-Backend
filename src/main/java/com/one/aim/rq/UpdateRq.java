package com.one.aim.rq;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UpdateRq {

    private String fullName;
    private String phoneNo;

    // Optional for password change
    private String oldPassword;
    private String newPassword;
    private String confirmPassword;

    private String email;
    private MultipartFile image;

    // ===========================================================
    // Helper: Check if user is trying to update password
    // ===========================================================
    public boolean hasPasswordUpdate() {
        return (oldPassword != null && !oldPassword.isBlank()) ||
                (newPassword != null && !newPassword.isBlank()) ||
                (confirmPassword != null && !confirmPassword.isBlank());
    }

    // ===========================================================
    // Helper: Validate password update fields
    // ===========================================================
    public boolean isPasswordDataValid() {

        // All 3 must be present
        if (oldPassword == null || oldPassword.isBlank() ||
                newPassword == null || newPassword.isBlank() ||
                confirmPassword == null || confirmPassword.isBlank()) {
            return false;
        }

        // New password length
        if (newPassword.length() < 6) {
            return false;
        }

        // New password & confirm must match
        if (!newPassword.equals(confirmPassword)) {
            return false;
        }

        // New password cannot be same as old password
        if (oldPassword.equals(newPassword)) {
            return false;
        }

        return true;
    }

    // ===========================================================
    // Helper: Provide specific password validation error message
    // ===========================================================
    public String passwordErrorMessage() {

        if (oldPassword == null || oldPassword.isBlank() ||
                newPassword == null || newPassword.isBlank() ||
                confirmPassword == null || confirmPassword.isBlank()) {
            return "All password fields (old, new, confirm) are required.";
        }

        if (newPassword.length() < 6) {
            return "New password must be at least 6 characters.";
        }

        if (oldPassword.equals(newPassword)) {
            return "New password cannot be same as old password.";
        }

        if (!newPassword.equals(confirmPassword)) {
            return "New password and confirm password do not match.";
        }

        return "Invalid password details.";
    }
}
