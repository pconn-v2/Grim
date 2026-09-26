package ac.grim.grimac.utils.anticheat;

import ac.grim.grimac.player.GrimPlayer;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * Optional compatibility bridge for PmaPaper's server-authoritative hit rewind.
 *
 * <p>The bridge is deliberately reflection-only so Grim's common module does not
 * acquire a compile-time dependency on PmaPaper/NMS. It only reads a result that
 * PmaPaper already computed; it never re-runs rewind or consumes compensation
 * budget.
 */
public final class PmaPaperCompat {
    private static final long MAX_RESCUE_AGE_NANOS = TimeUnit.SECONDS.toNanos(2);
    private static final int FAST_PING_ID_MASK = 0xFFFF_0000;
    private static final int FAST_PING_ID_PREFIX = 0x504D_0000;
    private static final @Nullable Bridge BRIDGE = Bridge.resolve();
    private static final boolean PMA_PAPER_PRESENT = BRIDGE != null || hasClass("org.pmapaper.network.PlayPhasePingSampler");

    private PmaPaperCompat() {
    }

    public static boolean isAvailable() {
        return BRIDGE != null;
    }

    public static boolean isPmaPaper() {
        return PMA_PAPER_PRESENT;
    }

    public static boolean isPmaPaperFastPingPong(int id) {
        return PMA_PAPER_PRESENT && (id & FAST_PING_ID_MASK) == FAST_PING_ID_PREFIX;
    }

    public static boolean isHitRewindEnabled() {
        Bridge bridge = BRIDGE;
        return bridge != null && bridge.isHitRewindEnabled();
    }

    public static long matchingRewindRescueSequenceAfter(GrimPlayer player, @Nullable UUID targetUuid, long attackNanos) {
        Bridge bridge = BRIDGE;
        if (bridge == null || targetUuid == null || attackNanos == Long.MIN_VALUE || player.platformPlayer == null) {
            return -1L;
        }

        RewindMarker marker = bridge.latestMarker(player);
        if (marker == null || !targetUuid.equals(marker.targetUuid()) || marker.nanoTime() < attackNanos) {
            return -1L;
        }

        long age = System.nanoTime() - marker.nanoTime();
        return age >= 0L && age <= MAX_RESCUE_AGE_NANOS ? marker.sequence() : -1L;
    }

    private static boolean hasClass(String name) {
        try {
            Class.forName(name, false, PmaPaperCompat.class.getClassLoader());
            return true;
        } catch (ClassNotFoundException | LinkageError ignored) {
            return false;
        }
    }

    private record RewindMarker(long sequence, UUID targetUuid, long nanoTime) {
    }

    private static final class Bridge {
        private final Method hitRewindEnabled;
        private final Method latestRewindRescue;
        private volatile @Nullable Class<?> nativePlayerClass;
        private volatile @Nullable Method getHandle;
        private volatile @Nullable Class<?> markerClass;
        private volatile @Nullable Method markerSequence;
        private volatile @Nullable Method markerTargetUuid;
        private volatile @Nullable Method markerNanoTime;

        private Bridge(Method hitRewindEnabled, Method latestRewindRescue) {
            this.hitRewindEnabled = hitRewindEnabled;
            this.latestRewindRescue = latestRewindRescue;
        }

        private static @Nullable Bridge resolve() {
            try {
                Class<?> combatHistory = Class.forName("org.pmapaper.pvp.AnticheatCombatBridge");
                Method enabled = combatHistory.getMethod("hitRewindEnabled");
                Method latest = null;
                for (Method method : combatHistory.getMethods()) {
                    if (method.getName().equals("latestHitRewindRescue") && method.getParameterCount() == 1) {
                        latest = method;
                        break;
                    }
                }
                return latest == null ? null : new Bridge(enabled, latest);
            } catch (ClassNotFoundException | NoSuchMethodException ignored) {
                return null;
            }
        }

        private boolean isHitRewindEnabled() {
            try {
                return Boolean.TRUE.equals(hitRewindEnabled.invoke(null));
            } catch (IllegalAccessException | InvocationTargetException ignored) {
                return false;
            }
        }

        private @Nullable RewindMarker latestMarker(GrimPlayer player) {
            try {
                Object nativePlayer = player.platformPlayer.getNative();
                Object handle = getHandle(nativePlayer);
                if (handle == null) {
                    return null;
                }

                Object marker = latestRewindRescue.invoke(null, handle);
                if (marker == null) {
                    return null;
                }

                resolveMarkerAccessors(marker.getClass());
                Method sequence = markerSequence;
                Method targetUuid = markerTargetUuid;
                Method nanoTime = markerNanoTime;
                if (sequence == null || targetUuid == null || nanoTime == null) {
                    return null;
                }

                return new RewindMarker(
                        ((Number) sequence.invoke(marker)).longValue(),
                        (UUID) targetUuid.invoke(marker),
                        ((Number) nanoTime.invoke(marker)).longValue()
                );
            } catch (ReflectiveOperationException | ClassCastException ignored) {
                return null;
            }
        }

        private @Nullable Object getHandle(Object nativePlayer) throws ReflectiveOperationException {
            Class<?> type = nativePlayer.getClass();
            Method method = getHandle;
            if (method == null || nativePlayerClass != type) {
                method = type.getMethod("getHandle");
                nativePlayerClass = type;
                getHandle = method;
            }
            return method.invoke(nativePlayer);
        }

        private void resolveMarkerAccessors(Class<?> type) throws NoSuchMethodException {
            if (markerClass == type && markerSequence != null && markerTargetUuid != null && markerNanoTime != null) {
                return;
            }

            markerSequence = type.getMethod("sequence");
            markerTargetUuid = type.getMethod("targetUuid");
            markerNanoTime = type.getMethod("nanoTime");
            markerClass = type;
        }
    }
}
