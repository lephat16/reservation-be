package com.example.ReservationApp.util;

public class UserAgentParser {
    public static String parseDevice(String userAgent) {
        if (userAgent == null)
            return "Uknown";
        userAgent = userAgent.toLowerCase();
        if (userAgent.contains("iphone"))
            return "iPhone";
        if (userAgent.contains("ipad"))
            return "iPad";
        if (userAgent.contains("android"))
            return "Android Device";
        if (userAgent.contains("windows"))
            return "Windows PC";
        if (userAgent.contains("macintosh"))
            return "Mac";
        if (userAgent.contains("linux"))
            return "Linux PC";

        return "Other Device";
    }

    public static String parseBrowser(String userAgent) {
        if (userAgent == null)
            return "Unknown";
        userAgent = userAgent.toLowerCase();

        if (userAgent.contains("chrome") && !userAgent.contains("edg"))
            return "Chrome";
        if (userAgent.contains("firefox"))
            return "Firefox";
        if (userAgent.contains("safari") && !userAgent.contains("chrome"))
            return "Safari";
        if (userAgent.contains("edg"))
            return "Edge";
        if (userAgent.contains("msie") || userAgent.contains("trident"))
            return "Internet Explorer";

        return "Other Browser";
    }

    public static String parseOS(String userAgent) {
        if (userAgent == null)
            return "Unknown";
        userAgent = userAgent.toLowerCase();

        if (userAgent.contains("windows"))
            return "Windows";
        if (userAgent.contains("macintosh"))
            return "macOS";
        if (userAgent.contains("android"))
            return "Android";
        if (userAgent.contains("iphone") || userAgent.contains("ipad"))
            return "iOS";
        if (userAgent.contains("linux"))
            return "Linux";

        return "Other OS";
    }

    public static String formatDeviceInfo(String userAgent) {
        String device = parseDevice(userAgent);
        String browser = parseBrowser(userAgent);
        String os = parseOS(userAgent);

        return String.format("%s / %s / %s", device, os, browser);
    }
}
