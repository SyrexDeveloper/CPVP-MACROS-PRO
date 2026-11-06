package dev.priyme.legitcrystalmacro.macro;

import dev.priyme.legitcrystalmacro.config.MacroConfig;
import dev.priyme.legitcrystalmacro.util.FallDistanceTracker;
import dev.priyme.legitcrystalmacro.util.FriendManager;
import dev.priyme.legitcrystalmacro.util.KeyInput;
import java.util.Random;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.tag.ItemTags;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult.Type;

public final class TriggerBot {
   private static final Random random = new Random();
   private static long lastAttack = 0L;
   private static long nextDelay = 0L;

   private TriggerBot() {
   }

   public static void reset() {
      lastAttack = 0L;
      nextDelay = 0L;
   }

   public static void tick(MinecraftClient client, MacroConfig config) {
      if (config == null || !config.triggerBotEnabled) {
         reset();
      } else if (client.player == null || client.world == null || client.interactionManager == null) {
         reset();
      } else if (KeyInput.isScreenNull(client)) {
         if (!config.triggerBotWeaponsOnly || isHoldingWeapon(client.player.getMainHandStack())) {
            float threshold = config.triggerBotCooldownThreshold > 0.0F ? config.triggerBotCooldownThreshold : 1.0F;
            if (!(client.player.getAttackCooldownProgress(0.0F) < threshold)) {
               if (!config.triggerBotCritOnly || canCrit(client.player)) {
                  LivingEntity target = getCrosshairTarget(client, config.triggerBotReach);
                  if (target != null && target.isAlive() && target != client.player) {
                     if (!(target instanceof PlayerEntity) || !FriendManager.isFriend(target.getName().getString())) {
                        if (!config.triggerBotPlayersOnly || target instanceof PlayerEntity) {
                           long now = System.currentTimeMillis();
                           if (now - lastAttack >= nextDelay) {
                              if (config.triggerBotHitChance < 100) {
                                 int roll = random.nextInt(100);
                                 if (roll >= config.triggerBotHitChance) {
                                    rollNextDelay(config);
                                    lastAttack = now;
                                    return;
                                 }
                              }

                              client.interactionManager.attackEntity(client.player, target);
                              client.player.swingHand(Hand.MAIN_HAND);
                              lastAttack = now;
                              rollNextDelay(config);
                           }
                        }
                     }
                  }
               }
            }
         }
      }
   }

   private static void rollNextDelay(MacroConfig config) {
      int minCps = Math.max(1, config.triggerBotMinCps);
      int maxCps = Math.max(minCps, config.triggerBotMaxCps);
      int cps = minCps + random.nextInt(maxCps - minCps + 1);
      nextDelay = Math.max((long)Math.max(0, config.triggerBotDelay), 1000L / cps);
      if (config.triggerBotRandomDelay > 0) {
         nextDelay = nextDelay + (random.nextInt(config.triggerBotRandomDelay) - config.triggerBotRandomDelay / 2L);
      }

      if (nextDelay < 10L) {
         nextDelay = 10L;
      }
   }

   private static boolean canCrit(PlayerEntity player) {
      return player.getAttackCooldownProgress(0.5F) > 0.9F
         && FallDistanceTracker.distance(player.fallDistance) > 0.0
         && !player.isSprinting()
         && player.getVelocity().y < 0.0
         && !player.isOnGround()
         && !player.isClimbing()
         && !player.isTouchingWater()
         && !player.hasStatusEffect(StatusEffects.BLINDNESS)
         && !player.hasVehicle();
   }

   private static boolean isHoldingWeapon(ItemStack stack) {
      if (stack == null || stack.isEmpty()) {
         return false;
      } else if (stack.isIn(ItemTags.SWORDS) || stack.isIn(ItemTags.AXES)) {
         return true;
      } else if (!stack.isOf(Items.MACE) && !stack.isOf(Items.TRIDENT)) {
         String name = stack.getItem().toString().toLowerCase();
         return name.contains("sword") || name.contains("axe") || name.contains("mace") || name.contains("trident") || name.contains("spear");
      } else {
         return true;
      }
   }

   private static LivingEntity getCrosshairTarget(MinecraftClient client, double maxReach) {
      if (client.crosshairTarget != null && client.crosshairTarget.getType() == Type.ENTITY) {
         EntityHitResult ehr = (EntityHitResult)client.crosshairTarget;
         if (ehr.getEntity() instanceof LivingEntity living) {
            double distSq = client.player.squaredDistanceTo(living);
            if (distSq <= maxReach * maxReach) {
               return living;
            }
         }
      }

      return null;
   }
}
