package dev.priyme.legitcrystalmacro.macro;

import dev.priyme.legitcrystalmacro.config.MacroConfig;
import dev.priyme.legitcrystalmacro.util.InventoryUtil;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.AxeItem;
import net.minecraft.item.Items;
import net.minecraft.registry.tag.ItemTags;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult.Type;

public final class ShieldBreaker {
   private static int originalSlot = -1;
   private static int tickCounter = 0;
   private static int cooldownTicks = 0;
   private static PlayerEntity targetPlayer = null;
   private static ShieldBreaker.State state = ShieldBreaker.State.IDLE;

   private ShieldBreaker() {
   }

   public static void tick(MinecraftClient client, MacroConfig config) {
      if (config != null && (config.shieldBreakerEnabled || config.shieldBreakerEnabled)) {
         if (client.player == null || client.interactionManager == null) {
            reset();
         } else if (cooldownTicks > 0) {
            cooldownTicks--;
         } else {
            switch (state) {
               case IDLE:
                  if (client.crosshairTarget != null && client.crosshairTarget.getType() == Type.ENTITY) {
                     EntityHitResult ehr = (EntityHitResult)client.crosshairTarget;
                     if (ehr.getEntity() instanceof PlayerEntity player && player.isUsingItem() && player.getActiveItem().isOf(Items.SHIELD)) {
                        targetPlayer = player;
                        originalSlot = InventoryUtil.getSelectedSlot(client);
                        int axeSlot = findBestAxeSlot(client);
                        if (axeSlot != -1) {
                           InventoryUtil.setSelectedSlot(client, axeSlot);
                           tickCounter = 0;
                           state = ShieldBreaker.State.SWITCHING_AXE;
                        }
                     }
                  }
                  break;
               case SWITCHING_AXE:
                  tickCounter++;
                  if (tickCounter >= config.shieldBreakerAxeSwitchDelay + 1) {
                     if (targetPlayer != null && targetPlayer.isAlive()) {
                        client.interactionManager.attackEntity(client.player, targetPlayer);
                        client.player.swingHand(Hand.MAIN_HAND);
                     }

                     tickCounter = 0;
                     state = ShieldBreaker.State.SWITCHING_BACK;
                  }
               case BREAKING:
               default:
                  break;
               case SWITCHING_BACK:
                  tickCounter++;
                  if (tickCounter >= config.shieldBreakerWeaponSwitchDelay + 1) {
                     if (config.shieldBreakerAutoSwapBack && originalSlot != -1) {
                        InventoryUtil.setSelectedSlot(client, originalSlot);
                     }

                     if (config.shieldBreakerKillSwitch && targetPlayer != null && targetPlayer.isAlive()) {
                        tickCounter = 0;
                        state = ShieldBreaker.State.KILLING;
                     } else {
                        reset();
                        cooldownTicks = config.shieldBreakerCycleCooldown;
                     }
                  }
                  break;
               case KILLING:
                  tickCounter++;
                  if (tickCounter >= config.shieldBreakerKillDelay + 1) {
                     if (targetPlayer != null && targetPlayer.isAlive()) {
                        client.interactionManager.attackEntity(client.player, targetPlayer);
                        client.player.swingHand(Hand.MAIN_HAND);
                     }

                     reset();
                     cooldownTicks = config.shieldBreakerCycleCooldown;
                  }
            }
         }
      } else {
         reset();
      }
   }

   private static int findBestAxeSlot(MinecraftClient client) {
      if (client.player == null) {
         return -1;
      } else {
         for (int i = 0; i < 9; i++) {
            if (client.player.getInventory().getStack(i).getItem() instanceof AxeItem) {
               return i;
            }
         }

         for (int ix = 0; ix < 9; ix++) {
            if (client.player.getInventory().getStack(ix).isIn(ItemTags.AXES)) {
               return ix;
            }
         }

         return -1;
      }
   }

   private static void reset() {
      state = ShieldBreaker.State.IDLE;
      targetPlayer = null;
      originalSlot = -1;
      tickCounter = 0;
   }

   private static enum State {
      IDLE,
      SWITCHING_AXE,
      BREAKING,
      SWITCHING_BACK,
      KILLING;
   }
}
