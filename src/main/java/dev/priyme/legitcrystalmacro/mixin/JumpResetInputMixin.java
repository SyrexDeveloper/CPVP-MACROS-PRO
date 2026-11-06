package dev.priyme.legitcrystalmacro.mixin;

import dev.priyme.legitcrystalmacro.config.MacroConfig;
import dev.priyme.legitcrystalmacro.macro.JumpReset;
import dev.priyme.legitcrystalmacro.macro.TapReset;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.input.Input;
import net.minecraft.client.input.KeyboardInput;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin({KeyboardInput.class})
public abstract class JumpResetInputMixin {
   @Inject(
      method = {"method_3129"},
      at = {@At("HEAD")}
   )
   private void tapResetInput(CallbackInfo ci) {
      TapReset.beforeInput(MinecraftClient.getInstance(), MacroConfig.get());
   }

   @Inject(
      method = {"method_3129"},
      at = {@At("TAIL")}
   )
   private void jumpResetInput(CallbackInfo ci) {
      if (JumpReset.wantsJump(MinecraftClient.getInstance())) {
         Input input = (Input)(Object)this;
         input.jump();
      }
   }
}
