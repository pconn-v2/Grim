package ac.grim.grimac.checks;

import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GrimProcessorTickTest {

    @Test
    void viaBackwardsEmulatedTickEndCannotFlushLegacyReach() {
        assertFalse(GrimProcessor.isUpdatePacket(PacketType.Play.Client.CLIENT_TICK_END, false));
        assertTrue(GrimProcessor.isUpdatePacket(PacketType.Play.Client.CLIENT_TICK_END, true));
    }

    @Test
    void movementAndTransactionStillAdvanceChecks() {
        assertTrue(GrimProcessor.isUpdatePacket(PacketType.Play.Client.PLAYER_FLYING, false));
        assertTrue(GrimProcessor.isUpdatePacket(PacketType.Play.Client.PONG, false));
    }
}
