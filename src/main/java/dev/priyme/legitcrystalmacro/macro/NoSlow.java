package dev.priyme.legitcrystalmacro.macro;

import dev.priyme.legitcrystalmacro.config.MacroConfig;
import dev.priyme.legitcrystalmacro.util.InventoryUtil;
import net.minecraft.client.MinecraftClient;
import net.minecraft.network.packet.c2s.play.ClientCommandC2SPacket;
import net.minecraft.network.packet.c2s.play.UpdateSelectedSlotC2SPacket;
import net.minecraft.network.packet.c2s.play.ClientCommandC2SPacket.Mode;

public final class NoSlow {
   private NoSlow() {
   }

   public static boolean shouldCancelSlowdown(MacroConfig config) {
      return config != null && config.noSlowEnabled;
   }

   public static void reset() {
   }

   public static void onPreMotion(MinecraftClient client, MacroConfig config) {
      if (shouldCancelSlowdown(config) && client.player != null && client.getNetworkHandler() != null) {
         if (!client.player.isDead() && !(client.player.getHealth() <= 0.0F) && !client.player.isSleeping()) {
            if (client.player.isUsingItem() && !client.player.hasVehicle()) {
               if (config.noSlowMode == 1) {
                  int slot = InventoryUtil.getSelectedSlot(client);
                  if (slot >= 0 && slot <= 8) {
                     client.getNetworkHandler().sendPacket(new UpdateSelectedSlotC2SPacket(slot));
                  }
               }
            }
         }
      }
   }

   public static void onPostMotion(MinecraftClient client, MacroConfig config) {
      if (shouldCancelSlowdown(config) && client.player != null && client.getNetworkHandler() != null) {
         if (!client.player.isDead() && !(client.player.getHealth() <= 0.0F) && !client.player.isSleeping()) {
            if (client.player.isUsingItem() && !client.player.hasVehicle()) {
               if (config.noSlowMode == 1 && client.player.isSprinting()) {
                  client.getNetworkHandler().sendPacket(new ClientCommandC2SPacket(client.player, Mode.START_SPRINTING));
               }
            }
         }
      }
   }
}
