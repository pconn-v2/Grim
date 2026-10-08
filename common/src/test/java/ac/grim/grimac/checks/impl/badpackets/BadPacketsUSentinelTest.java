package ac.grim.grimac.checks.impl.badpackets;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class BadPacketsUSentinelTest {

    @Test
    void legacyItemUseDecodingAcceptsSignedAndUnsignedEighteenSentinel() {
        assertTrue(LegacyUseItemSentinel.isValidY(-1, true));
        assertTrue(LegacyUseItemSentinel.isValidY(4095, true));
        assertFalse(LegacyUseItemSentinel.isValidY(0, true));
    }

    @Test
    void seventeenRetainsItsOwnSentinel() {
        assertTrue(LegacyUseItemSentinel.isValidY(255, false));
        assertFalse(LegacyUseItemSentinel.isValidY(-1, false));
        assertFalse(LegacyUseItemSentinel.isValidY(4095, false));
    }
}
