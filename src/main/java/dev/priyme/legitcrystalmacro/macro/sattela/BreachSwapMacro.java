package dev.priyme.legitcrystalmacro.macro.sattela;

import dev.priyme.legitcrystalmacro.config.MacroConfig;
import dev.priyme.legitcrystalmacro.util.FriendManager;
import dev.priyme.legitcrystalmacro.util.InventoryUtil;
import dev.priyme.legitcrystalmacro.util.KeyInput;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.EntityHitResult;

public final class BreachSwapMacro {
   private static boolean wasDown = false;
   private static int step = 0;
   private static int initialSlot = -1;
   private static long swapTime = 0L;

   private BreachSwapMacro() {
   }

   public static void tick(MinecraftClient client, MacroConfig config) {
      if (config == null || !config.sattelaBreachSwapEnabled) {
         reset(client);
      } else if (client.player != null && client.world != null && client.interactionManager != null) {
         boolean isDown = config.sattelaBreachSwapKey > 0 && KeyInput.isPressed(client, config.sattelaBreachSwapKey);
         if (client.currentScreen != null) {
            wasDown = isDown;
            reset(client);
         } else {
            long now = System.currentTimeMillis();
            boolean trigger = isDown && !wasDown;
            wasDown = isDown;
            if (step == 0 && trigger) {
               Entity target = findTarget(client, 4.5F);
               if (target != null) {
                  int maceSlot = findMaceSlot(client);
                  if (maceSlot != -1) {
                     initialSlot = InventoryUtil.getSelectedSlot(client);
                     InventoryUtil.setSelectedSlot(client, maceSlot);
                     client.interactionManager.attackEntity(client.player, target);
                     client.player.swingHand(Hand.MAIN_HAND);
                     swapTime = now;
                     step = 1;
                  }
               }
            } else if (step == 1) {
               long delay = Math.max(20, Math.min(250, config.sattelaBreachSwapDelay));
               if (now - swapTime >= delay) {
                  reset(client);
               }
            }
         }
      } else {
         reset(client);
      }
   }

   private static void reset(MinecraftClient client) {
      if (initialSlot != -1 && client.player != null) {
         InventoryUtil.setSelectedSlot(client, initialSlot);
      }

      initialSlot = -1;
      step = 0;
   }

   private static Entity findTarget(MinecraftClient client, float reach) {
      if (client.crosshairTarget instanceof EntityHitResult ehr
         && ehr.getEntity() instanceof PlayerEntity p
         && p != client.player
         && p.isAlive()
         && !FriendManager.isFriend(p.getName().getString())) {
         return p;
      } else {
         Entity best = null;
         double minDistanceSq = reach * reach;

         for (PlayerEntity other : client.world.getPlayers()) {
            if (other != client.player && other.isAlive() && !FriendManager.isFriend(other.getName().getString()) && client.player.canSee(other)) {
               double distSq = client.player.squaredDistanceTo(other);
               if (distSq <= minDistanceSq) {
                  minDistanceSq = distSq;
                  best = other;
               }
            }
         }

         return best;
      }
   }

   private static int findMaceSlot(MinecraftClient client) {
      if (client.player == null) {
         return -1;
      } else {
         for (int i = 0; i < 9; i++) {
            ItemStack stack = client.player.getInventory().getStack(i);
            if (stack.isOf(Items.MACE) || stack.getItem().toString().toLowerCase().contains("mace")) {
               return i;
            }
         }

         return -1;
      }
   }
}
