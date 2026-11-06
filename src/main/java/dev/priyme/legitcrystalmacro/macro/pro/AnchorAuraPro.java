package dev.priyme.legitcrystalmacro.macro.pro;

import dev.priyme.legitcrystalmacro.config.MacroConfig;
import dev.priyme.legitcrystalmacro.util.BlockInteract;
import dev.priyme.legitcrystalmacro.util.InventoryUtil;
import dev.priyme.legitcrystalmacro.util.KeyInput;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import net.minecraft.block.Blocks;
import net.minecraft.client.MinecraftClient;
import net.minecraft.item.Items;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

/**
 * PRO 3.6.2 - Anchor Aura Pro
 *
 * <p>Yakinindaki respawn anchor'larini otomatik sarj eder (glowstone) ve guvenli
 * mesafedeyken patlatir. Kristal yerine anchor ile oynayanlar icin pratik bir
 * alternatif: tek tusla cevredeki anchor'lari kullanir.
 *
 * <p>Guvenlik: patlatma sadece (a) Overworld'de, (b) oyuncu anchor'dan en az
 * {@code proAnchorAuraSafeDistance} uzaktaysa, (c) can esigin uzerindeyse yapilir.
 */
public final class AnchorAuraPro {

    private static int cooldown;
    private static int restoreSlot = -1;
    private static int restoreDelay;

    private AnchorAuraPro() {
    }

    public static void reset() {
        cooldown = 0;
        restoreSlot = -1;
        restoreDelay = 0;
    }

    public static void tick(MinecraftClient client, MacroConfig config) {
        if (config == null || !config.proAnchorAuraEnabled) {
            reset();
            return;
        }
        if (client.player == null || client.world == null || client.interactionManager == null) {
            reset();
            return;
        }
        if (!KeyInput.isScreenNull(client) || !client.player.isAlive()) {
            return;
        }
        if (!client.world.getRegistryKey().equals(World.OVERWORLD)) {
            return;
        }

        if (cooldown > 0) {
            cooldown--;
        }
        if (restoreSlot >= 0 && restoreDelay-- <= 0) {
            InventoryUtil.setSelectedSlot(client, restoreSlot);
            restoreSlot = -1;
        }
        if (cooldown > 0) {
            return;
        }

        List<BlockPos> anchors = findAnchors(client, config.proAnchorAuraRange);
        if (anchors.isEmpty()) {
            return;
        }

        boolean charged = false;
        BlockPos target = null;
        for (BlockPos pos : anchors) {
            if (BlockInteract.isChargedAnchor(client, pos)) {
                charged = true;
                target = pos;
                break;
            }
        }

        if (charged) {
            detonate(client, config, target);
            return;
        }

        // Sarj et
        BlockPos uncharged = anchors.get(0);
        int previous = InventoryUtil.getSelectedSlot(client);
        int glow = InventoryUtil.findHotbarSlot(client, Items.GLOWSTONE);
        if (glow == -1) {
            return;
        }
        InventoryUtil.setSelectedSlot(client, glow);

        BlockHitResult hit = new BlockHitResult(Vec3d.ofCenter(uncharged), Direction.UP, uncharged, false);
        ActionResult result = BlockInteract.interactBlock(client, Hand.MAIN_HAND, hit);
        if (result.isAccepted() && client.player != null) {
            client.player.swingHand(Hand.MAIN_HAND);
        }

        if (previous != glow) {
            restoreSlot = previous;
            restoreDelay = 2;
        }
        cooldown = Math.max(2, config.proAnchorAuraDelay);
    }

    private static void detonate(MinecraftClient client, MacroConfig config, BlockPos pos) {
        if (client.player == null) {
            return;
        }

        double distance = client.player.getEyePos().distanceTo(Vec3d.ofCenter(pos));
        if (distance < (double) config.proAnchorAuraSafeDistance) {
            return;
        }
        if (client.player.getHealth() <= (float) config.proShieldHealth) {
            return;
        }

        BlockHitResult hit = new BlockHitResult(Vec3d.ofCenter(pos), Direction.UP, pos, false);
        ActionResult result = BlockInteract.interactBlock(client, Hand.MAIN_HAND, hit);
        if (result.isAccepted()) {
            client.player.swingHand(Hand.MAIN_HAND);
        }
        cooldown = Math.max(3, config.proAnchorAuraDelay + 2);
    }

    private static List<BlockPos> findAnchors(MinecraftClient client, int range) {
        List<BlockPos> found = new ArrayList<>();
        if (client.player == null || client.world == null) {
            return found;
        }

        BlockPos origin = client.player.getBlockPos();
        int r = Math.max(1, Math.min(6, range));

        for (int dx = -r; dx <= r; dx++) {
            for (int dy = -2; dy <= 2; dy++) {
                for (int dz = -r; dz <= r; dz++) {
                    BlockPos pos = origin.add(dx, dy, dz);
                    if (BlockInteract.isBlockAt(client, pos, Blocks.RESPAWN_ANCHOR)) {
                        found.add(pos);
                    }
                }
            }
        }

        found.sort(Comparator.comparingDouble(pos -> client.player.getEyePos().squaredDistanceTo(Vec3d.ofCenter(pos))));
        return found;
    }
}
