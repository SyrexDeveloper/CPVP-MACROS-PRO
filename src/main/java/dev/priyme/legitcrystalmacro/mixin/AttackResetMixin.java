package dev.priyme.legitcrystalmacro.mixin;

import dev.priyme.legitcrystalmacro.macro.TapReset;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerInteractionManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin({ClientPlayerInteractionManager.class})
public abstract class AttackResetMixin {
   @Unique
   private boolean cpvp$wasSprinting;

   @Inject(
      method = {"method_2918"},
      at = {@At("HEAD")}
   )
   private void beforeAttack(PlayerEntity player, Entity target, CallbackInfo ci) {
      this.cpvp$wasSprinting = player.isSprinting();
   }

   @Inject(
      method = {"method_2918"},
      at = {@At("RETURN")}
   )
   private void afterAttack(PlayerEntity player, Entity target, CallbackInfo ci) {
      if (target instanceof LivingEntity && target.isAlive()) {
         TapReset.onAttack(MinecraftClient.getInstance(), this.cpvp$wasSprinting);
      }
   }
}
