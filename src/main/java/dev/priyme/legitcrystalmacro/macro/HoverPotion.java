package dev.priyme.legitcrystalmacro.macro;

import dev.priyme.legitcrystalmacro.config.MacroConfig;
import dev.priyme.legitcrystalmacro.mixin.HandledScreenAccessor;
import dev.priyme.legitcrystalmacro.util.InventoryUtil;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.item.ItemStack;
import net.minecraft.item.PotionItem;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.Slot;

public final class HoverPotion {
   private static Object lastScreen = null;
   private static int lastSlotId = -1;
   private static long hoverStart = 0L;
   private static int lastSwappedSlotId = -1;

   private HoverPotion() {
   }

   public static void tick(MinecraftClient client, MacroConfig config) {
      if (config == null || !config.hoverPotionEnabled) {
         reset();
         lastSwappedSlotId = -1;
         lastScreen = null;
      } else if (client != null && client.player != null && client.interactionManager != null) {
         if (client.currentScreen instanceof HandledScreen<?> screen) {
            if (client.currentScreen != lastScreen) {
               lastScreen = client.currentScreen;
               reset();
               lastSwappedSlotId = -1;
            }

            ScreenHandler handler = screen.getScreenHandler();
            if (handler != null && handler.getCursorStack().isEmpty()) {
               Slot slot = ((HandledScreenAccessor)screen).getFocusedSlot();
               if (slot != null && slot.id >= 0 && slot.hasStack()) {
                  ItemStack stack = slot.getStack();
                  if (!(stack.getItem() instanceof PotionItem)) {
                     reset();
                  } else {
                     int targetHotbar = Math.max(0, Math.min(8, config.hoverPotionTargetSlot - 1));
                     if (slot.inventory == client.player.getInventory() && slot.getIndex() == targetHotbar) {
                        reset();
                     } else {
                        long now = System.currentTimeMillis();
                        if (slot.id != lastSlotId) {
                           lastSlotId = slot.id;
                           hoverStart = now;
                           lastSwappedSlotId = -1;
                        } else {
                           if (now - hoverStart >= config.hoverPotionDelay && slot.id != lastSwappedSlotId) {
                              InventoryUtil.swap(client, slot.id, targetHotbar);
                              lastSwappedSlotId = slot.id;
                           }
                        }
                     }
                  }
               } else {
                  reset();
               }
            } else {
               reset();
            }
         } else {
            reset();
            lastSwappedSlotId = -1;
            lastScreen = null;
         }
      } else {
         reset();
         lastScreen = null;
      }
   }

   public static void reset() {
      lastSlotId = -1;
      hoverStart = 0L;
      lastSwappedSlotId = -1;
   }
}
