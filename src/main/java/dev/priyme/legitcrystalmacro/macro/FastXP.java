package dev.priyme.legitcrystalmacro.macro;

import dev.priyme.legitcrystalmacro.config.MacroConfig;
import dev.priyme.legitcrystalmacro.util.InventoryUtil;
import dev.priyme.legitcrystalmacro.util.KeyInput;
import net.minecraft.client.MinecraftClient;
import net.minecraft.item.Items;
import net.minecraft.util.Hand;

public final class FastXP {
   private static long lastThrow;
   private static long switchAt;
   private static int requestedSlot = -1;

   private FastXP() {
   }

   public static void tick(MinecraftClient client, MacroConfig config) {
      if (config != null
         && config.fastXpEnabled
         && client.player != null
         && client.interactionManager != null
         && KeyInput.isScreenNull(client)
         && client.options.useKey.isPressed()) {
         long now = System.currentTimeMillis();
         if (!client.player.getMainHandStack().isOf(Items.EXPERIENCE_BOTTLE)) {
            int slot = InventoryUtil.findHotbarSlot(client, Items.EXPERIENCE_BOTTLE);
            if (slot < 0) {
               requestedSlot = -1;
               return;
            }

            if (requestedSlot != slot) {
               requestedSlot = slot;
               switchAt = now + Math.max(0, config.fastXpSwitchDelay);
            }

            if (now < switchAt) {
               return;
            }

            InventoryUtil.setSelectedSlot(client, slot);
         }

         requestedSlot = -1;
         if (now - lastThrow >= Math.max(1, config.fastXpDelay) * 50L && client.interactionManager.interactItem(client.player, Hand.MAIN_HAND).isAccepted()) {
            lastThrow = now;
         }
      } else {
         requestedSlot = -1;
         switchAt = 0L;
      }
   }
}
