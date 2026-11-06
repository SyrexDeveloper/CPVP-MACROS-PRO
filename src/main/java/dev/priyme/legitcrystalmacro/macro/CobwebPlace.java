package dev.priyme.legitcrystalmacro.macro;

import dev.priyme.legitcrystalmacro.config.MacroConfig;
import dev.priyme.legitcrystalmacro.util.InventoryUtil;
import dev.priyme.legitcrystalmacro.util.KeyInput;
import net.minecraft.client.MinecraftClient;
import net.minecraft.item.Items;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;

public final class CobwebPlace {
   private static long lastPlace = 0L;
   private static boolean wasDown = false;

   private CobwebPlace() {
   }

   public static void tick(MinecraftClient client, MacroConfig config) {
      if (config != null && config.cobwebPlaceEnabled) {
         if (client.player != null && client.world != null && client.interactionManager != null) {
            boolean isDown = config.cobwebPlaceKey > 0 && KeyInput.isPressed(client, config.cobwebPlaceKey);
            boolean trigger = config.cobwebPlaceKey > 0 ? isDown && !wasDown : false;
            wasDown = isDown;
            if (trigger) {
               long now = System.currentTimeMillis();
               if (now - lastPlace >= config.cobwebPlaceDelay) {
                  BlockPos pos = client.player.getBlockPos();
                  if (client.world.getBlockState(pos).isAir()) {
                     int webSlot = InventoryUtil.findHotbarSlot(client, Items.COBWEB);
                     if (webSlot != -1) {
                        int prev = InventoryUtil.getSelectedSlot(client);
                        InventoryUtil.setSelectedSlot(client, webSlot);
                        BlockHitResult bhr = new BlockHitResult(new Vec3d(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5), Direction.UP, pos.down(), false);
                        client.interactionManager.interactBlock(client.player, Hand.MAIN_HAND, bhr);
                        int returnSlot = config.cobwebReturnSlot >= 1 && config.cobwebReturnSlot <= 9 ? config.cobwebReturnSlot - 1 : prev;
                        InventoryUtil.setSelectedSlot(client, returnSlot);
                        lastPlace = now;
                     }
                  }
               }
            }
         }
      }
   }
}
