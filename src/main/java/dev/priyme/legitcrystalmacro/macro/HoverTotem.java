package dev.priyme.legitcrystalmacro.macro;

import dev.priyme.legitcrystalmacro.config.MacroConfig;
import dev.priyme.legitcrystalmacro.mixin.HandledScreenAccessor;
import dev.priyme.legitcrystalmacro.util.InventoryUtil;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.Slot;

public final class HoverTotem {
   private static int lastHoveredSlotId = -1;
   private static int hoverCount = 0;
   private static long hoverStartTime = 0L;
   private static long targetDelayMs = 0L;
   private static int lastSwappedSlotId = -1;
   private static long lastSwapTime = 0L;
   private static int lastSwapAge = -1;

   private HoverTotem() {
   }

   public static void tick(MinecraftClient client, MacroConfig config) {
      if (config == null || !config.hoverTotemEnabled) {
         reset();
         lastSwappedSlotId = -1;
      } else if (client != null && client.player != null && client.interactionManager != null) {
         if (!(client.currentScreen instanceof HandledScreen<?> screen)) {
            reset();
            lastSwappedSlotId = -1;
         } else {
            ScreenHandler handler = screen.getScreenHandler();
            if (handler != null && handler.getCursorStack().isEmpty()) {
               Slot hovered = ((HandledScreenAccessor)screen).getFocusedSlot();
               if (hovered != null && hovered.id >= 0 && hovered.hasStack() && hovered.getStack().isOf(Items.TOTEM_OF_UNDYING)) {
                  int targetButton = -1;
                  boolean hasOffhand = client.player.getOffHandStack().isOf(Items.TOTEM_OF_UNDYING);
                  if (!hasOffhand) {
                     targetButton = 40;
                  } else if (config.hoverTotemHotbar && isMainInventorySlot(client, hovered)) {
                     int refillSlot = config.hoverTotemHotbarSlot - 1;
                     if (refillSlot >= 0 && refillSlot < 9) {
                        ItemStack hotbarStack = client.player.getInventory().getStack(refillSlot);
                        if (hotbarStack.isEmpty() && (hovered.inventory != client.player.getInventory() || hovered.getIndex() != refillSlot)) {
                           targetButton = refillSlot;
                        }
                     }
                  }

                  if (targetButton == -1) {
                     reset();
                  } else {
                     long now = System.currentTimeMillis();
                     int slotId = hovered.id;
                     if (slotId != lastHoveredSlotId) {
                        lastHoveredSlotId = slotId;
                        hoverCount = 1;
                        hoverStartTime = now;
                        targetDelayMs = Math.max(0, config.hoverTotemDelayMs);
                     } else {
                        hoverCount++;
                     }

                     if ((config.hoverTotemDelayMs <= 0 || hoverCount >= 2)
                        && now - hoverStartTime >= targetDelayMs
                        && (slotId != lastSwappedSlotId || now - lastSwapTime >= 50L + targetDelayMs)) {
                        int playerAge = client.player.age;
                        if (lastSwapAge != playerAge) {
                           InventoryUtil.swap(client, slotId, targetButton);
                           lastSwappedSlotId = slotId;
                           lastSwapTime = now;
                           lastSwapAge = playerAge;
                        }
                     }
                  }
               } else {
                  reset();
               }
            } else {
               reset();
            }
         }
      } else {
         reset();
      }
   }

   private static boolean isMainInventorySlot(MinecraftClient client, Slot slot) {
      if (client != null && client.player != null && slot != null) {
         if (slot.inventory != client.player.getInventory()) {
            return false;
         } else {
            int idx = slot.getIndex();
            return idx >= 9 && idx < 36;
         }
      } else {
         return false;
      }
   }

   public static void reset() {
      lastHoveredSlotId = -1;
      hoverCount = 0;
      hoverStartTime = 0L;
      targetDelayMs = 0L;
   }
}
