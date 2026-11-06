package dev.priyme.legitcrystalmacro.macro;

import dev.priyme.legitcrystalmacro.config.MacroConfig;
import dev.priyme.legitcrystalmacro.util.InventoryUtil;
import dev.priyme.legitcrystalmacro.util.KeyInput;
import net.minecraft.block.AbstractPressurePlateBlock;
import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.item.Items;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public final class PlateWeb {
   private static BlockPos lastPlatePos = null;
   private static long lastPlaceTime = 0L;
   private static int attemptsOnPlate = 0;
   private static World lastWorldKey = null;

   private PlateWeb() {
   }

   public static void tick(MinecraftClient client, MacroConfig config) {
      if (config == null || !config.plateWebEnabled) {
         reset();
      } else if (client.player != null && client.world != null && client.interactionManager != null) {
         if (client.player.isDead() || client.player.getHealth() <= 0.0F || client.player.isSpectator()) {
            reset();
         } else if (KeyInput.isScreenNull(client)) {
            World currentWorldKey = client.world;
            if (lastWorldKey != null && lastWorldKey != currentWorldKey) {
               reset();
            }

            lastWorldKey = currentWorldKey;
            BlockPos playerPos = client.player.getBlockPos();
            BlockState state = client.world.getBlockState(playerPos);
            if (state.getBlock() instanceof AbstractPressurePlateBlock) {
               BlockPos targetWebPos = playerPos.up();
               BlockState aboveState = client.world.getBlockState(targetWebPos);
               if (aboveState.isAir() || aboveState.isReplaceable()) {
                  long now = System.currentTimeMillis();
                  if (playerPos.equals(lastPlatePos) && now - lastPlaceTime >= Math.max(500L, (long)config.plateWebDelay)) {
                     lastPlatePos = null;
                     attemptsOnPlate = Math.max(1, attemptsOnPlate);
                  }

                  long retryDelay = Math.max((long)config.plateWebDelay, attemptsOnPlate >= 3 ? 1000L : (attemptsOnPlate > 0 ? 250L : 0L));
                  if (!playerPos.equals(lastPlatePos) && now - lastPlaceTime >= retryDelay) {
                     int webSlot = InventoryUtil.findHotbarSlot(client, Items.COBWEB);
                     if (webSlot != -1) {
                        int prevSlot = InventoryUtil.getSelectedSlot(client);
                        InventoryUtil.setSelectedSlot(client, webSlot);
                        BlockHitResult bhr = new BlockHitResult(
                           new Vec3d(playerPos.getX() + 0.5, playerPos.getY() + 0.1, playerPos.getZ() + 0.5), Direction.UP, playerPos, false
                        );
                        ActionResult result = client.interactionManager.interactBlock(client.player, Hand.MAIN_HAND, bhr);
                        int returnSlot = config.plateWebReturnSlot >= 1 && config.plateWebReturnSlot <= 9 ? config.plateWebReturnSlot - 1 : prevSlot;
                        InventoryUtil.setSelectedSlot(client, returnSlot);
                        lastPlaceTime = now;
                        if (result.isAccepted()) {
                           lastPlatePos = playerPos;
                           attemptsOnPlate = 0;
                        } else {
                           attemptsOnPlate = Math.min(3, attemptsOnPlate + 1);
                        }
                     }
                  }
               }
            } else {
               lastPlatePos = null;
               attemptsOnPlate = 0;
            }
         }
      } else {
         reset();
      }
   }

   public static void reset() {
      lastPlatePos = null;
      lastPlaceTime = 0L;
      attemptsOnPlate = 0;
      lastWorldKey = null;
   }
}
