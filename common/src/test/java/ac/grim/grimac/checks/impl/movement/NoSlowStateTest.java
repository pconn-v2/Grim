package ac.grim.grimac.checks.impl.movement;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class NoSlowStateTest {

    private record Result(boolean shouldFlag, boolean nextFailed) {}

    private static Result tick(boolean previousFailed, boolean usingItem, boolean failedPrediction) {
        boolean nextFailed = NoSlow.nextFailureState(usingItem, failedPrediction);
        return new Result(previousFailed && nextFailed, nextFailed);
    }

    @Test
    void isolatedFailedTicksFromDifferentItemUsesDoNotAccumulate() {
        Result firstUse = tick(false, true, true);
        assertFalse(firstUse.shouldFlag());
        Result endedUse = tick(firstUse.nextFailed(), false, false);
        assertFalse(endedUse.nextFailed());
        Result secondUse = tick(endedUse.nextFailed(), true, true);
        assertFalse(secondUse.shouldFlag());
    }

    @Test
    void repeatedBadPredictionsWithinOneUseStillFlag() {
        Result first = tick(false, true, true);
        Result second = tick(first.nextFailed(), true, true);
        assertTrue(second.shouldFlag());
    }

    @Test
    void legitimateTickBreaksFailureStreak() {
        Result first = tick(false, true, true);
        Result clean = tick(first.nextFailed(), true, false);
        Result next = tick(clean.nextFailed(), true, true);
        assertFalse(next.shouldFlag());
    }
}
