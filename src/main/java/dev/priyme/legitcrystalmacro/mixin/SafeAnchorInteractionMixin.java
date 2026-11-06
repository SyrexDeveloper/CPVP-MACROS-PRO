package dev.priyme.legitcrystalmacro.mixin;

import dev.priyme.legitcrystalmacro.macro.SafeAnchorMacro;
import dev.priyme.legitcrystalmacro.macro.safeanchor.SafeAnchorSupport;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.network.ClientPlayerInteractionManager;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin({ClientPlayerInteractionManager.class})
public abstract class SafeAnchorInteractionMixin {
   @Inject(
      method = {"method_2896"},
      at = {@At("HEAD")}
   )
   private void safeAnchorBeforeInteract(ClientPlayerEntity player, Hand hand, BlockHitResult hit, CallbackInfoReturnable<ActionResult> cir) {
      SafeAnchorMacro.beforeInteract(MinecraftClient.getInstance(), hand, hit);
   }

   @Inject(
      method = {"method_2896"},
      at = {@At("RETURN")}
   )
   private void safeAnchorAfterInteract(ClientPlayerEntity player, Hand hand, BlockHitResult hit, CallbackInfoReturnable<ActionResult> cir) {
      SafeAnchorSupport.observeInteraction(MinecraftClient.getInstance(), (ActionResult)cir.getReturnValue());
      SafeAnchorMacro.afterInteract(MinecraftClient.getInstance(), hand, hit, (ActionResult)cir.getReturnValue());
   }
}
