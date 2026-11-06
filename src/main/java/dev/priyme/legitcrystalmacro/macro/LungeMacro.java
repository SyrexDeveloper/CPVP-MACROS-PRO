package dev.priyme.legitcrystalmacro.macro;

import dev.priyme.legitcrystalmacro.config.MacroConfig;
import dev.priyme.legitcrystalmacro.mixin.MinecraftClientAccessor;
import dev.priyme.legitcrystalmacro.util.FriendManager;
import dev.priyme.legitcrystalmacro.util.InventoryUtil;
import dev.priyme.legitcrystalmacro.util.KeyInput;
import java.util.Random;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult.Type;

public final class LungeMacro {
   private static boolean swapping = false;
   private static boolean triggerWasDown = false;
   private static int previousSlot = -1;
   private static int lastSelectedSpearSlot = -1;
   private static int heldTicks = 0;
   private static int holdFor = 2;
   private static long lastLunge = 0L;
   private static final Random random = new Random();

   private LungeMacro() {
   }

   public static void tick(MinecraftClient client, MacroConfig config) {
      if (config == null || !config.lungeMacroEnabled) {
         reset();
      } else if (client.player != null && client.world != null && client.interactionManager != null) {
         boolean screenOpen = !KeyInput.isScreenNull(client);
         boolean keyPress = config.lungeMacroKey > 0 && KeyInput.isPressed(client, config.lungeMacroKey);
         boolean attackPress = client.options.attackKey.isPressed();
         boolean down = !screenOpen && (keyPress || config.lungeMacroKey <= 0 && attackPress);
         boolean pressed = down && !triggerWasDown;
         triggerWasDown = down;
         if (screenOpen) {
            if (swapping) {
               if (config.lungeSwapBack && previousSlot != -1) {
                  InventoryUtil.setSelectedSlot(client, previousSlot);
               }

               reset();
            }
         } else if (swapping) {
            int currentSlot = InventoryUtil.getSelectedSlot(client);
            if (currentSlot != lastSelectedSpearSlot) {
               reset();
            } else {
               heldTicks++;
               if (heldTicks >= holdFor) {
                  if (config.lungeSwapBack && previousSlot != -1) {
                     InventoryUtil.setSelectedSlot(client, previousSlot);
                  }

                  swapping = false;
                  previousSlot = -1;
                  lastSelectedSpearSlot = -1;
               }
            }
         } else {
            boolean auto = config.lungeContinuous
               && down
               && System.currentTimeMillis() - lastLunge >= config.lungeContinuousDelay
               && client.player.getAttackCooldownProgress(0.0F) >= 1.0F;
            if (pressed || auto) {
               lunge(client, config);
            }
         }
      } else {
         reset();
      }
   }

   private static void lunge(MinecraftClient client, MacroConfig config) {
      int weaponSlot = findSpearSlot(client);
      if (weaponSlot != -1) {
         lastLunge = System.currentTimeMillis();
         int currentSlot = InventoryUtil.getSelectedSlot(client);
         boolean alreadyHolding = currentSlot == weaponSlot;
         if (!alreadyHolding) {
            previousSlot = currentSlot;
            lastSelectedSpearSlot = weaponSlot;
            holdFor = Math.max(2, config.lungeSwapBackDelay);
            if (config.lungeRandomize) {
               holdFor = holdFor + random.nextInt(2);
            }

            InventoryUtil.setSelectedSlot(client, weaponSlot);
            swapping = true;
            heldTicks = 0;
         }

         try {
            if (client instanceof MinecraftClientAccessor accessor) {
               accessor.setAttackCooldown(0);
               accessor.invokeDoAttack();
            }
         } catch (Throwable var8) {
         }

         Entity target = getTarget(client);
         if (target != null && target.isAlive()) {
            try {
               client.interactionManager.attackEntity(client.player, target);
            } catch (Throwable var7) {
            }
         }

         client.player.swingHand(Hand.MAIN_HAND);
      }
   }

   private static Entity getTarget(MinecraftClient client) {
      if (client.crosshairTarget != null && client.crosshairTarget.getType() == Type.ENTITY) {
         EntityHitResult ehr = (EntityHitResult)client.crosshairTarget;
         if (ehr.getEntity() instanceof LivingEntity living
            && living.isAlive()
            && !living.isRemoved()
            && living != client.player
            && (!(living instanceof PlayerEntity) || !FriendManager.isFriend(living.getName().getString()))) {
            return living;
         }
      }

      return findTargetInReach(client, 4.5F);
   }

   private static Entity findTargetInReach(MinecraftClient client, float reach) {
      if (client.player != null && client.world != null) {
         double minDistanceSq = reach * reach;
         Entity best = null;

         for (Entity entity : client.world.getOtherEntities(client.player, client.player.getBoundingBox().expand(reach))) {
            if (entity instanceof LivingEntity living
               && living.isAlive()
               && !living.isRemoved()
               && living != client.player
               && (!(living instanceof PlayerEntity) || !FriendManager.isFriend(living.getName().getString()))) {
               double distSq = client.player.squaredDistanceTo(living);
               if (distSq <= minDistanceSq) {
                  minDistanceSq = distSq;
                  best = living;
               }
            }
         }

         return best;
      } else {
         return null;
      }
   }

   private static int getSpearTier(ItemStack stack) {
      if (!isSpear(stack)) {
         return -1;
      } else {
         String name = stack.getItem().toString().toLowerCase();
         if (name.contains("netherite")) {
            return 6;
         } else if (name.contains("diamond")) {
            return 5;
         } else if (name.contains("iron")) {
            return 4;
         } else if (name.contains("golden") || name.contains("gold")) {
            return 3;
         } else if (name.contains("copper")) {
            return 2;
         } else {
            return name.contains("stone") ? 1 : 0;
         }
      }
   }

   private static int findSpearSlot(MinecraftClient client) {
      if (client.player == null) {
         return -1;
      } else {
         ItemStack current = client.player.getMainHandStack();
         if (isSpear(current)) {
            return InventoryUtil.getSelectedSlot(client);
         } else {
            int bestSlot = -1;
            int bestTier = -1;

            for (int i = 0; i < 9; i++) {
               ItemStack stack = client.player.getInventory().getStack(i);
               int tier = getSpearTier(stack);
               if (tier > bestTier) {
                  bestTier = tier;
                  bestSlot = i;
               }
            }

            return bestSlot;
         }
      }
   }

   public static boolean isSpear(ItemStack stack) {
      if (stack != null && !stack.isEmpty()) {
         String name = stack.getItem().toString().toLowerCase();
         String translationKey = stack.getItem().getTranslationKey().toLowerCase();
         String customName = stack.getName().getString().toLowerCase();
         return name.contains("spear") || translationKey.contains("spear") || customName.contains("spear");
      } else {
         return false;
      }
   }

   private static void reset() {
      swapping = false;
      triggerWasDown = false;
      previousSlot = -1;
      lastSelectedSpearSlot = -1;
      heldTicks = 0;
   }
}
