package com.example.labmanagementsystembackend.common.util;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;

public final class TimeUtil {
    private static final ZoneId LOCAL_ZONE = ZoneId.of("Asia/Shanghai");

    private TimeUtil() {
    }

    /**
     * 将 UTC LocalDateTime 转换为本地时间（用于和实验室开放时间比较）
     */
    public static LocalTime toLocalTime(LocalDateTime utcTime) {
        if (utcTime == null) {
            return null;
        }
        return utcTime.atOffset(ZoneOffset.UTC)
                .atZoneSameInstant(LOCAL_ZONE)
                .toLocalTime();
    }

    public static LocalDateTime toUtcLocalDateTime(OffsetDateTime time) {
        if (time == null) {
            return null;
        }
        return time.withOffsetSameInstant(ZoneOffset.UTC).toLocalDateTime();
    }

    public static OffsetDateTime fromUtcLocalDateTime(LocalDateTime time) {
        if (time == null) {
            return null;
        }
        return time.atOffset(ZoneOffset.UTC);
    }
}
