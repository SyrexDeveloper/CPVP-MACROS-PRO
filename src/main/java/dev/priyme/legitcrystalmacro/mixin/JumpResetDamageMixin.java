package dev.priyme.legitcrystalmacro.mixin;

import dev.priyme.legitcrystalmacro.macro.JumpReset;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.network.packet.s2c.play.EntityDamageS2CPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin({ClientPlayNetworkHandler.class})
public abstract class JumpResetDamageMixin {
   @Inject(
      method = {"method_49034"},
      at = {@At("TAIL")}
   )
   private void jumpResetDamage(EntityDamageS2CPacket packet, CallbackInfo ci) {
      MinecraftClient client = MinecraftClient.getInstance();
      if (client.player != null && client.world != null && packet.entityId() == client.player.getId()) {
         JumpReset.damaged(packet.createDamageSource(client.world));
      }
   }
}
