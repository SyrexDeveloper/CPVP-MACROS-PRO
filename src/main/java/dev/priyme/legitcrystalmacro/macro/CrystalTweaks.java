package dev.priyme.legitcrystalmacro.macro;

import dev.priyme.legitcrystalmacro.config.MacroConfig;
import dev.priyme.legitcrystalmacro.util.KeyInput;
import net.minecraft.client.MinecraftClient;

public final class CrystalTweaks {
   private CrystalTweaks() {
   }

   public static void tick(MinecraftClient client, MacroConfig config) {
      if (config != null && config.crystalTweaksEnabled) {
         if (client.player != null) {
            if (config.crystalTweaksZeroDelay) {
               KeyInput.setItemUseCooldown(client, 0);
            }
         }
      }
   }
}
