package dev.priyme.legitcrystalmacro.macro;

import dev.priyme.legitcrystalmacro.config.MacroConfig;
import dev.priyme.legitcrystalmacro.util.FriendManager;
import dev.priyme.legitcrystalmacro.util.InventoryUtil;
import dev.priyme.legitcrystalmacro.util.KeyInput;
import net.minecraft.client.MinecraftClient;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.ItemEnchantmentsComponent;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.registry.tag.ItemTags;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult.Type;

public final class MaceSwapMacro {
   private static boolean swapping = false;
   private static int previousSlot = -1;
   private static long swapTime = 0L;
   private static boolean wasTriggerDown = false;

   private MaceSwapMacro() {
   }

   public static void reset() {
      swapping = false;
      previousSlot = -1;
      swapTime = 0L;
      wasTriggerDown = false;
   }

   public static void tick(MinecraftClient client, MacroConfig config) {
      if (config != null && config.maceSwapEnabled) {
         if (client.player == null || client.world == null || client.interactionManager == null) {
            reset();
         } else if (!KeyInput.isScreenNull(client)) {
            if (swapping && config.maceSwapSwitchBack && previousSlot != -1) {
               InventoryUtil.setSelectedSlot(client, previousSlot);
            }

            reset();
         } else {
            long now = System.currentTimeMillis();
            if (swapping) {
               if (now - swapTime >= config.maceSwapSwitchDelay) {
                  if (config.maceSwapSwitchBack && previousSlot != -1) {
                     InventoryUtil.setSelectedSlot(client, previousSlot);
                  }

                  swapping = false;
                  previousSlot = -1;
               }
            } else {
               boolean triggerDown = config.maceSwapAuto
                  || config.maceSwapKey > 0 && KeyInput.isPressed(client, config.maceSwapKey)
                  || client.options.attackKey.isPressed();
               boolean triggered = triggerDown && !wasTriggerDown;
               wasTriggerDown = triggerDown;
               boolean canTrigger = config.maceSwapAuto ? triggerDown : triggered;
               if (canTrigger) {
                  if (!(config.maceSwapMinFallDistance > 0.0) || !(client.player.fallDistance < config.maceSwapMinFallDistance)) {
                     LivingEntity target = getTarget(client, config.maceSwapReach);
                     if (target != null && target.isAlive() && target != client.player) {
                        if (!FriendManager.isFriend(target.getName().getString())) {
                           int maceSlot = findBestMaceSlot(client, config, target);
                           if (maceSlot != -1) {
                              int currentSlot = InventoryUtil.getSelectedSlot(client);
                              if (currentSlot == maceSlot) {
                                 client.interactionManager.attackEntity(client.player, target);
                                 client.player.swingHand(Hand.MAIN_HAND);
                              } else {
                                 if (config.maceSwapStunSlam && target.isBlocking()) {
                                    int axeSlot = InventoryUtil.findHotbarSlotMatching(
                                       client, s -> s.isIn(ItemTags.AXES) || s.getItem().toString().toLowerCase().contains("axe")
                                    );
                                    if (axeSlot != -1 && axeSlot != currentSlot) {
                                       InventoryUtil.setSelectedSlot(client, axeSlot);
                                       client.interactionManager.attackEntity(client.player, target);
                                       client.player.swingHand(Hand.MAIN_HAND);
                                    }
                                 }

                                 previousSlot = currentSlot;
                                 InventoryUtil.setSelectedSlot(client, maceSlot);
                                 client.interactionManager.attackEntity(client.player, target);
                                 client.player.swingHand(Hand.MAIN_HAND);
                                 swapping = true;
                                 swapTime = now;
                              }
                           }
                        }
                     }
                  }
               }
            }
         }
      } else {
         if (client.player != null && swapping && config != null && config.maceSwapSwitchBack && previousSlot != -1) {
            InventoryUtil.setSelectedSlot(client, previousSlot);
         }

         reset();
      }
   }

   private static LivingEntity getTarget(MinecraftClient client, double reach) {
      if (client.crosshairTarget != null && client.crosshairTarget.getType() == Type.ENTITY) {
         EntityHitResult ehr = (EntityHitResult)client.crosshairTarget;
         if (ehr.getEntity() instanceof LivingEntity living && client.player.squaredDistanceTo(living) <= reach * reach) {
            return living;
         }
      }

      double bestDistSq = reach * reach;
      LivingEntity best = null;

      for (Entity e : client.world.getOtherEntities(client.player, client.player.getBoundingBox().expand(reach))) {
         if (e instanceof LivingEntity living
            && living.isAlive()
            && e != client.player
            && (!(e instanceof PlayerEntity) || !FriendManager.isFriend(e.getName().getString()))) {
            double dSq = client.player.squaredDistanceTo(living);
            if (dSq <= bestDistSq) {
               bestDistSq = dSq;
               best = living;
            }
         }
      }

      return best;
   }

   private static int findBestMaceSlot(MinecraftClient client, MacroConfig config, LivingEntity target) {
      if (client.player == null) {
         return -1;
      } else {
         int bestSlot = -1;
         int highestScore = -1;

         for (int i = 0; i < 9; i++) {
            ItemStack stack = client.player.getInventory().getStack(i);
            if (stack != null && !stack.isEmpty() && isMace(stack)) {
               int score = 1;
               if (config.maceSwapBreachPriority && target != null && hasBreachEnchant(stack)) {
                  score += 10;
               }

               if (hasDensityEnchant(stack)) {
                  score += 5;
               }

               if (score > highestScore) {
                  highestScore = score;
                  bestSlot = i;
               }
            }
         }

         return bestSlot;
      }
   }

   private static boolean isMace(ItemStack stack) {
      if (stack == null || stack.isEmpty()) {
         return false;
      } else if (stack.isOf(Items.MACE)) {
         return true;
      } else {
         String name = stack.getItem().toString().toLowerCase();
         return name.contains("mace");
      }
   }

   private static boolean hasBreachEnchant(ItemStack stack) {
      try {
         ItemEnchantmentsComponent enchants = (ItemEnchantmentsComponent)stack.get(DataComponentTypes.ENCHANTMENTS);
         if (enchants == null) {
            return false;
         }

         for (RegistryEntry<Enchantment> e : enchants.getEnchantments()) {
            String id = e.getIdAsString().toLowerCase();
            if (id.contains("breach")) {
               return true;
            }
         }
      } catch (Throwable var5) {
      }

      return false;
   }

   private static boolean hasDensityEnchant(ItemStack stack) {
      try {
         ItemEnchantmentsComponent enchants = (ItemEnchantmentsComponent)stack.get(DataComponentTypes.ENCHANTMENTS);
         if (enchants == null) {
            return false;
         }

         for (RegistryEntry<Enchantment> e : enchants.getEnchantments()) {
            String id = e.getIdAsString().toLowerCase();
            if (id.contains("density")) {
               return true;
            }
         }
      } catch (Throwable var5) {
      }

      return false;
   }
}
