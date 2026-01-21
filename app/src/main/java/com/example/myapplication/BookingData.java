package com.example.myapplication;

import java.util.HashMap;
import java.util.Map;

public class BookingData {

    // Factory method for Hotel booking
    public static Map<String, Object> forHotel(String checkInDate, String checkOutDate,
            String roomType, String guestName) {
        Map<String, Object> data = new HashMap<>();
        data.put("checkInDate", checkInDate);
        data.put("checkOutDate", checkOutDate);
        data.put("roomType", roomType);
        data.put("guestName", guestName);
        return data;
    }

    // Factory method for Wisata booking
    public static Map<String, Object> forWisata(String visitDate, int ticketQuantity,
            String visitorName) {
        Map<String, Object> data = new HashMap<>();
        data.put("visitDate", visitDate);
        data.put("ticketQuantity", ticketQuantity);
        data.put("visitorName", visitorName);
        return data;
    }

    // Factory method for Aksesoris order
    public static Map<String, Object> forAksesoris(String shippingAddress, String recipientName,
            String phoneNumber) {
        Map<String, Object> data = new HashMap<>();
        data.put("shippingAddress", shippingAddress);
        data.put("recipientName", recipientName);
        data.put("phoneNumber", phoneNumber);
        return data;
    }

    // Factory method for Kuliner order
    public static Map<String, Object> forKuliner(String deliveryAddress, String deliveryTime,
            String customerName) {
        Map<String, Object> data = new HashMap<>();
        data.put("deliveryAddress", deliveryAddress);
        data.put("deliveryTime", deliveryTime);
        data.put("customerName", customerName);
        return data;
    }

    // Generic method to get value from booking data
    public static String getString(Map<String, Object> bookingData, String key) {
        if (bookingData == null || !bookingData.containsKey(key)) {
            return "";
        }
        Object value = bookingData.get(key);
        return value != null ? value.toString() : "";
    }

    public static int getInt(Map<String, Object> bookingData, String key) {
        if (bookingData == null || !bookingData.containsKey(key)) {
            return 0;
        }
        Object value = bookingData.get(key);
        if (value instanceof Number) {
            return ((Number) value).intValue();
        }
        try {
            return Integer.parseInt(value.toString());
        } catch (NumberFormatException e) {
            return 0;
        }
    }
}
