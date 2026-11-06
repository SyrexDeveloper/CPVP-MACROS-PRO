package dev.priyme.legitcrystalmacro.mixin;

import com.mojang.authlib.GameProfile;
import dev.priyme.legitcrystalmacro.config.MacroConfig;
import dev.priyme.legitcrystalmacro.util.NameProtectUtil;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin({PlayerListEntry.class})
public abstract class PlayerListEntryMixin {
   @Shadow
   public abstract GameProfile method_2966();

   @Inject(
      method = {"method_2971"},
      at = {@At("RETURN")},
      cancellable = true
   )
   private void onGetDisplayName(CallbackInfoReturnable<Text> cir) {
      MacroConfig config = MacroConfig.get();
      if (config != null && config.playerNameProtectEnabled) {
         MinecraftClient client = MinecraftClient.getInstance();
         if (this.method_2966() != null && NameProtectUtil.isSelf(null, this.method_2966(), client)) {
            String profName = NameProtectUtil.getProfileName(this.method_2966());
            String alias = NameProtectUtil.getAlias(profName);
            Text current = (Text)cir.getReturnValue();
            if (current != null) {
               cir.setReturnValue(NameProtectUtil.maskText(current, profName, alias));
            } else {
               cir.setReturnValue(Text.literal(alias));
            }
         }
      }
   }
}
