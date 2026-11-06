package dev.priyme.legitcrystalmacro.macro;

import dev.priyme.legitcrystalmacro.config.MacroConfig;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.block.ShulkerBoxBlock;
import net.minecraft.client.MinecraftClient;
import net.minecraft.util.hit.BlockHitResult;

public final class NoInteract {
   private NoInteract() {
   }

   public static boolean blocks(MinecraftClient client, BlockHitResult hit) {
      MacroConfig config = MacroConfig.get();
      if (config != null && config.noInteractEnabled && client.world != null && hit != null) {
         Block block = client.world.getBlockState(hit.getBlockPos()).getBlock();
         return block == Blocks.CHEST
            || block == Blocks.TRAPPED_CHEST
            || block == Blocks.ENDER_CHEST
            || block == Blocks.BARREL
            || block instanceof ShulkerBoxBlock;
      } else {
         return false;
      }
   }
}
