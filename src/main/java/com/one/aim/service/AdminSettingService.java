package com.one.aim.service;

import com.one.aim.bo.AdminSettingsBO;
import com.one.aim.bo.SellerBO;
import com.one.aim.rs.SellerRs;

import java.util.List;
import java.util.Map;

public interface AdminSettingService {

    String get(String key);
    AdminSettingsBO save(String key, String value);
    Map<String, String> getAll();
    void initDefaultSettings();

    List<SellerRs> getUnverifiedSellers();
    List<SellerRs> getVerifiedSellers();
    List<SellerRs> getRejectedSellers();

    String verifySeller(String idOrCode, Boolean status);
    byte[] getSellerDocumentsZip(String sellerId);

    int getGlobalDiscount();
    boolean isDiscountEngineEnabled();

    double getDefaultTaxPercent();
    double getDeliveryChargeDefault();
    int getDefaultReturnPolicyDays();

    // For number based settings
    double getDoubleValue(String key, double defaultValue);
    long getLongValue(String key, long defaultValue);


    boolean getBooleanValue(String key, boolean defaultValue);
}

