package ru.practicum;

import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

public class CallbackParser {

    public static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ISO_DATE_TIME;
    public static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE;

    // Метод для парсинга callbackData
    public static Map<String, String> parse(String callbackData) {
        Map<String, String> result = new HashMap<>();

        if (callbackData.startsWith("date_")) {
            String[] parts = callbackData.replace("date_", "").split("_", 2); // ["123", "2025-06-15"]
            if (parts.length >= 2) {
                result.put("type", "date");
                result.put("doctorId", parts[0]);
                result.put("date", parts[1]);
            }

        } else if (callbackData.startsWith("time_")) {
            String[] parts = callbackData.replace("time_", "").split("_", 2); // ["123", "2025-06-15T10:00"]
            if (parts.length >= 2) {
                result.put("type", "time");
                result.put("doctorId", parts[0]);
                result.put("time", parts[1]);
            }

        } else if (callbackData.startsWith("doctor_")) {
            String idStr = callbackData.replace("doctor_", "");
            result.put("type", "doctor");
            result.put("doctorId", idStr);

        } else if (callbackData.startsWith("specialization_")) {
            String code = callbackData.replace("specialization_", "");
            result.put("type", "specialization");
            result.put("code", code);

        } else if (callbackData.startsWith("confirm_")) {
            String[] parts = callbackData.replace("confirm_", "").split("_", 2);
            if (parts.length >= 2) {
                result.put("type", "confirm");
                result.put("doctorId", parts[0]);
                result.put("time", parts[1]);
            }

        } else if (callbackData.startsWith("cancel_")) {
            String idStr = callbackData.replace("cancel_", "");
            result.put("type", "cancel");
            result.put("appointmentId", idStr);
        }

        return result;
    }
}