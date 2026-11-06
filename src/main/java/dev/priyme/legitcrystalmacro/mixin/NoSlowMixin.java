package dev.priyme.legitcrystalmacro.mixin;

import dev.priyme.legitcrystalmacro.config.MacroConfig;
import dev.priyme.legitcrystalmacro.macro.NoSlow;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin({ClientPlayerEntity.class})
public abstract class NoSlowMixin {
   @Redirect(
      method = {"method_67270"},
      at = @At(
         value = "INVOKE",
         target = "Lnet/minecraft/class_746;method_6115()Z"
      )
   )
   private boolean noSlowRedirectIsUsingItem(ClientPlayerEntity player) {
      MacroConfig cfg = MacroConfig.get();
      return NoSlow.shouldCancelSlowdown(cfg) && !player.hasVehicle() && !player.isDead() && !player.isSleeping() ? false : player.isUsingItem();
   }

   @Inject(
      method = {"method_6007"},
      at = {@At("HEAD")}
   )
   private void onPreTickMovement(CallbackInfo ci) {
      MacroConfig cfg = MacroConfig.get();
      if (NoSlow.shouldCancelSlowdown(cfg)) {
         NoSlow.onPreMotion(MinecraftClient.getInstance(), cfg);
      }
   }

   @Inject(
      method = {"method_6007"},
      at = {@At("RETURN")}
   )
   private void onPostTickMovement(CallbackInfo ci) {
      MacroConfig cfg = MacroConfig.get();
      if (NoSlow.shouldCancelSlowdown(cfg)) {
         NoSlow.onPostMotion(MinecraftClient.getInstance(), cfg);
      }
   }
}
