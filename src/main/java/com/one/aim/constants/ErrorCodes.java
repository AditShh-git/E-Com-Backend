package com.one.aim.constants;

public class ErrorCodes {

    // ============================================================
    // CRUD OPERATIONS
    // ============================================================
    public static final String EC_CREATE_FAILED             = "EC_CREATE_FAILED";
    public static final String EC_SAVE_FAILED               = "EC_SAVE_FAILED";
    public static final String EC_UPDATE_FAILED             = "EC_UPDATE_FAILED";
    public static final String EC_DELETE_FAILED             = "EC_DELETE_FAILED";
    public static final String EC_RETRIEVE_FAILED           = "EC_RETRIEVE_FAILED";
    public static final String EC_ADD_FAILED                = "EC_ADD_FAILED";
    public static final String EC_COMMENT_ADD_FAILED        = "EC_COMMENT_ADD_FAILED";
    public static final String EC_MERGE_FAILED              = "EC_MERGE_FAILED";
    public static final String EC_NO_RECORDS_FOUND          = "EC_NO_RECORDS_FOUND";
    public static final String EC_RECORD_NOT_FOUND          = "EC_RECORD_NOT_FOUND";
    public static final String EC_ALREADY_UPTO_DATE         = "EC_ALREADY_UPTO_DATE";

    // ============================================================
    // STATUS / WORKFLOW
    // ============================================================
    public static final String EC_SUBMISSION_FAILED         = "EC_SUBMISSION_FAILED";
    public static final String EC_COMPLETION_FAILED         = "EC_COMPLETION_FAILED";
    public static final String EC_VALIDATION_FAILED         = "EC_VALIDATION_FAILED";
    public static final String EC_DISCARD_FAILED            = "EC_DISCARD_FAILED";
    public static final String EC_TRANSFER_FAILED           = "EC_TRANSFER_FAILED";
    public static final String EC_CANCELLATION_FAILED       = "EC_CANCELLATION_FAILED";
    public static final String EC_STATUS_UPDATE_FAILED      = "EC_STATUS_UPDATE_FAILED";
    public static final String EC_STATUS_UNKNOWN            = "EC_STATUS_UNKNOWN";
    public static final String EC_LOCK_FAILED               = "EC_LOCK_FAILED";
    public static final String EC_UNLOCK_FAILED             = "EC_UNLOCK_FAILED";
    public static final String EC_USER_INACTIVE             = "EC_USER_INACTIVE";

    // ============================================================
    // AUTHENTICATION & AUTHORIZATION

    // AUTH / LOGIN ERRORS
    // ===========================================================
    public static final String EC_INVALID_CREDENTIALS = "EC_INVALID_CREDENTIALS"; // 👈 added
    public static final String EC_ACCOUNT_NOT_VERIFIED = "EC_ACCOUNT_NOT_VERIFIED"; // 👈 added
    public static final String EC_ACCOUNT_LOCKED = "EC_ACCOUNT_LOCKED"; // optional
    public static final String EC_TOKEN_EXPIRED = "EC_TOKEN_EXPIRED";
    public static final String EC_INVALID_TOKEN = "EC_INVALID_TOKEN";
    public static final String EC_INVALID_USERNAME          = "EC_INVALID_USERNAME";
    public static final String EC_INVALID_PASSWORD          = "EC_INVALID_PASSWORD";
    public static final String EC_INVALID_USERNAME_PASSWORD = "EC_INVALID_USERNAME_PASSWORD";
    public static final String EC_INCORRECT_USERNAME_PASSWORD = "EC_INCORRECT_USERNAME_PASSWORD";
    public static final String EC_LOGIN_FAILED              = "EC_LOGIN_FAILED";
    public static final String EC_LOGOUT_FAILED             = "EC_LOGOUT_FAILED";
    public static final String EC_REGISTRATION_FAILED       = "EC_REGISTRATION_FAILED";
    public static final String EC_PASSWORD_RESET_FAILED     = "EC_PASSWORD_RESET_FAILED";
    public static final String EC_EMAIL_VERIFICATION_FAILED = "EC_EMAIL_VERIFICATION_FAILED";
    public static final String EC_EMAIL_ALREADY_VERIFIED    = "EC_EMAIL_ALREADY_VERIFIED";
    public static final String EC_VERIFICATION_LINK_EXPIRED = "EC_VERIFICATION_LINK_EXPIRED";
    public static final String EC_RESET_LINK_EXPIRED        = "EC_RESET_LINK_EXPIRED";
    public static final String EC_UNAUTHORIZED_ACCESS       = "EC_UNAUTHORIZED_ACCESS";
    public static final String EC_ACCESS_DENIED             = "EC_ACCESS_DENIED";
    public static final String EC_ACTION_NOT_ALLOWED        = "EC_ACTION_NOT_ALLOWED";
    public static final String EC_INACTIVE_ACCOUNT          = "EC_INACTIVE_ACCOUNT";
    public static final String EC_USER_NOT_FOUND            = "EC_USER_NOT_FOUND";
    public static final String EC_USER_ALREADY_EXIST        = "EC_USER_ALREADY_EXIST";
    public static final String EC_AUTHORIZATION_NOT_FOUND   = "EC_AUTHORIZATION_NOT_FOUND";
    public static final String EC_USERROLE_NOT_FOUND        = "EC_USERROLE_NOT_FOUND";
    public static final String EC_INVALID_USERROLE          = "EC_INVALID_USERROLE";
    public static final String EC_REQUIRED_AUTHORIZATION_ID = "EC_REQUIRED_AUTHORIZATION_ID";

    // ============================================================
    // EMAIL ERRORS
    // ============================================================
    public static final String EC_EMAIL_SEND_FAILED         = "EC_EMAIL_SEND_FAILED";
    public static final String EC_INVALID_EMAIL             = "EC_INVALID_EMAIL";
    public static final String EC_REQUIRED_EMAIL            = "EC_REQUIRED_EMAIL";
    public static final String EC_REQUIRED_USERNAME_EMAIL   = "EC_REQUIRED_USERNAME_EMAIL";

    // ============================================================
    // FILE OPERATIONS
    // ============================================================
    public static final String EC_FILEUPLOAD_FAILED         = "EC_FILEUPLOAD_FAILED";
    public static final String EC_FILE_NOT_FOUND            = "EC_FILE_NOT_FOUND";
    public static final String EC_ALLOWED_FILE_TYPES        = "EC_ALLOWED_FILE_TYPES";
    public static final String EC_DOCUMENT_ALREADY_EXISTS   = "EC_DOCUMENT_ALREADY_EXISTED";
    public static final String EC_CLONE_FAILED              = "EC_CLONE_FAILED";

    // ============================================================
    // VALIDATION & INPUT ERRORS
    // ============================================================
    public static final String EC_INVALID_INPUT             = "EC_INVALID_INPUT";
    public static final String EC_INVALID_OBJTYPE           = "EC_INVALID_OBJTYPE";
    public static final String EC_INVALID_DOCTYPE           = "EC_INVALID_DOCTYPE";

    // ============================================================
    // REQUIRED FIELD ERRORS
    // ============================================================
    public static final String EC_REQUIRED_USERNAME         = "EC_REQUIRED_USERNAME";
    public static final String EC_REQUIRED_PASSWORD         = "EC_REQUIRED_PASSWORD";
    public static final String EC_REQUIRED_USERID           = "EC_REQUIRED_USERID";
    public static final String EC_REQUIRED_USERROLE         = "EC_REQUIRED_USERROLE";
    public static final String EC_REQUIRED_USERROLE_OBJDOCID = "EC_REQUIRED_USERROLE_OR_OBJDOCID";
    public static final String EC_REQUIRED_DOCTYPE          = "EC_REQUIRED_DOCTYPE";
    public static final String EC_REQUIRED_DOCID            = "EC_REQUIRED_DOCID";
    public static final String EC_REQUIRED_OBJCODE          = "EC_REQUIRED_OBJCODE";
    public static final String EC_REQUIRED_OBJTYPE          = "EC_REQUIRED_OBJTYPE";
    public static final String EC_REQUIRED_DATE             = "EC_REQUIRED_DATE";
    public static final String EC_REQUIRED_TIMEZONE         = "EC_REQUIRED_TIMEZONE";
    public static final String EC_REQUIRED_PHONENO          = "EC_REQUIRED_PHONENO";
    public static final String EC_REQUIRED_PHONE            = "EC_REQUIRED_PHONE";
    public static final String EC_REQUIRED_SERVICE          = "EC_REQUIRED_SERVICE";
    public static final String EC_REQUIRED_MESSAGE          = "EC_REQUIRED_MESSAGE";
    public static final String EC_REQUIRED_FULL_ADDRESS     = "EC_REQUIRED_FULL_ADDRESS";
    public static final String EC_REQUIRED_STREET           = "EC_REQUIRED_STREET";
    public static final String EC_REQUIRED_CITY             = "EC_REQUIRED_CITY";
    public static final String EC_REQUIRED_STATE            = "EC_REQUIRED_STATE";
    public static final String EC_REQUIRED_COUNTRY          = "EC_REQUIRED_COUNTRY";
    public static final String EC_REQUIRED_ZIP              = "EC_REQUIRED_ZIP";

    // ============================================================
    // DATA / RECORD NOT FOUND
    // ============================================================
    public static final String EC_NOT_FOUND                 = "EC_NOT_FOUND";
    public static final String EC_VENDOR_NOT_FOUND          = "EC_VENDOR_NOT_FOUND";
    public static final String EC_ADMIN_NOT_FOUND           = "EC_ADMIN_NOT_FOUND";
    public static final String EC_SELLER_NOT_FOUND          = "EC_SELLER_NOT_FOUND";
    public static final String EC_CART_NOT_FOUND            = "EC_CART_NOT_FOUND";
    public static final String EC_ORDER_NOT_FOUND           = "EC_ORDER_NOT_FOUND";

    // ===========================================================
    // ✅ Seller-Specific Validation Errors
    // ===========================================================

    // Full Name
    public static final String EC_REQUIRED_FULLNAME = "EC_REQUIRED_FULLNAME";

    // Phone
    public static final String EC_INVALID_PHONE = "EC_INVALID_PHONE";

    // PAN
    public static final String EC_INVALID_PAN = "EC_INVALID_PAN";

    // Aadhaar
    public static final String EC_INVALID_AADHAAR = "EC_INVALID_AADHAAR";

    // GST
    public static final String EC_INVALID_GST = "EC_INVALID_GST";

    // Product-related
    public static final String EC_PRODUCT_NOT_FOUND = "EC_PRODUCT_NOT_FOUND";

    // Unauthorized actions
    public static final String EC_UNAUTHORIZED = "EC_UNAUTHORIZED";



    // ============================================================
    // SYSTEM & INTERNAL ERRORS
    // ============================================================
    public static final String EC_INTERNAL_ERROR            = "EC_INTERNAL_ERROR";
    public static final String EC_UNKNOWN_ERROR             = "EC_UNKNOWN_ERROR";

}
