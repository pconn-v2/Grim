package ac.grim.grimac.checks.impl.movement;

/**
 * Pure state transition for checked NoSlow predictions. No previous item-use
 * session can contribute to a new consecutive failure streak.
 */
final class NoSlowFailureState {
    private NoSlowFailureState() {
    }

    static boolean next(boolean usingItem, boolean predictionFailed) {
        return usingItem && predictionFailed;
    }
}
