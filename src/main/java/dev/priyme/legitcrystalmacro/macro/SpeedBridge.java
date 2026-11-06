package dev.priyme.legitcrystalmacro.macro;

import dev.priyme.legitcrystalmacro.config.MacroConfig;
import dev.priyme.legitcrystalmacro.util.KeyInput;
import net.minecraft.client.MinecraftClient;
import net.minecraft.util.math.BlockPos;

public final class SpeedBridge {
   private static boolean sneaked;
   private static long edgeAt;

   private SpeedBridge() {
   }

   public static void reset(MinecraftClient client) {
      if (sneaked && client.options != null) {
         client.options.sneakKey.setPressed(false);
      }

      sneaked = false;
      edgeAt = 0L;
   }

   public static void tick(MinecraftClient client, MacroConfig config) {
      if (config != null && config.movementSpeedBridgeEnabled && client.player != null && client.world != null && KeyInput.isScreenNull(client)) {
         double forward = (client.options.forwardKey.isPressed() ? 1 : 0) - (client.options.backKey.isPressed() ? 1 : 0);
         double side = (client.options.leftKey.isPressed() ? 1 : 0) - (client.options.rightKey.isPressed() ? 1 : 0);
         if (client.player.isOnGround() && (forward != 0.0 || side != 0.0)) {
            double yaw = Math.toRadians(client.player.getYaw());
            double dx = -Math.sin(yaw) * forward + Math.cos(yaw) * side;
            double dz = Math.cos(yaw) * forward + Math.sin(yaw) * side;
            double length = Math.hypot(dx, dz);
            double probe = 0.32 / Math.max(1.0, length);
            BlockPos ahead = BlockPos.ofFloored(client.player.getX() + dx * probe, client.player.getY() - 0.1, client.player.getZ() + dz * probe);
            boolean edge = client.world.getBlockState(ahead).getCollisionShape(client.world, ahead).isEmpty();
            long now = System.currentTimeMillis();
            if (edge) {
               edgeAt = now;
               if (!client.options.sneakKey.isPressed()) {
                  client.options.sneakKey.setPressed(true);
                  sneaked = true;
               }
            } else if (sneaked && now - edgeAt >= Math.max(0, config.movementSpeedBridgeDelay)) {
               reset(client);
            }
         } else {
            reset(client);
         }
      } else {
         reset(client);
      }
   }
}
