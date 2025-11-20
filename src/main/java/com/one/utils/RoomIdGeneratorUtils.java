package com.one.utils;

public class RoomIdGeneratorUtils {

    public static String adminSellerRoom(Long sellerId) {
        return "ADMIN_SELLER_" + sellerId;
    }

    public static String adminDeliveryRoom(Long deliveryId) {
        return "ADMIN_DELIVERY_" + deliveryId;
    }
}
