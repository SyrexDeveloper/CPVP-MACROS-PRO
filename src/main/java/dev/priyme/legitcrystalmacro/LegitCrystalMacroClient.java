package dev.priyme.legitcrystalmacro;

import dev.priyme.legitcrystalmacro.config.MacroConfig;
import dev.priyme.legitcrystalmacro.gui.ClickGuiScreen;
import dev.priyme.legitcrystalmacro.macro.JumpReset;
import dev.priyme.legitcrystalmacro.macro.MacroManager;
import dev.priyme.legitcrystalmacro.macro.Trajectories;
import dev.priyme.legitcrystalmacro.macro.TrajectoryRenderer;
import dev.priyme.legitcrystalmacro.util.KeyInput;
import dev.priyme.legitcrystalmacro.util.UpdateTracker;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents.StartTick;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents.Disconnect;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.option.KeyBinding.Category;
import net.minecraft.client.util.InputUtil.Type;
import net.minecraft.util.Identifier;

public final class LegitCrystalMacroClient implements ClientModInitializer {
   public static final String MOD_ID = "legitcrystalmacro";
   public static final String MOD_NAME = "CPvP Macros";
   public static final String MOD_VERSION = "3.6";
   public static MacroConfig config;
   public static MacroManager macroManager;
   private static KeyBinding clickGuiKey;

   public void onInitializeClient() {
      config = MacroConfig.load();
      macroManager = new MacroManager();
      UpdateTracker.checkForUpdates();
      TrajectoryRenderer.register();
      ClientPlayConnectionEvents.DISCONNECT.register((Disconnect)(handler, client) -> {
         JumpReset.reset();
         Trajectories.reset();
      });
      clickGuiKey = KeyBindingHelper.registerKeyBinding(
         createKeyBinding("key.legitcrystalmacro.open_gui", Type.KEYSYM, 344, "category.legitcrystalmacro.title")
      );
      ClientTickEvents.START_CLIENT_TICK.register((StartTick)client -> {
         if (clickGuiKey != null && clickGuiKey.wasPressed() && client.currentScreen == null) {
            client.setScreen(new ClickGuiScreen(null));
         }

         if (client.currentScreen != null && KeyInput.isTitleScreen(client.currentScreen)) {
            UpdateTracker.handleTitleScreenTick(client, client.currentScreen);
         }

         if (client.player != null && client.world != null) {
            macroManager.tick(client, config);
         }
      });
   }

   private static KeyBinding createKeyBinding(String translationKey, Type type, int code, String categoryKey) {
      try {
         for (Constructor<?> ctor : KeyBinding.class.getConstructors()) {
            Class<?>[] params = ctor.getParameterTypes();
            if (params.length == 4 && params[0] == String.class && params[1] == Type.class && params[2] == int.class) {
               if (params[3] == String.class) {
                  return (KeyBinding)ctor.newInstance(translationKey, type, code, categoryKey);
               }

               Class<?> catClass = params[3];
               Object categoryObj = null;

               for (Method m : catClass.getMethods()) {
                  if (Modifier.isStatic(m.getModifiers()) && m.getParameterCount() == 1 && m.getReturnType() == catClass) {
                     categoryObj = m.invoke(null, Identifier.of("legitcrystalmacro", "general"));
                     break;
                  }
               }

               if (categoryObj != null) {
                  return (KeyBinding)ctor.newInstance(translationKey, type, code, categoryObj);
               }
            }
         }
      } catch (Throwable var15) {
      }

      return new KeyBinding(translationKey, type, code, Category.create(Identifier.of("legitcrystalmacro", "general")));
   }
}
