package dev.priyme.legitcrystalmacro.macro;

import dev.priyme.legitcrystalmacro.config.MacroConfig;
import dev.priyme.legitcrystalmacro.util.KeyInput;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;

public final class PanicDisconnectMacro {
   private static boolean wasKeyPressed = false;

   private PanicDisconnectMacro() {
   }

   public static void tick(MinecraftClient client, MacroConfig config) {
      if (config != null && client.player != null && client.world != null) {
         if (config.playerPanicEnabled) {
            if (config.playerPanicKey != -1 && config.playerPanicKey != 0) {
               boolean isKeyDown = KeyInput.isPressed(client, config.playerPanicKey);
               if (isKeyDown && !wasKeyPressed) {
                  disconnect(client, "Panic key pressed!");
                  wasKeyPressed = true;
                  return;
               }

               wasKeyPressed = isKeyDown;
            }
         }
      }
   }

   private static void disconnect(MinecraftClient client, String reason) {
      if (client.getNetworkHandler() != null && client.getNetworkHandler().getConnection() != null) {
         client.getNetworkHandler().getConnection().disconnect(Text.literal("§c[Panic Disconnect] " + reason));
      }
   }
}
