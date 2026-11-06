package dev.priyme.legitcrystalmacro.macro;

import dev.priyme.legitcrystalmacro.config.MacroConfig;
import net.minecraft.client.MinecraftClient;

public final class AntiTrap {
   private AntiTrap() {
   }

   public static void tick(MinecraftClient client, MacroConfig config) {
      if (config == null || client.player == null) {
         ;
      }
   }
}
