package dev.priyme.legitcrystalmacro.util;

import java.util.function.Predicate;
import net.minecraft.client.MinecraftClient;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.network.packet.c2s.play.UpdateSelectedSlotC2SPacket;
import net.minecraft.screen.slot.SlotActionType;

public final class InventoryUtil {
   private InventoryUtil() {
   }

   public static int getSelectedSlot(MinecraftClient client) {
      return client.player == null ? 0 : client.player.getInventory().getSelectedSlot();
   }

   public static void setSelectedSlot(MinecraftClient client, int slot) {
      if (client.player != null && slot >= 0 && slot <= 8) {
         client.player.getInventory().setSelectedSlot(slot);
         if (client.player.networkHandler != null) {
            client.player.networkHandler.sendPacket(new UpdateSelectedSlotC2SPacket(slot));
         }
      }
   }

   public static int findHotbarSlot(MinecraftClient client, Item item) {
      if (client.player == null) {
         return -1;
      } else {
         for (int i = 0; i < 9; i++) {
            ItemStack stack = client.player.getInventory().getStack(i);
            if (stack.isOf(item)) {
               return i;
            }
         }

         return -1;
      }
   }

   public static int findHotbarSlotMatching(MinecraftClient client, Predicate<ItemStack> predicate) {
      if (client.player != null && predicate != null) {
         for (int i = 0; i < 9; i++) {
            ItemStack stack = client.player.getInventory().getStack(i);
            if (predicate.test(stack)) {
               return i;
            }
         }

         return -1;
      } else {
         return -1;
      }
   }

   public static boolean isHolding(MinecraftClient client, Item item) {
      return client.player == null ? false : client.player.getMainHandStack().isOf(item) || client.player.getOffHandStack().isOf(item);
   }

   public static void quickMove(MinecraftClient client, int slot) {
      if (client.player != null && client.interactionManager != null) {
         client.interactionManager.clickSlot(client.player.currentScreenHandler.syncId, slot, 0, SlotActionType.QUICK_MOVE, client.player);
      }
   }

   public static void swap(MinecraftClient client, int slot, int hotbarTarget) {
      if (client.player != null && client.interactionManager != null) {
         client.interactionManager.clickSlot(client.player.currentScreenHandler.syncId, slot, hotbarTarget, SlotActionType.SWAP, client.player);
      }
   }
}
