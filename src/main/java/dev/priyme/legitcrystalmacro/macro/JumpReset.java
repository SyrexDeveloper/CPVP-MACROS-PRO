package dev.priyme.legitcrystalmacro.macro;

import dev.priyme.legitcrystalmacro.config.MacroConfig;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.MathHelper;

public final class JumpReset {
   private static int cooldown;
   private static int lastHurtTime;
   private static boolean pendingDamage;
   private static boolean delayedJump;
   private static LivingEntity attacker;
   private static PlayerEntity player;

   private JumpReset() {
   }

   public static void reset() {
      lastHurtTime = 0;
      cooldown = 0;
      delayedJump = false;
      pendingDamage = false;
      attacker = null;
      player = null;
   }

   public static void damaged(DamageSource source) {
      MacroConfig config = MacroConfig.get();
      if (config != null && config.jumpResetEnabled) {
         Entity entity = source == null ? null : source.getAttacker();
         if (entity == null && source != null) {
            entity = source.getSource();
         }

         attacker = entity instanceof LivingEntity living ? living : null;
         pendingDamage = true;
      }
   }

   public static boolean wantsJump(MinecraftClient client) {
      MacroConfig config = MacroConfig.get();
      if (client.player != player) {
         reset();
         player = client.player;
      }

      if (config != null
         && config.jumpResetEnabled
         && client.player != null
         && client.world != null
         && client.currentScreen == null
         && client.isWindowFocused()
         && client.player.isAlive()) {
         if (cooldown > 0) {
            cooldown--;
         }

         int hurtTime = client.player.hurtTime;
         if (hurtTime >= 9 && lastHurtTime == 0) {
            pendingDamage = true;
         }

         lastHurtTime = hurtTime;
         boolean jump = delayedJump;
         delayedJump = false;
         if (pendingDamage) {
            LivingEntity target = attacker;
            if (target == null) {
               target = client.player.getAttacker();
            }

            if (target == null) {
               target = client.player.getAttacking();
            }

            if (target == null) {
               target = nearestPlayer(client);
            }

            pendingDamage = false;
            attacker = null;
            if (cooldown == 0 && eligible(client, target, config) && Math.random() * 100.0 < config.jumpResetChance) {
               cooldown = config.jumpResetCooldown;
               if (config.jumpResetMode == 1) {
                  delayedJump = true;
                  jump = false;
               } else {
                  jump = true;
               }
            } else {
               jump = false;
            }
         }

         return jump && client.player.isOnGround() && !client.player.isUsingItem();
      } else {
         reset();
         return false;
      }
   }

   private static boolean eligible(MinecraftClient client, LivingEntity target, MacroConfig config) {
      if (!client.player.isOnGround() || client.player.isUsingItem() || target == client.player) {
         return false;
      } else if (config.jumpResetPlayersOnly && !(target instanceof PlayerEntity)) {
         return false;
      } else if (!config.jumpResetFacingOnly) {
         return true;
      } else if (target == null) {
         return false;
      } else {
         double dx = target.getX() - client.player.getX();
         double dz = target.getZ() - client.player.getZ();
         return facing(client.player.getYaw(), dx, dz);
      }
   }

   static boolean facing(float yaw, double dx, double dz) {
      if (dx * dx + dz * dz < 1.0E-6) {
         return true;
      } else {
         float targetYaw = (float)(Math.toDegrees(Math.atan2(dz, dx)) - 90.0);
         return Math.abs(MathHelper.wrapDegrees(targetYaw - yaw)) <= 90.0F;
      }
   }

   private static PlayerEntity nearestPlayer(MinecraftClient client) {
      PlayerEntity nearest = null;
      double distance = 36.0;

      for (PlayerEntity candidate : client.world.getPlayers()) {
         if (candidate != client.player && candidate.isAlive()) {
            double squared = client.player.squaredDistanceTo(candidate);
            if (squared < distance) {
               distance = squared;
               nearest = candidate;
            }
         }
      }

      return nearest;
   }
}
