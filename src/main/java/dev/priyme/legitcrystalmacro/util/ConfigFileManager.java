package dev.priyme.legitcrystalmacro.util;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import dev.priyme.legitcrystalmacro.config.MacroConfig;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Locale;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;

public final class ConfigFileManager {
   private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

   private ConfigFileManager() {
   }

   public static void executeOnClient(Runnable action) {
      MinecraftClient.getInstance().execute(action);
   }

   public static void applyPreset(String name) {
      applyPreset(name, null);
   }

   public static void applyPreset(String name, Runnable onComplete) {
      try {
         applyPresetChecked(name);
         sendChatMessage("Successfully applied preset: " + name);
         if (onComplete != null) {
            executeOnClient(onComplete);
         }
      } catch (Exception var3) {
         sendChatMessage("Failed to apply preset: " + var3.getMessage());
      }
   }

   public static void applyPresetChecked(String name) throws IOException {
      String presetName = name.toLowerCase(Locale.ROOT);

      String resource = switch (presetName) {
         case "sword config" -> "sword_config.json";
         case "spear config" -> "spear_config.json";
         default -> throw new IOException("Unknown preset: " + name);
      };

      MacroConfig preset;
      try (InputStream in = ConfigFileManager.class.getResourceAsStream(
              "/assets/legitcrystalmacro/presets/" + resource)) {
         if (in == null) {
            throw new IOException("Preset resource not found: " + name);
         }
         preset = GSON.fromJson(new InputStreamReader(in, StandardCharsets.UTF_8), MacroConfig.class);
      }

      if (preset == null) {
         throw new IOException("Invalid preset: " + name);
      } else {
         MacroConfig current = MacroConfig.get();
         if (current == null) {
            throw new IOException("Configuration is not initialized");
         } else {
            preset.playerNameProtectEnabled = current.playerNameProtectEnabled;
            preset.playerNameProtectFakeName = current.playerNameProtectFakeName;
            preset.playerNameProtectOriginalName = current.playerNameProtectOriginalName;
            preset.playerPanicEnabled = current.playerPanicEnabled;
            preset.playerPanicKey = current.playerPanicKey;
            preset.playerPanicHealth = current.playerPanicHealth;
            preset.playerPingSpoofEnabled = current.playerPingSpoofEnabled;
            preset.checkUpdates = current.checkUpdates;
            if (current.spearLungeKey > 0) {
               preset.spearLungeKey = current.spearLungeKey;
            }

            preset.customCrosshairEnabled = current.customCrosshairEnabled;
            preset.crosshairStyle = current.crosshairStyle;
            preset.crosshairSize = current.crosshairSize;
            preset.crosshairGap = current.crosshairGap;
            preset.crosshairColor = current.crosshairColor;
            preset.capesOrbitEnabled = current.capesOrbitEnabled;
            preset.orbitSpeed = current.orbitSpeed;
            preset.orbitRadius = current.orbitRadius;
            preset.orbitCount = current.orbitCount;
            preset.moduleKeybinds = new HashMap(current.moduleKeybinds);
            current.replaceAndSave(preset);
         }
      }
   }

   public static void resetToDefaults() {
      try {
         MacroConfig.get().resetToDefaults();
         sendChatMessage("Default configuration restored; previous settings backed up.");
      } catch (Exception var1) {
         sendChatMessage("Failed to reset config: " + var1.getMessage());
      }
   }

   private static void sendChatMessage(String message) {
      executeOnClient(() -> {
         MinecraftClient client = MinecraftClient.getInstance();
         if (client.player != null) {
            client.player.sendMessage(Text.literal("§7[§dCPvP§7] " + message), false);
         }
      });
   }
}
