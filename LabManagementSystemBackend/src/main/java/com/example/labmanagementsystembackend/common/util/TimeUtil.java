package com.example.labmanagementsystembackend.common.util;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;

public final class TimeUtil {
    private TimeUtil() {
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
