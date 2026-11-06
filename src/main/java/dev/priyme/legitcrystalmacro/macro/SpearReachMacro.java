package dev.priyme.legitcrystalmacro.macro;

import dev.priyme.legitcrystalmacro.config.MacroConfig;
import dev.priyme.legitcrystalmacro.mixin.MinecraftClientAccessor;
import dev.priyme.legitcrystalmacro.util.FriendManager;
import dev.priyme.legitcrystalmacro.util.InventoryUtil;
import dev.priyme.legitcrystalmacro.util.KeyInput;
import net.minecraft.client.MinecraftClient;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.AttackRangeComponent;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.tag.ItemTags;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;

public final class SpearReachMacro {
   private static boolean swapping;
   private static int previousSlot = -1;
   private static int lastSelectedSlot = -1;
   private static int primedSwordSlot = -1;
   private static int heldTicks;
   private static long lastAttackTime;
   private static Object lastPlayer;
   private static Object lastWorld;

   private SpearReachMacro() {
   }

   public static void reset() {
      swapping = false;
      primedSwordSlot = -1;
      lastSelectedSlot = -1;
      previousSlot = -1;
      heldTicks = 0;
   }

   public static void tick(MinecraftClient client, MacroConfig config) {
      if (client.player != lastPlayer || client.world != lastWorld) {
         reset();
         lastPlayer = client.player;
         lastWorld = client.world;
      }

      if (client.player == null || client.world == null || client.interactionManager == null || !client.player.isAlive() || client.player.isSpectator()) {
         reset();
      } else if (config == null || !config.spearReachEnabled || !KeyInput.isScreenNull(client) || client.player.isUsingItem()) {
         finishSwap(client, config != null && config.spearReachSwitchBack);
      } else if (swapping) {
         if (InventoryUtil.getSelectedSlot(client) != lastSelectedSlot) {
            reset();
         } else if (++heldTicks >= 2) {
            finishSwap(client, config.spearReachSwitchBack);
         }
      } else {
         int swordSlot = InventoryUtil.getSelectedSlot(client);
         if (!client.player.getMainHandStack().isIn(ItemTags.SWORDS)) {
            primedSwordSlot = -1;
         } else if (primedSwordSlot != swordSlot) {
            primedSwordSlot = swordSlot;
         } else {
            boolean active = config.spearReachAuto
               || (config.spearReachKey > 0 ? KeyInput.isPressed(client, config.spearReachKey) : client.options.attackKey.isPressed());
            if (active && !(client.player.getAttackCooldownProgress(0.0F) < 1.0F)) {
               long now = System.currentTimeMillis();
               if (now < lastAttackTime || now - lastAttackTime >= config.spearReachDelay) {
                  int spearSlot = findSpearSlot(client);
                  if (spearSlot >= 0) {
                     ItemStack spear = client.player.getInventory().getStack(spearSlot);
                     if (findTargetInSpearReach(client, config, spear) != null && client instanceof MinecraftClientAccessor accessor) {
                        previousSlot = swordSlot;
                        lastSelectedSlot = spearSlot;
                        heldTicks = 0;
                        swapping = true;
                        InventoryUtil.setSelectedSlot(client, spearSlot);
                        boolean attacked = false;

                        try {
                           attacked = accessor.invokeDoAttack();
                        } finally {
                           if (!attacked) {
                              finishSwap(client, true);
                           }
                        }

                        if (attacked) {
                           lastAttackTime = now;
                        }
                     }
                  }
               }
            }
         }
      }
   }

   private static void finishSwap(MinecraftClient client, boolean restore) {
      if (restore && swapping && previousSlot >= 0 && InventoryUtil.getSelectedSlot(client) == lastSelectedSlot) {
         InventoryUtil.setSelectedSlot(client, previousSlot);
      }

      reset();
   }

   public static double getDistanceToEntity(PlayerEntity player, Entity target, int distanceMode) {
      if (player == null || target == null) {
         return Double.MAX_VALUE;
      } else if (distanceMode == 0) {
         double dx = player.getX() - target.getX();
         double dz = player.getZ() - target.getZ();
         return Math.sqrt(dx * dx + dz * dz);
      } else if (distanceMode == 1) {
         return player.getEyePos().distanceTo(target.getEyePos());
      } else {
         Vec3d eye = player.getEyePos();
         Box bb = target.getBoundingBox();
         return eye.distanceTo(
            new Vec3d(Math.max(bb.minX, Math.min(eye.x, bb.maxX)), Math.max(bb.minY, Math.min(eye.y, bb.maxY)), Math.max(bb.minZ, Math.min(eye.z, bb.maxZ)))
         );
      }
   }

   private static LivingEntity findTargetInSpearReach(MinecraftClient client, MacroConfig config, ItemStack spear) {
      AttackRangeComponent range = (AttackRangeComponent)spear.get(DataComponentTypes.ATTACK_RANGE);
      if (range == null) {
         return null;
      } else if (!(
         range.getHitResult(
               client.player,
               1.0F,
               e -> e instanceof LivingEntity living && living.isAlive() && !living.isRemoved() && !living.isSpectator() && e != client.player
            ) instanceof EntityHitResult entityHit
            && entityHit.getEntity() instanceof LivingEntity target
      )) {
         return null;
      } else if (target instanceof PlayerEntity && FriendManager.isFriend(target.getName().getString())) {
         return null;
      } else {
         return getDistanceToEntity(client.player, target, config.spearReachDistanceMode) <= config.spearReachDistance ? target : null;
      }
   }

   private static int findSpearSlot(MinecraftClient client) {
      for (int i = 0; i < 9; i++) {
         if (client.player.getInventory().getStack(i).isIn(ItemTags.SPEARS)) {
            return i;
         }
      }

      return -1;
   }
}
