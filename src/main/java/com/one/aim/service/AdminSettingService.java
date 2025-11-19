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

    String verifySeller(Long sellerId, Boolean status);

    List<SellerRs> getUnverifiedSellers();
    List<SellerRs> getVerifiedSellers();


    int getGlobalDiscount();
    boolean isDiscountEngineEnabled();


}
