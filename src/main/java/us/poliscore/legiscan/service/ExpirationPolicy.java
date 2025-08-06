package us.poliscore.legiscan.service;

import java.time.DayOfWeek;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Objects;
import java.util.function.BiFunction;

import us.poliscore.legiscan.view.LegiscanMasterListView;

/**
 * Determines expiration policies for Legiscan cache entries.
 */
public abstract class ExpirationPolicy {

    /**
     * Returns the duration until this cache entry should expire,
     * or null for "never expire".
     *
     * @param createdAt The time the object was cached (UTC).
     * @param cacheKey  The cache key (may be used for advanced logic).
     * @return The duration until expiration, or null for "never expire".
     */
    public abstract Duration getTtl(Instant createdAt, String cacheKey);

    /**
     * Returns TTL in seconds (or -1 for never expire), given now and createdAt.
     */
    public final long computeTtlSecs(Instant now, Instant createdAt, String cacheKey) {
        Duration duration = getTtl(createdAt, cacheKey);
        if (duration == null) return -1;
        long ttl = duration.getSeconds() - Duration.between(createdAt, now).getSeconds();
        return ttl > 0 ? ttl : 0;
    }

    /** Static policy: never expires (static files). */
    public static ExpirationPolicy never() {
        return new ExpirationPolicy() {
            @Override
            public Duration getTtl(Instant createdAt, String cacheKey) {
                return null;
            }
        };
    }

    /** Static policy: expires after the given java.time.Duration. */
    public static ExpirationPolicy fixedDuration(Duration duration) {
        Objects.requireNonNull(duration, "Duration cannot be null");
        return new ExpirationPolicy() {
            @Override
            public Duration getTtl(Instant createdAt, String cacheKey) {
                return duration;
            }
        };
    }

    /** Static policy: expires after the given seconds. */
    public static ExpirationPolicy fixedDuration(long ttlSecs) {
        return fixedDuration(Duration.ofSeconds(ttlSecs));
    }

    /**
     * Expires at the next Sunday at 7AM Eastern (America/New_York) after createdAt.
     * Example: use for requests that should expire weekly.
     */
    public static ExpirationPolicy weekly() {
        return new ExpirationPolicy() {
            private final ZoneId zone = ZoneId.of("America/New_York");

            @Override
            public Duration getTtl(Instant createdAt, String cacheKey) {
                ZonedDateTime zdt = createdAt.atZone(zone);
                int daysUntilSunday = DayOfWeek.SUNDAY.getValue() - zdt.getDayOfWeek().getValue();
                if (daysUntilSunday < 0) daysUntilSunday += 7;
                ZonedDateTime nextSunday = zdt.plusDays(daysUntilSunday).withHour(7).withMinute(0).withSecond(0).withNano(0);
                if (!nextSunday.isAfter(zdt)) {
                    nextSunday = nextSunday.plusWeeks(1);
                }
                return Duration.between(zdt, nextSunday);
            }
        };
    }

    /**
     * Expires the next day at 7AM Eastern (America/New_York) after createdAt.
     * Example: use for requests that should expire daily.
     */
    public static ExpirationPolicy daily() {
        return new ExpirationPolicy() {
            private final ZoneId zone = ZoneId.of("America/New_York");

            @Override
            public Duration getTtl(Instant createdAt, String cacheKey) {
                ZonedDateTime zdt = createdAt.atZone(zone);
                ZonedDateTime next7am = zdt.withHour(7).withMinute(0).withSecond(0).withNano(0);
                if (!next7am.isAfter(zdt)) {
                    next7am = next7am.plusDays(1);
                }
                return Duration.between(zdt, next7am);
            }
        };
    }

    public static ExpirationPolicy hourly() {
        return fixedDuration(Duration.ofHours(1));
    }

    /** Allows custom logic, for full control. */
    public static ExpirationPolicy of(BiFunction<Instant, String, Duration> fn) {
        return new ExpirationPolicy() {
            @Override
            public Duration getTtl(Instant createdAt, String cacheKey) {
                return fn.apply(createdAt, cacheKey);
            }
        };
    }
}
