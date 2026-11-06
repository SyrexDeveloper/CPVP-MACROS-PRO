# Development status

Sources imported from `CPVP-Macros-Pro-3.6.2-kaynak-kod.zip` supplied by the owner
on the repository's main branch. Original authorship and license are retained.

## Crystal sequence fix

- Resolve the obsidian destination from the clicked face (or replaceable target).
- Restore the original slot after the obsidian placement attempt, preserving
  offhand-crystal use.
- Wait for the obsidian world state, then interact with its **top face**.
- Track the new base while waiting for the crystal entity and attack it only
  after it exists, respecting break delay, loot protection and self-distance.
- Cancel on key release, screen/world reset, timeout, distance or safety failure.
- Do not run ordinary crystal cycles during the obsidian switch delay.

## Build

Requires Java 21 and access to Gradle, Mojang, Fabric and Maven repositories.
Run `./gradlew --no-daemon build`; the GitHub Actions workflow does the same
and uploads `build/libs/*.jar`. No successful compilation or in-game test is
claimed until this workflow succeeds and the jar is tested in Minecraft.
The imported project includes precompiled original mixins and a jar-replacement
task; these are retained unchanged, not independently verified.

## Manual regression checklist (Minecraft 1.21.11 / Fabric)

Use a private test world; hold the configured activation key throughout.

- Crystal main hand + obsidian hotbar: target solid ground, confirm the new
  obsidian base, crystal spawn and subsequent explosion.
- Repeat on side faces and replaceable blocks.
- Repeat with crystal in offhand; original main-hand slot must be restored.
- Release during switch delay and during spawn wait: no further macro actions.
- Rejected placement, missing obsidian, blocked space above base, unreachable
  base and server lag must not produce an endless placement loop.
- Self-distance and loot protections must prevent the sequence when applicable.
- Existing obsidian/bedrock targeting must still work normally.
- Closing the world or opening the menu clears pending state.

## Remaining requested expansion

Six existing categories: CPvP, UHC, Mace, Movement, Player and Other.
The requested twelve functional modules per category is **not implemented** in
this patch. Audit existing modules, agree the missing features, then implement
and test them category by category; do not pad menus with non-functional toggles.
