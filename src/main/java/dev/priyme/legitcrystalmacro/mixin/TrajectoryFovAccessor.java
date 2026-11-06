package dev.priyme.legitcrystalmacro.mixin;

import net.minecraft.client.render.Camera;
import net.minecraft.client.render.GameRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin({GameRenderer.class})
public interface TrajectoryFovAccessor {
   @Invoker("method_3196")
   float trajectoryFov(Camera var1, float var2, boolean var3);
}
