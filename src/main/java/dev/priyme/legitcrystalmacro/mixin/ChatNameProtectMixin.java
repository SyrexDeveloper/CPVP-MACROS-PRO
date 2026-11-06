package dev.priyme.legitcrystalmacro.mixin;

import dev.priyme.legitcrystalmacro.config.MacroConfig;
import dev.priyme.legitcrystalmacro.util.NameProtectUtil;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.hud.ChatHud;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin({ChatHud.class})
public abstract class ChatNameProtectMixin {
   @ModifyVariable(
      method = {"method_1812"},
      at = @At("HEAD"),
      argsOnly = true,
      ordinal = 0
   )
   private Text maskDisplayedMessage(Text text) {
      MacroConfig config = MacroConfig.get();
      if (config != null && config.playerNameProtectEnabled) {
         String original = NameProtectUtil.getOriginalName(null, null, MinecraftClient.getInstance());
         return NameProtectUtil.maskText(text, original, NameProtectUtil.getAlias(original));
      } else {
         return text;
      }
   }
}
