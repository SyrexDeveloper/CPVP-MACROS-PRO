package dev.priyme.legitcrystalmacro.mixin;

import net.minecraft.client.MinecraftClient;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin({MinecraftClient.class})
public interface MinecraftClientAccessor {
   @Invoker("method_1536")
   boolean invokeDoAttack();

   @Accessor("field_1771")
   void setAttackCooldown(int var1);

   @Accessor("field_1771")
   int getAttackCooldown();

   @Accessor("field_1752")
   void setItemUseCooldown(int var1);

   @Accessor("field_1752")
   int getItemUseCooldown();
}
