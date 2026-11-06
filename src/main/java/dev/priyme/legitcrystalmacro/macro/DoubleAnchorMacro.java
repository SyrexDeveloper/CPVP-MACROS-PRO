package dev.priyme.legitcrystalmacro.macro;

import dev.priyme.legitcrystalmacro.config.MacroConfig;
import dev.priyme.legitcrystalmacro.util.InventoryUtil;
import dev.priyme.legitcrystalmacro.util.KeyInput;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.RespawnAnchorBlock;
import net.minecraft.client.MinecraftClient;
import net.minecraft.item.Items;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult.Type;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;

public final class DoubleAnchorMacro {
   private static long lastTime;
   private static Object world;
   private static BlockPos first;
   private static BlockPos second;
   private static int stage;
   private static boolean held;

   public static void reset() {
      first = null;
      second = null;
      stage = 0;
      held = false;
      lastTime = 0L;
      world = null;
   }

   public static void tick(MinecraftClient client, MacroConfig config) {
      if (config != null
         && config.doubleAnchorEnabled
         && client.player != null
         && client.world != null
         && client.interactionManager != null
         && KeyInput.isScreenNull(client)) {
         if (world != client.world) {
            reset();
            world = client.world;
         }

         boolean down = client.options.useKey.isPressed() || config.doubleAnchorActivateKey > 0 && KeyInput.isPressed(client, config.doubleAnchorActivateKey);
         if (!down) {
            first = null;
            second = null;
            stage = 0;
            held = false;
         } else {
            long now = System.currentTimeMillis();
            if (now - lastTime >= config.doubleAnchorSwitchDelay * 50L) {
               if (first == null) {
                  if (!(client.crosshairTarget instanceof BlockHitResult hit) || hit.getType() != Type.BLOCK) {
                     return;
                  }

                  BlockPos pos = hit.getBlockPos();
                  if (client.world.getBlockState(pos).isOf(Blocks.RESPAWN_ANCHOR)) {
                     charge(client, config, pos);
                     lastTime = now;
                     return;
                  }

                  if (held) {
                     return;
                  }

                  first = pos.offset(hit.getSide()).toImmutable();
                  second = first.offset(client.player.getHorizontalFacing().rotateYClockwise()).toImmutable();
                  if (!placeable(client, first) || !placeable(client, second)) {
                     first = null;
                     second = null;
                     return;
                  }

                  stage = 0;
                  held = true;
               }

               if (stage < 2) {
                  BlockPos posx = stage == 0 ? first : second;
                  int slot = InventoryUtil.findHotbarSlot(client, Items.RESPAWN_ANCHOR);
                  if (slot < 0 || !placeable(client, posx)) {
                     return;
                  }

                  BlockPos support = posx.down();
                  if (use(client, slot, new BlockHitResult(Vec3d.ofCenter(support).add(0.0, 0.5, 0.0), Direction.UP, support, false))) {
                     stage++;
                  }
               } else if (stage < 4) {
                  BlockPos posxx = stage == 2 ? first : second;
                  if (charge(client, config, posxx)) {
                     stage++;
                  }
               } else {
                  int slotx = Math.max(0, Math.min(8, config.doubleAnchorTotemSlot - 1));
                  if (client.player.getInventory().getStack(slotx).isOf(Items.TOTEM_OF_UNDYING)) {
                     InventoryUtil.setSelectedSlot(client, slotx);
                  }
               }

               lastTime = now;
            }
         }
      } else {
         reset();
      }
   }

   private static boolean placeable(MinecraftClient client, BlockPos pos) {
      return client.world.getBlockState(pos).isReplaceable()
         && !client.world.getBlockState(pos.down()).isAir()
         && client.player.getEyePos().squaredDistanceTo(Vec3d.ofCenter(pos)) <= 20.25;
   }

   private static boolean charge(MinecraftClient client, MacroConfig config, BlockPos pos) {
      BlockState state = client.world.getBlockState(pos);
      if (!state.isOf(Blocks.RESPAWN_ANCHOR)) {
         return false;
      } else if ((Integer)state.get(RespawnAnchorBlock.CHARGES) > 0) {
         return true;
      } else {
         int slot = InventoryUtil.findHotbarSlot(client, Items.GLOWSTONE);
         return slot >= 0 && use(client, slot, new BlockHitResult(Vec3d.ofCenter(pos).add(0.0, 0.5, 0.0), Direction.UP, pos, false));
      }
   }

   private static boolean use(MinecraftClient client, int slot, BlockHitResult hit) {
      int previous = InventoryUtil.getSelectedSlot(client);
      InventoryUtil.setSelectedSlot(client, slot);

      boolean var4;
      try {
         var4 = client.interactionManager.interactBlock(client.player, Hand.MAIN_HAND, hit).isAccepted();
      } finally {
         InventoryUtil.setSelectedSlot(client, previous);
      }

      return var4;
   }
}
