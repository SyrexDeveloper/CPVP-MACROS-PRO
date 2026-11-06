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
import net.minecraft.registry.tag.ItemTags;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.EntityHitResult;
import org.lwjgl.glfw.GLFW;

public final class StunSlamMacro {
   private static boolean wasLeftClick = false;
   private static boolean wasCustomKey = false;
   private static long stageStartTime = 0L;
   private static long lastComboTime = 0L;
   private static int initialSlot = -1;
   private static int maceSlotUsed = -1;
   private static Entity activeTarget = null;
   private static int step = 0;

   private StunSlamMacro() {
   }

   public static void tick(MinecraftClient client, MacroConfig config) {
      if (config == null || !config.sattelaStunSlamEnabled) {
         reset(client);
      } else if (client.player == null || client.world == null || client.interactionManager == null || client.getWindow() == null) {
         reset(client);
      } else if (!KeyInput.isScreenNull(client)) {
         reset(client);
      } else {
         long now = System.currentTimeMillis();
         boolean isLeftClick = GLFW.glfwGetMouseButton(client.getWindow().getHandle(), 0) == 1 || client.options.attackKey.isPressed();
         boolean isCustomKey = config.sattelaStunSlamKey > 0 && KeyInput.isPressed(client, config.sattelaStunSlamKey);
         boolean manualTrigger = isLeftClick && !wasLeftClick || isCustomKey && !wasCustomKey;
         wasLeftClick = isLeftClick;
         wasCustomKey = isCustomKey;
         if (step != 0 || !client.player.isOnGround()) {
            boolean isAirborne = !client.player.isOnGround();
            float reach = (float)(config.sattelaStunSlamReach > 0.0 ? config.sattelaStunSlamReach : 4.2F);
            Entity target = findHittableTarget(client, reach);
            boolean canAutoTrigger = config.sattelaStunSlamAuto
               && isAirborne
               && target instanceof PlayerEntity p
               && (!config.sattelaStunSlamShieldOnly || isShieldBlocking(p))
               && now - lastComboTime >= 400L;
            boolean trigger = manualTrigger || canAutoTrigger;
            if (step == 0) {
               if (trigger && isAirborne && target != null) {
                  int axeSlot = findAxeSlot(client);
                  int maceSlot = findMaceSlot(client);
                  if (axeSlot != -1 && maceSlot != -1) {
                     initialSlot = InventoryUtil.getSelectedSlot(client);
                     maceSlotUsed = maceSlot;
                     activeTarget = target;
                     InventoryUtil.setSelectedSlot(client, axeSlot);
                     performAttack(client, activeTarget);
                     stageStartTime = now;
                     step = 1;
                  }
               }
            } else if (step == 1) {
               long delay = Math.max(10, Math.min(300, config.sattelaStunSlamDelay));
               if (now - stageStartTime >= delay || client.player.isOnGround()) {
                  if (maceSlotUsed != -1) {
                     InventoryUtil.setSelectedSlot(client, maceSlotUsed);
                     performAttack(client, activeTarget);
                     stageStartTime = now;
                     step = 2;
                  } else {
                     reset(client);
                  }
               }
            } else if (step == 2 && (now - stageStartTime >= 40L || client.player.isOnGround())) {
               lastComboTime = now;
               reset(client);
            }
         }
      }
   }

   private static boolean isShieldBlocking(PlayerEntity player) {
      return player == null
         ? false
         : player.isBlocking() || player.isUsingItem() && (player.getMainHandStack().isOf(Items.SHIELD) || player.getOffHandStack().isOf(Items.SHIELD));
   }

   private static void reset(MinecraftClient client) {
      if (initialSlot != -1 && client.player != null) {
         InventoryUtil.setSelectedSlot(client, initialSlot);
      }

      initialSlot = -1;
      maceSlotUsed = -1;
      activeTarget = null;
      step = 0;
   }

   public static Entity findHittableTarget(MinecraftClient client, float reachDistance) {
      if (client.world != null && client.player != null) {
         if (client.crosshairTarget instanceof EntityHitResult ehr
            && ehr.getEntity() instanceof PlayerEntity p
            && p != client.player
            && p.isAlive()
            && !FriendManager.isFriend(p.getName().getString())
            && client.player.squaredDistanceTo(p) <= (reachDistance + 0.5F) * (reachDistance + 0.5F)) {
            return p;
         } else {
            Entity best = null;
            double minDistanceSq = reachDistance * reachDistance;

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
      } else {
         return null;
      }
   }

   private static void performAttack(MinecraftClient client, Entity target) {
      if (client.player != null && client.interactionManager != null) {
         Entity finalTarget = target != null && target.isAlive() ? target : findHittableTarget(client, 4.5F);
         if (finalTarget != null) {
            client.interactionManager.attackEntity(client.player, finalTarget);
            client.player.swingHand(Hand.MAIN_HAND);
         }
      }
   }

   public static int findAxeSlot(MinecraftClient client) {
      if (client.player == null) {
         return -1;
      } else {
         for (int i = 0; i < 9; i++) {
            ItemStack stack = client.player.getInventory().getStack(i);
            if (stack.isIn(ItemTags.AXES) || stack.getItem().toString().toLowerCase().contains("axe")) {
               return i;
            }
         }

         return -1;
      }
   }

   public static int findMaceSlot(MinecraftClient client) {
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
