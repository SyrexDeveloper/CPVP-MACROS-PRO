package dev.priyme.legitcrystalmacro.macro;

import dev.priyme.legitcrystalmacro.config.MacroConfig;
import dev.priyme.legitcrystalmacro.util.KeyInput;
import net.minecraft.client.MinecraftClient;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.math.Vec3d;

public final class SpearLunge {
   private static boolean wasPressed = false;
   private static long lastLungeTime = 0L;

   private SpearLunge() {
   }

   public static void tick(MinecraftClient client, MacroConfig config) {
      if (config != null && client.player != null && client.world != null) {
         boolean isPressed = config.spearLungeKey > 0 && KeyInput.isPressed(client, config.spearLungeKey);
         if (config.spearLungeEnabled && KeyInput.isScreenNull(client)) {
            boolean triggered = isPressed && !wasPressed;
            wasPressed = isPressed;
            if (triggered) {
               if (client.player.isAlive() && !client.player.isSpectator() && !client.player.hasVehicle()) {
                  if (!(client.player.getHungerManager().getFoodLevel() < config.spearLungeMinHunger)) {
                     ItemStack mainHand = client.player.getMainHandStack();
                     boolean isSpear = mainHand.isOf(Items.TRIDENT) || LungeMacro.isSpear(mainHand);
                     if (isSpear) {
                        long now = System.currentTimeMillis();
                        if (now < lastLungeTime) {
                           lastLungeTime = 0L;
                        }

                        if (now - lastLungeTime >= config.spearLungeCooldown) {
                           double horizontalPower = config.spearLungePower;
                           double verticalPower = config.spearLungeUpwardPower;
                           if (Double.isFinite(horizontalPower) && Double.isFinite(verticalPower)) {
                              Vec3d look = client.player.getRotationVec(1.0F);
                              if (look != null) {
                                 double lookLengthSq = look.x * look.x + look.y * look.y + look.z * look.z;
                                 if (lookLengthSq > 1.0E-6) {
                                    look = look.normalize();
                                 }

                                 Vec3d impulse = new Vec3d(look.x * horizontalPower, verticalPower, look.z * horizontalPower);
                                 if (Double.isFinite(impulse.x) && Double.isFinite(impulse.y) && Double.isFinite(impulse.z)) {
                                    Vec3d currentVelocity = client.player.getVelocity();
                                    if (currentVelocity == null
                                       || !Double.isFinite(currentVelocity.x)
                                       || !Double.isFinite(currentVelocity.y)
                                       || !Double.isFinite(currentVelocity.z)) {
                                       currentVelocity = Vec3d.ZERO;
                                    }

                                    Vec3d targetVelocity = currentVelocity.add(impulse);
                                    if (Double.isFinite(targetVelocity.x) && Double.isFinite(targetVelocity.y) && Double.isFinite(targetVelocity.z)) {
                                       client.player.setVelocity(targetVelocity);
                                       lastLungeTime = now;
                                    }
                                 }
                              }
                           }
                        }
                     }
                  }
               }
            }
         } else {
            wasPressed = isPressed;
         }
      } else {
         reset();
      }
   }

   public static void reset() {
      wasPressed = false;
   }
}
