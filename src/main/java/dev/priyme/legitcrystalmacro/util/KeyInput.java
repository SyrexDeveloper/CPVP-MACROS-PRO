package dev.priyme.legitcrystalmacro.util;

import dev.priyme.legitcrystalmacro.mixin.MinecraftClientAccessor;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.TitleScreen;
import org.lwjgl.glfw.GLFW;

public final class KeyInput {
   private KeyInput() {
   }

   public static boolean isPressed(MinecraftClient client, int keyCode) {
      if (client != null && client.getWindow() != null && keyCode != -1) {
         long handle = client.getWindow().getHandle();
         return keyCode >= 0 && keyCode <= 7 ? GLFW.glfwGetMouseButton(handle, keyCode) == 1 : GLFW.glfwGetKey(handle, keyCode) == 1;
      } else {
         return false;
      }
   }

   public static void tapKey(MinecraftClient client, int keyCode) {
      if (client != null && client.player != null) {
         if (keyCode >= 49 && keyCode <= 57) {
            int slot = keyCode - 49;
            if (slot >= 0 && slot <= 8) {
               InventoryUtil.setSelectedSlot(client, slot);
            }
         }
      }
   }

   public static String keyName(int code) {
      return GLFW.glfwGetKeyName(code, 0);
   }

   public static boolean isScreenNull(MinecraftClient client) {
      return client == null || client.currentScreen == null;
   }

   public static Object getCurrentScreen(MinecraftClient client) {
      return client != null ? client.currentScreen : null;
   }

   public static void setItemUseCooldown(MinecraftClient client, int cooldown) {
      if (client != null) {
         ((MinecraftClientAccessor)client).setItemUseCooldown(Math.max(0, cooldown));
      }
   }

   public static boolean isTitleScreen(Object screen) {
      return screen instanceof TitleScreen;
   }

   public static void setScreen(Object clientObj, Object screenObj) {
      if (clientObj instanceof MinecraftClient client) {
         client.setScreen((Screen)screenObj);
      }
   }

   public static void returnToScreen(Object screen) {
      MinecraftClient mc = MinecraftClient.getInstance();
      if (mc != null) {
         mc.setScreen((Screen)screen);
      }
   }
}
