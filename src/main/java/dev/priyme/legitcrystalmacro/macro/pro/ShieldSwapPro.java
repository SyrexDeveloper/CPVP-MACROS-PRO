package dev.priyme.legitcrystalmacro.macro.pro;

import dev.priyme.legitcrystalmacro.config.MacroConfig;
import dev.priyme.legitcrystalmacro.util.InventoryUtil;
import dev.priyme.legitcrystalmacro.util.KeyInput;
import net.minecraft.client.MinecraftClient;
import net.minecraft.item.Items;
import net.minecraft.network.packet.c2s.play.PlayerActionC2SPacket;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;

/**
 * PRO 3.6.2 - Shield Swap Pro
 *
 * <p>Can esigin altina dustugunde kalkani otomatik olarak offhand'e alip kaldirir,
 * can geri topladiginda kalkani indirir. Totem onceligi korunur: offhand'de totem
 * varsa kalkan takasina girismez.
 */
public final class ShieldSwapPro {

    private static int cooldown;
    private static boolean raised;
    private static int restoreSlot = -1;
    private static int restoreDelay;

    private ShieldSwapPro() {
    }

    public static void reset() {
        cooldown = 0;
        raised = false;
        restoreSlot = -1;
        restoreDelay = 0;
    }

    public static void tick(MinecraftClient client, MacroConfig config) {
        if (config == null || !config.proShieldSwapEnabled) {
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
        if (restoreSlot >= 0 && restoreDelay-- <= 0) {
            InventoryUtil.setSelectedSlot(client, restoreSlot);
            restoreSlot = -1;
        }

        float health = client.player.getHealth();
        boolean low = health <= (float) config.proShieldHealth;

        // Can geri toplandi -> kalkani indir
        if (!low && raised) {
            if (client.player.isUsingItem()) {
                client.interactionManager.stopUsingItem(client.player);
            }
            raised = false;
            return;
        }

        if (!low) {
            return;
        }

        boolean offhandShield = client.player.getOffHandStack().isOf(Items.SHIELD);
        boolean mainShield = client.player.getMainHandStack().isOf(Items.SHIELD);

        if (!offhandShield && !mainShield) {
            // Totem varsa offhand'e dokunmayiz
            if (client.player.getOffHandStack().isOf(Items.TOTEM_OF_UNDYING)) {
                return;
            }
            if (cooldown > 0) {
                return;
            }
            int shieldSlot = InventoryUtil.findHotbarSlot(client, Items.SHIELD);
            if (shieldSlot == -1) {
                return;
            }
            int previous = InventoryUtil.getSelectedSlot(client);
            InventoryUtil.setSelectedSlot(client, shieldSlot);
            swapWithOffhand(client);
            if (previous != shieldSlot) {
                restoreSlot = previous;
                restoreDelay = 2;
            }
            cooldown = 4;
            return;
        }

        // Kalkani kaldir (kullan)
        if (!client.player.isUsingItem() && !raised && cooldown <= 0) {
            Hand hand = offhandShield ? Hand.OFF_HAND : Hand.MAIN_HAND;
            if (client.interactionManager.interactItem(client.player, hand).isAccepted()) {
                raised = true;
                cooldown = 6;
            }
        }
    }

    private static void swapWithOffhand(MinecraftClient client) {
        if (client.player != null && client.player.networkHandler != null) {
            client.player.networkHandler.sendPacket(new PlayerActionC2SPacket(
                PlayerActionC2SPacket.Action.SWAP_ITEM_WITH_OFFHAND,
                BlockPos.ORIGIN,
                Direction.DOWN
            ));
        }
    }
}
