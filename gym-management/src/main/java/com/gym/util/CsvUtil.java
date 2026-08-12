package com.gym.util;

import java.util.ArrayList;
import java.util.List;

/**
 * Minimal CSV encode/decode helper that supports quoted fields containing
 * commas, quotes, or newlines. Good enough for this application's simple
 * flat-file persistence needs (no external dependency required).
 */
public final class CsvUtil {

    private CsvUtil() {
    }

    public static String escape(String value) {
        if (value == null) {
            return "";
        }
        boolean needsQuoting = value.contains(",") || value.contains("\"") || value.contains("\n");
        String escaped = value.replace("\"", "\"\"");
        return needsQuoting ? "\"" + escaped + "\"" : escaped;
    }

    public static String join(List<String> fields) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < fields.size(); i++) {
            if (i > 0) {
                sb.append(",");
            }
            sb.append(escape(fields.get(i)));
        }
        return sb.toString();
    }

    public static List<String> parseLine(String line) {
        List<String> fields = new ArrayList<>();
        StringBuilder current = new StringBuilder();
        boolean inQuotes = false;
        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);
            if (inQuotes) {
                if (c == '"') {
                    if (i + 1 < line.length() && line.charAt(i + 1) == '"') {
                        current.append('"');
                        i++;
                    } else {
                        inQuotes = false;
                    }
                } else {
                    current.append(c);
                }
            } else {
                if (c == '"') {
                    inQuotes = true;
                } else if (c == ',') {
                    fields.add(current.toString());
                    current.setLength(0);
                } else {
                    current.append(c);
                }
            }
        }
        fields.add(current.toString());
        return fields;
    }

    /** Returns "" instead of null for optional/empty cells. */
    public static String nullToEmpty(String s) {
        return s == null ? "" : s;
    }

    /** Returns null if the string is empty, otherwise the string itself. */
    public static String emptyToNull(String s) {
        return (s == null || s.isEmpty()) ? null : s;
    }
}
