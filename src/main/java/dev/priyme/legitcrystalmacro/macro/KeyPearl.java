package dev.priyme.legitcrystalmacro.macro;

import dev.priyme.legitcrystalmacro.config.MacroConfig;
import dev.priyme.legitcrystalmacro.util.InventoryUtil;
import dev.priyme.legitcrystalmacro.util.KeyInput;
import net.minecraft.client.MinecraftClient;
import net.minecraft.item.Items;
import net.minecraft.util.Hand;

public final class KeyPearl {
   private static boolean wasDown = false;
   private static long lastThrow = 0L;

   private KeyPearl() {
   }

   public static void tick(MinecraftClient client, MacroConfig config) {
      if (config != null && client.player != null && client.interactionManager != null) {
         boolean isDown = config.keyPearlKey > 0 && KeyInput.isPressed(client, config.keyPearlKey);
         if (config.keyPearlEnabled && KeyInput.isScreenNull(client)) {
            if (isDown && !wasDown) {
               long now = System.currentTimeMillis();
               if (now - lastThrow >= config.keyPearlDelay) {
                  int pearlSlot = InventoryUtil.findHotbarSlot(client, Items.ENDER_PEARL);
                  if (pearlSlot != -1) {
                     int prev = InventoryUtil.getSelectedSlot(client);
                     InventoryUtil.setSelectedSlot(client, pearlSlot);
                     client.interactionManager.interactItem(client.player, Hand.MAIN_HAND);
                     if (config.keyPearlSwitchBack) {
                        InventoryUtil.setSelectedSlot(client, prev);
                     }

                     lastThrow = now;
                  }
               }
            }

            wasDown = isDown;
         } else {
            wasDown = isDown;
         }
      } else {
         wasDown = false;
      }
   }
}
