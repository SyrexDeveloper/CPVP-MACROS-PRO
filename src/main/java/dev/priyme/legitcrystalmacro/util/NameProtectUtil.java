package dev.priyme.legitcrystalmacro.util;

import com.mojang.authlib.GameProfile;
import dev.priyme.legitcrystalmacro.config.MacroConfig;
import java.lang.reflect.Method;
import java.util.UUID;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;

public final class NameProtectUtil {
   private NameProtectUtil() {
   }

   public static String getAlias(String original) {
      MacroConfig config = MacroConfig.get();
      if (config != null && config.playerNameProtectEnabled) {
         return config.playerNameProtectFakeName != null && !config.playerNameProtectFakeName.trim().isEmpty()
            ? config.playerNameProtectFakeName.trim()
            : "⚡「 幽霊 」⚡";
      } else {
         return original != null ? original : "⚡「 幽霊 」⚡";
      }
   }

   public static String getProfileName(GameProfile profile) {
      if (profile == null) {
         return null;
      } else {
         try {
            Method m = profile.getClass().getMethod("name");
            return (String)m.invoke(profile);
         } catch (Throwable var4) {
            try {
               Method mx = profile.getClass().getMethod("getName");
               return (String)mx.invoke(profile);
            } catch (Throwable var3) {
               return null;
            }
         }
      }
   }

   public static UUID getProfileId(GameProfile profile) {
      if (profile == null) {
         return null;
      } else {
         try {
            Method m = profile.getClass().getMethod("id");
            return (UUID)m.invoke(profile);
         } catch (Throwable var4) {
            try {
               Method mx = profile.getClass().getMethod("getId");
               return (UUID)mx.invoke(profile);
            } catch (Throwable var3) {
               return null;
            }
         }
      }
   }

   public static boolean isSelf(PlayerEntity player, GameProfile profile, MinecraftClient client) {
      if (client == null) {
         return false;
      } else {
         if (player != null && client.player != null) {
            if (player == client.player) {
               return true;
            }

            if (player.getUuid() != null && player.getUuid().equals(client.player.getUuid())) {
               return true;
            }
         }

         if (profile != null) {
            UUID id = getProfileId(profile);
            if (client.player != null && id != null && id.equals(client.player.getUuid())) {
               return true;
            }

            String name = getProfileName(profile);
            if (client.getSession() != null && name != null && name.equalsIgnoreCase(client.getSession().getUsername())) {
               return true;
            }
         }

         return false;
      }
   }

   public static String getOriginalName(PlayerEntity player, GameProfile profile, MinecraftClient client) {
      if (profile != null) {
         String name = getProfileName(profile);
         if (name != null && !name.isEmpty()) {
            return name;
         }
      }

      if (client != null && client.getSession() != null && client.getSession().getUsername() != null) {
         return client.getSession().getUsername();
      } else {
         return player != null ? player.getNameForScoreboard() : "";
      }
   }

   public static Text maskText(Text text, String original, String alias) {
      if (text == null) {
         return null;
      } else {
         if (alias == null) {
            alias = "⚡「 幽霊 」⚡";
         }

         String raw = text.getString();
         if (raw != null && !raw.isEmpty()) {
            if (original == null || original.isEmpty()) {
               MinecraftClient client = MinecraftClient.getInstance();
               if (client != null && client.getSession() != null) {
                  original = client.getSession().getUsername();
               }
            }

            if (original == null || original.isEmpty() || !raw.contains(original)) {
               return text;
            } else if (text.getSiblings().isEmpty()) {
               return Text.literal(raw.replace(original, alias)).setStyle(text.getStyle());
            } else {
               MutableText copy = text.copyContentOnly();
               String rootStr = copy.getString();
               if (rootStr != null && rootStr.contains(original)) {
                  copy = Text.literal(rootStr.replace(original, alias)).setStyle(text.getStyle());
               } else {
                  copy.setStyle(text.getStyle());
               }

               for (Text sibling : text.getSiblings()) {
                  copy.append(maskText(sibling, original, alias));
               }

               return copy;
            }
         } else {
            return text;
         }
      }
   }

   public static String filterText(String text) {
      if (text == null) {
         return null;
      } else {
         MacroConfig config = MacroConfig.get();
         if (config != null && config.playerNameProtectEnabled) {
            String fake = config.playerNameProtectFakeName != null && !config.playerNameProtectFakeName.trim().isEmpty()
               ? config.playerNameProtectFakeName.trim()
               : "⚡「 幽霊 」⚡";
            if (config.playerNameProtectOriginalName != null && !config.playerNameProtectOriginalName.trim().isEmpty()) {
               text = text.replace(config.playerNameProtectOriginalName.trim(), fake);
            }

            try {
               MinecraftClient client = MinecraftClient.getInstance();
               if (client != null && client.getSession() != null && client.getSession().getUsername() != null) {
                  text = text.replace(client.getSession().getUsername(), fake);
               }
            } catch (Throwable var4) {
            }
         }

         return text;
      }
   }
}
