package dev.priyme.legitcrystalmacro.macro.pro;

import dev.priyme.legitcrystalmacro.config.MacroConfig;
import dev.priyme.legitcrystalmacro.util.InventoryUtil;
import dev.priyme.legitcrystalmacro.util.KeyInput;
import net.minecraft.client.MinecraftClient;
import net.minecraft.item.Items;
import net.minecraft.network.packet.c2s.play.PlayerActionC2SPacket;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;

/**
 * PRO 3.6.2 - Auto Totem Pro
 *
 * <p>Can belirli bir esigin altina dustugunde envanterdeki/hotbar'daki totemi
 * offhand'e takar. Offhand'de totem yoksa ve can yuksekse dokunmaz (kalkan vb.
 * korunur). Takas sonrasi hotbar slotu geri alinir, yani oyuncu elindeki esyayi
 * kaybetmez.
 */
public final class AutoTotemPro {

    private static int cooldown;
    private static int restoreSlot = -1;
    private static int restoreDelay;

    private AutoTotemPro() {
    }

    public static void reset() {
        cooldown = 0;
        restoreSlot = -1;
        restoreDelay = 0;
    }

    public static void tick(MinecraftClient client, MacroConfig config) {
        if (config == null || !config.proAutoTotemEnabled) {
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

        if (cooldown > 0) {
            cooldown--;
        }

        // Bekleyen slot geri donusu
        if (restoreSlot >= 0) {
            if (restoreDelay-- <= 0) {
                InventoryUtil.setSelectedSlot(client, restoreSlot);
                restoreSlot = -1;
                restoreDelay = 0;
            }
        }

        boolean offhandTotem = client.player.getOffHandStack().isOf(Items.TOTEM_OF_UNDYING);
        float health = client.player.getHealth();
        boolean critical = health <= (float) config.proAutoTotemHealth;

        if (offhandTotem) {
            return;
        }

        // Can iyi ve offhand'de baska bir esya varsa (kalkan/yemek) karismayalim
        if (!critical && !client.player.getOffHandStack().isEmpty()) {
            return;
        }

        if (cooldown > 0) {
            return;
        }

        int totemSlot = InventoryUtil.findHotbarSlot(client, Items.TOTEM_OF_UNDYING);
        if (totemSlot == -1) {
            // hotbar'da yok: envanterden hotbar'a cek (shift + tik)
            int empty = findEmptyHotbarSlot(client);
            int invSlot = findInventoryTotemSlot(client);
            if (empty != -1 && invSlot != -1) {
                InventoryUtil.setSelectedSlot(client, empty);
                InventoryUtil.quickMove(client, invSlot);
                totemSlot = empty;
            }
        }

        if (totemSlot == -1) {
            return;
        }

        int previous = InventoryUtil.getSelectedSlot(client);
        if (previous != totemSlot) {
            InventoryUtil.setSelectedSlot(client, totemSlot);
        }
        swapWithOffhand(client);

        if (previous != totemSlot) {
            restoreSlot = previous;
            restoreDelay = Math.max(1, config.proAutoTotemDelay);
        }

        cooldown = Math.max(2, config.proAutoTotemDelay + 2);
    }

    private static int findEmptyHotbarSlot(MinecraftClient client) {
        if (client.player == null) {
            return -1;
        }
        for (int i = 0; i < 9; i++) {
            if (client.player.getInventory().getStack(i).isEmpty()) {
                return i;
            }
        }
        return -1;
    }

    private static int findInventoryTotemSlot(MinecraftClient client) {
        if (client.player == null) {
            return -1;
        }
        for (int i = 9; i < 36; i++) {
            if (client.player.getInventory().getStack(i).isOf(Items.TOTEM_OF_UNDYING)) {
                return i;
            }
        }
        return -1;
    }

    private static void swapWithOffhand(MinecraftClient client) {
        if (client.player != null && client.player.networkHandler != null) {
            client.player.networkHandler.sendPacket(new PlayerActionC2SPacket(
                PlayerActionC2SPacket.Action.SWAP_ITEM_WITH_OFFHAND,
                BlockPos.ORIGIN,
                Direction.DOWN
            ));
            client.player.swingHand(net.minecraft.util.Hand.MAIN_HAND);
        }
    }
}
