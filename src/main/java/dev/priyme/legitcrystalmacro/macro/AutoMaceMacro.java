package dev.priyme.legitcrystalmacro.macro;

import dev.priyme.legitcrystalmacro.config.MacroConfig;
import dev.priyme.legitcrystalmacro.util.FallDistanceTracker;
import dev.priyme.legitcrystalmacro.util.FriendManager;
import dev.priyme.legitcrystalmacro.util.InventoryUtil;
import dev.priyme.legitcrystalmacro.util.KeyInput;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.Hand;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;

public final class AutoMaceMacro {
   private static boolean inSmash = false;
   private static int previousSlot = -1;
   private static long lastSmashTime = 0L;
   private static int targetEntityId = -1;

   private AutoMaceMacro() {
   }

   public static void reset() {
      inSmash = false;
      previousSlot = -1;
      targetEntityId = -1;
   }

   public static void tick(MinecraftClient client, MacroConfig config) {
      if (config != null && config.autoMaceEnabled) {
         if (client.player == null || client.world == null || client.interactionManager == null) {
            reset();
         } else if (!KeyInput.isScreenNull(client)) {
            if (inSmash && config.autoMaceSwitchBack && previousSlot != -1) {
               InventoryUtil.setSelectedSlot(client, previousSlot);
            }

            reset();
         } else {
            long now = System.currentTimeMillis();
            if (inSmash) {
               if (now - lastSmashTime >= config.autoMaceSwitchDelay) {
                  if (config.autoMaceSwitchBack && previousSlot != -1) {
                     InventoryUtil.setSelectedSlot(client, previousSlot);
                  }

                  inSmash = false;
                  previousSlot = -1;
                  targetEntityId = -1;
               }
            } else {
               boolean isFalling = FallDistanceTracker.distance(client.player.fallDistance) >= config.autoMaceMinFallDistance
                  && client.player.getVelocity().y < -0.05;
               if (isFalling) {
                  if (now - lastSmashTime >= config.autoMaceCooldown) {
                     if (!(client.player.getAttackCooldownProgress(0.0F) < config.autoMaceCooldownThreshold)) {
                        LivingEntity target = findSmashTarget(client, config);
                        if (target != null) {
                           int maceSlot = findMaceSlot(client);
                           if (maceSlot != -1) {
                              int currentSlot = InventoryUtil.getSelectedSlot(client);
                              previousSlot = currentSlot;
                              if (currentSlot != maceSlot) {
                                 InventoryUtil.setSelectedSlot(client, maceSlot);
                              }

                              client.interactionManager.attackEntity(client.player, target);
                              client.player.swingHand(Hand.MAIN_HAND);
                              inSmash = true;
                              lastSmashTime = now;
                              targetEntityId = target.getId();
                           }
                        }
                     }
                  }
               }
            }
         }
      } else {
         if (client.player != null && inSmash && config != null && config.autoMaceSwitchBack && previousSlot != -1) {
            InventoryUtil.setSelectedSlot(client, previousSlot);
         }

         reset();
      }
   }

   private static LivingEntity findSmashTarget(MinecraftClient client, MacroConfig config) {
      if (client.player != null && client.world != null) {
         double reach = config.autoMaceReach;
         Box box = client.player.getBoundingBox().expand(reach);
         Vec3d eyePos = client.player.getEyePos();
         Vec3d lookVec = client.player.getRotationVec(1.0F);
         LivingEntity bestTarget = null;
         double bestScore = Double.MAX_VALUE;

         for (Entity e : client.world.getOtherEntities(client.player, box)) {
            if (e instanceof LivingEntity living
               && living.isAlive()
               && e != client.player
               && (!(e instanceof PlayerEntity) || !FriendManager.isFriend(e.getName().getString()))) {
               double distSq = client.player.squaredDistanceTo(living);
               if (!(distSq > reach * reach)) {
                  Vec3d toTarget = living.getEyePos().subtract(eyePos).normalize();
                  double dot = lookVec.dotProduct(toTarget);
                  if (config.autoMaceFov < 360.0) {
                     double angle = Math.toDegrees(Math.acos(Math.max(-1.0, Math.min(1.0, dot))));
                     if (angle > config.autoMaceFov / 2.0) {
                        continue;
                     }
                  }

                  if (distSq < bestScore) {
                     bestScore = distSq;
                     bestTarget = living;
                  }
               }
            }
         }

         return bestTarget;
      } else {
         return null;
      }
   }

   private static int findMaceSlot(MinecraftClient client) {
      if (client.player == null) {
         return -1;
      } else {
         for (int i = 0; i < 9; i++) {
            ItemStack stack = client.player.getInventory().getStack(i);
            if (stack != null && !stack.isEmpty() && (stack.isOf(Items.MACE) || stack.getItem().toString().toLowerCase().contains("mace"))) {
               return i;
            }
         }

         return -1;
      }
   }
}
