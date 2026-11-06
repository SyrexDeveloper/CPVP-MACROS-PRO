package dev.priyme.legitcrystalmacro.util;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.RespawnAnchorBlock;
import net.minecraft.client.MinecraftClient;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;

public final class BlockInteract {
   private BlockInteract() {
   }

   public static boolean isBlockAt(MinecraftClient client, BlockPos pos, Block block) {
      return client.world == null ? false : client.world.getBlockState(pos).isOf(block);
   }

   public static boolean isChargedAnchor(MinecraftClient client, BlockPos pos) {
      if (client.world == null) {
         return false;
      } else {
         BlockState state = client.world.getBlockState(pos);
         return !state.isOf(Blocks.RESPAWN_ANCHOR) ? false : (Integer)state.get(RespawnAnchorBlock.CHARGES) > 0;
      }
   }

   public static boolean isUnchargedAnchor(MinecraftClient client, BlockPos pos) {
      if (client.world == null) {
         return false;
      } else {
         BlockState state = client.world.getBlockState(pos);
         return !state.isOf(Blocks.RESPAWN_ANCHOR) ? false : (Integer)state.get(RespawnAnchorBlock.CHARGES) == 0;
      }
   }

   public static int getAnchorCharges(MinecraftClient client, BlockPos pos) {
      if (client.world == null) {
         return 0;
      } else {
         BlockState state = client.world.getBlockState(pos);
         return !state.isOf(Blocks.RESPAWN_ANCHOR) ? 0 : (Integer)state.get(RespawnAnchorBlock.CHARGES);
      }
   }

   public static ActionResult interactBlock(MinecraftClient client, Hand hand, BlockHitResult hitResult) {
      return (ActionResult)(client.player != null && client.interactionManager != null
         ? client.interactionManager.interactBlock(client.player, hand, hitResult)
         : ActionResult.FAIL);
   }
}
