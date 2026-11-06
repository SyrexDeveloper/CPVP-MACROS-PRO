package dev.priyme.legitcrystalmacro.mixin;

import dev.priyme.legitcrystalmacro.config.MacroConfig;
import dev.priyme.legitcrystalmacro.util.NameProtectUtil;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin({PlayerEntity.class})
public abstract class PlayerEntityMixin {
   @Inject(
      method = {"method_5476"},
      at = {@At("RETURN")},
      cancellable = true
   )
   private void onGetDisplayName(CallbackInfoReturnable<Text> cir) {
      MacroConfig config = MacroConfig.get();
      if (config != null && config.playerNameProtectEnabled) {
         MinecraftClient client = MinecraftClient.getInstance();
         PlayerEntity self = (PlayerEntity)(Object)this;
         if (NameProtectUtil.isSelf(self, self.getGameProfile(), client)) {
            String original = NameProtectUtil.getOriginalName(self, self.getGameProfile(), client);
            String alias = NameProtectUtil.getAlias(original);
            Text masked = NameProtectUtil.maskText((Text)cir.getReturnValue(), original, alias);
            cir.setReturnValue(masked);
         }
      }
   }

   @Inject(
      method = {"method_5477"},
      at = {@At("RETURN")},
      cancellable = true
   )
   private void onGetName(CallbackInfoReturnable<Text> cir) {
      MacroConfig config = MacroConfig.get();
      if (config != null && config.playerNameProtectEnabled) {
         MinecraftClient client = MinecraftClient.getInstance();
         PlayerEntity self = (PlayerEntity)(Object)this;
         if (NameProtectUtil.isSelf(self, self.getGameProfile(), client)) {
            String original = NameProtectUtil.getOriginalName(self, self.getGameProfile(), client);
            String alias = NameProtectUtil.getAlias(original);
            cir.setReturnValue(Text.literal(alias));
         }
      }
   }
}
