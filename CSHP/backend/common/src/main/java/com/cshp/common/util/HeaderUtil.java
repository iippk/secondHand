package com.cshp.common.util;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

/**
 * HTTP 请求头工具类
 * 用于处理包含非 ISO-8859-1 字符的请求头（如中文字符）
 */
public class HeaderUtil {

    /**
     * 解码请求头值
     * 如果值以 "base64:" 开头，则进行 Base64 解码
     * 否则直接返回原值
     *
     * @param headerValue 请求头值
     * @return 解码后的字符串
     */
    public static String decodeHeaderValue(String headerValue) {
        if (headerValue == null || headerValue.isEmpty()) {
            return headerValue;
        }

        // 检查是否包含 base64: 前缀
        if (headerValue.startsWith("base64:")) {
            try {
                String base64Value = headerValue.substring(7); // 移除 "base64:" 前缀
                byte[] decodedBytes = Base64.getDecoder().decode(base64Value);
                // 使用 UTF-8 解码
                return new String(decodedBytes, StandardCharsets.UTF_8);
            } catch (Exception e) {
                // 如果解码失败，返回原值
                System.err.println("Base64 解码失败: " + e.getMessage());
                return headerValue;
            }
        }

        // 没有 base64: 前缀，直接返回
        return headerValue;
    }

    /**
     * 检查字符串是否包含非 ISO-8859-1 字符
     *
     * @param str 待检查的字符串
     * @return 如果包含非 ISO-8859-1 字符返回 true，否则返回 false
     */
    public static boolean containsNonISO88591(String str) {
        if (str == null) {
            return false;
        }
        for (int i = 0; i < str.length(); i++) {
            char c = str.charAt(i);
            // ISO-8859-1 范围是 0x00-0xFF
            if (c > 255) {
                return true;
            }
        }
        return false;
    }
}

