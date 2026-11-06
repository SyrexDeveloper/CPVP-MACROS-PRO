package dev.priyme.legitcrystalmacro.macro;

import dev.priyme.legitcrystalmacro.config.MacroConfig;
import dev.priyme.legitcrystalmacro.util.KeyInput;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;

public final class TapReset {
   private static long until;
   private static boolean sTap;
   private static boolean inputApplied;

   private TapReset() {
   }

   public static boolean isResetting() {
      return until != 0L;
   }

   public static void reset() {
      if (until != 0L) {
         KeyBinding.updatePressedStates();
      }

      until = 0L;
      inputApplied = false;
   }

   public static void onAttack(MinecraftClient client, boolean wasSprinting) {
      MacroConfig config = MacroConfig.get();
      if (config != null
         && (config.tapResetEnabled || config.sTabSprintResetEnabled)
         && client.player != null
         && KeyInput.isScreenNull(client)
         && wasSprinting
         && client.options.forwardKey.isPressed()) {
         sTap = !config.tapResetEnabled || config.tapResetSTap && !config.tapResetWTap;
         int duration = config.tapResetEnabled ? Math.max(20, Math.min(500, config.tapResetDuration)) : 100;
         until = System.nanoTime() + duration * 1000000L;
         inputApplied = false;
         apply(client);
      }
   }

   private static void apply(MinecraftClient client) {
      client.player.setSprinting(false);
      client.options.forwardKey.setPressed(false);
      if (sTap) {
         client.options.backKey.setPressed(true);
      }
   }

   public static void beforeInput(MinecraftClient client, MacroConfig config) {
      tick(client, config);
      if (until != 0L) {
         inputApplied = true;
      }
   }

   public static void tick(MinecraftClient client, MacroConfig config) {
      if (config != null
         && (config.tapResetEnabled || config.sTabSprintResetEnabled)
         && client.player != null
         && KeyInput.isScreenNull(client)
         && (until == 0L || !inputApplied || System.nanoTime() < until)) {
         if (until != 0L) {
            apply(client);
         }
      } else {
         reset();
      }
   }
}
