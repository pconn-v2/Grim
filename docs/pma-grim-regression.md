# PmaPaper + GrimAC regression matrix

This branch merges Grim upstream fixes while retaining PmaPaper's optional combat rewind and fast-ping bridge.
Do not roll out automatic bans based only on green unit tests. Check PvP behavior against the actual server build.

## Automated gates

- `./gradlew :common:test build --no-daemon` on Java 21 (CI).
- Legacy emulated `CLIENT_TICK_END` must not advance Reach / Hitboxes until a valid movement or transaction arrives.
- True end-tick clients must continue advancing Reach / Hitboxes.
- NoSlow evidence must reset between item-use sessions but still flag two consecutive invalid slowed predictions.
- Legacy item-use sentinels must accept either signed (-1) or unsigned (4095) 1.8 decoder output, without accepting arbitrary values.
- Include Java 25 / Minecraft 26.3 runtime smoke tests separately; Java 21 CI alone does not cover them.

## Manual acceptance matrix (not automatically verified)

| Category | Minimum cases | Expected |
| --- | --- | --- |
| Reach / Hitboxes | Vanilla 1.20.4 via ViaBackwards 5.12.0 on 1.21.11, plus native 1.21.11 | No normal-PvP flags; clearly excessive reach remains detected |
| Rewind | PmaPaper rewind on/off, low ping and 150 ms jitter, repeated attacks | Valid compensated hits remain accepted; uncompensated hits still flag |
| Timer | Fast-ping sampler on/off, ping jitter, packet bursts, high TPS and low TPS | No normal-traffic resets or freezes; accelerated tick stream flags |
| NoFall / GroundSpoof | Edge of blocks, slabs, stairs, knockback, water, teleports | No ordinary landing or transition false positives |
| Simulation / AntiKB | Knockback and jump around 1.21.9+ and 26.3 | No unexpected setbacks; deliberate movement mismatch still flags |
| NoSlow | Start/stop use of food, potions and shield; switch inventory slot mid-use | Cross-session false flag prevented; continuous NoSlow flagged |
| Performance | 25 / 50 / 100 connected combatants, record allocation and CPU profiler samples | No regression in per-player allocations / Netty-loop latency |
| Failure mode | Disable optional Pma bridge / run ordinary Paper | Grim's standalone checks remain enabled |

## Rollout guidance

1. Preserve a known-working jar and config.
2. Run all checks in alert-only mode in the production-like PvP test environment first.
3. Compare false-positive and true-positive counts over identical scripted runs.
4. Only then enable setbacks / automatic sanctions for individually validated checks.
5. Record `/grim version`, PacketEvents and ViaBackwards versions, server version, client version, and logs with each report.

## Dependency policy

The Pma fork temporarily retains the known Pma-compatible PacketEvents `2.14.0-SNAPSHOT` dependency and repository wiring rather than blindly adopting upstream's `2.14.1+b1f9403-SNAPSHOT`. The legacy sentinel check accepts both decoder representations. Validate the newer dependency's resolution and runtime compatibility independently before updating.
