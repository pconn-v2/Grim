package ac.grim.grimac.checks.impl.badpackets;

/**
 * PacketEvents decoders differ in whether the legacy 1.8 sentinel is signed.
 * 1.7 has a separate wrapped-Y representation.
 */
final class LegacyUseItemSentinel {
    private LegacyUseItemSentinel() {
    }

    static boolean isValidY(int y, boolean v18) {
        return v18 ? y == -1 || y == 4095 : y == 255;
    }
}
