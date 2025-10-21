package com.ecole._2.utils;

import com.ecole._2.models.User;

public class CheckingUtils {

    public static String determineUserKind(User user) {
        if (user == null || user.getKind() == null) {
            return "unknown";
        }
        if ("admin".equalsIgnoreCase(user.getKind())) {
            return "admin";
        } else if ("student".equalsIgnoreCase(user.getKind())) {
            return "student";
        } else {
            return "unknown";
        }
    }
}