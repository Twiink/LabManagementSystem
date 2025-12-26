package com.example.labmanagementsystembackend.common.util;

public final class PageUtil {
    private PageUtil() {
    }

    public static int offset(int page, int pageSize) {
        int safePage = Math.max(page, 1);
        int safeSize = Math.min(Math.max(pageSize, 1), 200);
        return (safePage - 1) * safeSize;
    }

    public static int normalizePage(int page) {
        return Math.max(page, 1);
    }

    public static int normalizePageSize(int pageSize) {
        return Math.min(Math.max(pageSize, 1), 200);
    }
}
