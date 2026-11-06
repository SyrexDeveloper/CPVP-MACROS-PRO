package dev.priyme.legitcrystalmacro.macro;

import dev.priyme.legitcrystalmacro.config.MacroConfig;
import dev.priyme.legitcrystalmacro.util.InventoryUtil;
import dev.priyme.legitcrystalmacro.util.KeyInput;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import net.minecraft.entity.projectile.AbstractWindChargeEntity;
import net.minecraft.item.Items;
import net.minecraft.util.Hand;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;

public final class WindChargeMacro {
   private static boolean wasKeyDown = false;
   private static int previousSlot = -1;
   private static long lastUseTime = 0L;
   private static int stage = 0;
   private static float originalPitch = 0.0F;
   private static int handledWindChargeId = -1;

   private WindChargeMacro() {
   }

   public static void reset() {
      wasKeyDown = false;
      previousSlot = -1;
      stage = 0;
      handledWindChargeId = -1;
   }

   public static void tick(MinecraftClient client, MacroConfig config) {
      if (config == null) {
         reset();
      } else if (client.player != null && client.world != null && client.interactionManager != null) {
         if (config.perfectWindChargeEnabled) {
            tickPerfectWindcharge(client, config);
         }

         if (config.keyWindChargeEnabled) {
            tickKeyWindcharge(client, config);
         }
      } else {
         reset();
      }
   }

   private static void tickKeyWindcharge(MinecraftClient client, MacroConfig config) {
      if (!KeyInput.isScreenNull(client)) {
         if (stage != 0 && previousSlot != -1 && config.keyWindChargeSwitchBack) {
            InventoryUtil.setSelectedSlot(client, previousSlot);
         }

         stage = 0;
         previousSlot = -1;
      } else {
         long now = System.currentTimeMillis();
         boolean isDown = config.keyWindChargeKey > 0 && KeyInput.isPressed(client, config.keyWindChargeKey);
         boolean pressed = isDown && !wasKeyDown;
         wasKeyDown = isDown;
         if (stage == 0) {
            if (!pressed) {
               return;
            }

            if (now - lastUseTime < config.keyWindChargeCooldown) {
               return;
            }

            int windSlot = InventoryUtil.findHotbarSlot(client, Items.WIND_CHARGE);
            if (windSlot == -1) {
               return;
            }

            previousSlot = InventoryUtil.getSelectedSlot(client);
            originalPitch = client.player.getPitch();
            client.player.setPitch(90.0F);
            InventoryUtil.setSelectedSlot(client, windSlot);
            client.interactionManager.interactItem(client.player, Hand.MAIN_HAND);
            client.player.swingHand(Hand.MAIN_HAND);
            lastUseTime = now;
            stage = 1;
         } else if (stage == 1 && now - lastUseTime >= config.keyWindChargeJumpDelay) {
            if (client.player.isOnGround()) {
               client.player.jump();
            }

            if (config.keyWindChargeRestorePitch) {
               client.player.setPitch(originalPitch);
            }

            if (config.keyWindChargeSwitchBack && previousSlot != -1) {
               InventoryUtil.setSelectedSlot(client, previousSlot);
               previousSlot = -1;
            }

            stage = 0;
         }
      }
   }

   private static void tickPerfectWindcharge(MinecraftClient client, MacroConfig config) {
      double range = config.perfectWindChargeTriggerRange;
      Box box = client.player.getBoundingBox().expand(range);

      for (Entity e : client.world.getOtherEntities(client.player, box)) {
         boolean isWindCharge = e instanceof AbstractWindChargeEntity || e.getType().toString().toLowerCase().contains("wind_charge");
         if (isWindCharge && e.getId() != handledWindChargeId) {
            Vec3d playerPos = new Vec3d(client.player.getX(), client.player.getY(), client.player.getZ());
            Vec3d ePos = new Vec3d(e.getX(), e.getY(), e.getZ());
            double dy = Math.abs(playerPos.y - ePos.y);
            double distSq = playerPos.squaredDistanceTo(ePos);
            if (distSq <= range * range && dy <= 1.5 && (client.player.isOnGround() || client.player.getVelocity().y <= 0.0)) {
               client.player.jump();
               handledWindChargeId = e.getId();
               break;
            }
         }
      }
   }
}
