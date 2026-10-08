package ac.grim.grimac.checks.impl.badpackets;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class BadPacketsUSentinelTest {

    @Test
    void legacyItemUseDecodingAcceptsSignedAndUnsignedEighteenSentinel() {
        assertTrue(BadPacketsU.isValidLegacyUseItemY(-1, true));
        assertTrue(BadPacketsU.isValidLegacyUseItemY(4095, true));
        assertFalse(BadPacketsU.isValidLegacyUseItemY(0, true));
    }

    @Test
    void seventeenRetainsItsOwnSentinel() {
        assertTrue(BadPacketsU.isValidLegacyUseItemY(255, false));
        assertFalse(BadPacketsU.isValidLegacyUseItemY(-1, false));
        assertFalse(BadPacketsU.isValidLegacyUseItemY(4095, false));
    }
}
