package com.winlator.core;

import java.util.Iterator;

public class KeyValueSet implements Iterable<String[]> {
    private String data;

    public KeyValueSet() {
        this.data = "";
    }

    public KeyValueSet(Object data) {
        this(data != null ? data.toString() : null);
    }

    public KeyValueSet(String data) {
        String str = "";
        this.data = (data == null || data.isEmpty()) ? str : data;
    }

    private int[] indexOfKey(String key) {
        int start = 0;
        int end = data.indexOf(",");
        if (end == -1) end = data.length();

        while (start < end) {
            int eqIndex = data.indexOf("=", start);
            if (eqIndex != -1 && data.substring(start, eqIndex).equals(key)) {
                return new int[]{start, end};
            }
            start = end + 1;
            end = data.indexOf(",", start);
            if (end == -1) end = data.length();
        }
        return null;
    }

    public String get(String key) {
        return get(key, "");
    }

    public String get(String key, String fallback) {
        if (data.isEmpty()) return fallback;
        for (String[] keyValue : this) {
            if (keyValue[0].equals(key)) return keyValue[1];
        }
        return fallback;
    }

    public float getFloat(String key, float fallback) {
        try {
            String value = get(key);
            return !value.isEmpty() ? Float.parseFloat(value) : fallback;
        } catch (NumberFormatException e) {
            return fallback;
        }
    }

    public int getInt(String key, int fallback) {
        try {
            String value = get(key);
            return !value.isEmpty() ? Integer.parseInt(value) : fallback;
        } catch (NumberFormatException e) {
            return fallback;
        }
    }

    public String getHexString(String key, int fallback) {
        int result;
        try {
            String value = get(key);
            result = !value.isEmpty() ? Integer.parseInt(value) : fallback;
        } catch (NumberFormatException e) {
            result = fallback;
        }
        return String.format("0x%08x", result);
    }

    public boolean getBoolean(String key) {
        return getBoolean(key, false);
    }

    public boolean getBoolean(String key, boolean fallback) {
        String value = get(key);
        if (value.isEmpty()) return fallback;
        return value.equals("1") || value.equalsIgnoreCase("true") || value.equals("t");
    }

    public KeyValueSet put(String key, Object value) {
        int[] range = indexOfKey(key);
        String str = "=";
        StringBuilder newEntry = new StringBuilder();
        newEntry.append(key).append(str).append(value);

        if (range != null) {
            String str2 = data;
            int i = range[0];
            int i2 = range[1];
            this.data = str2.substring(0, i) + newEntry.toString() + str2.substring(i2);
        } else {
            if (!data.isEmpty()) {
                this.data += ",";
            }
            this.data += newEntry.toString();
        }
        return this;
    }

    @Override
    public Iterator<String[]> iterator() {
        final int[] start = {0};
        final int[] end = {data.indexOf(",") != -1 ? data.indexOf(",") : data.length()};
        final String[] item = new String[2];

        return new Iterator<String[]>() {
            @Override
            public boolean hasNext() {
                return start[0] < data.length();
            }

            @Override
            public String[] next() {
                int eqIndex = data.indexOf("=", start[0]);
                item[0] = data.substring(start[0], eqIndex);
                item[1] = data.substring(eqIndex + 1, end[0]);
                start[0] = end[0] + 1;
                end[0] = data.indexOf(",", start[0]);
                if (end[0] == -1) end[0] = data.length();
                return item;
            }
        };
    }

    public String toString() {
        return data;
    }

    public boolean isEmpty() {
        return data.isEmpty();
    }
}