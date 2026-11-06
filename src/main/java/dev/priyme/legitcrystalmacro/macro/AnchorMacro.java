package dev.priyme.legitcrystalmacro.macro;

import dev.priyme.legitcrystalmacro.config.MacroConfig;
import dev.priyme.legitcrystalmacro.util.InventoryUtil;
import dev.priyme.legitcrystalmacro.util.KeyInput;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.RespawnAnchorBlock;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.Items;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult.Type;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;

public final class AnchorMacro {
   private static long lastChargeTime = 0L;
   private static long lastExplodeTime = 0L;
   private static long lastSwapTime = 0L;
   private static int previousSlot = -1;
   private static int pendingRestoreTicks = 0;

   private AnchorMacro() {
   }

   static boolean isRunning() {
      return previousSlot != -1 || pendingRestoreTicks > 0;
   }

   public static void reset() {
      previousSlot = -1;
      pendingRestoreTicks = 0;
   }

   public static void tick(MinecraftClient client, MacroConfig config) {
      if (config != null && (config.anchorMacroEnabled || config.anchorEnabled)) {
         if (client.player == null || client.world == null || client.interactionManager == null) {
            reset();
         } else if (client.currentScreen != null) {
            reset();
         } else {
            if (pendingRestoreTicks > 0) {
               pendingRestoreTicks--;
               if (pendingRestoreTicks == 0 && config.anchorSwitchBack && previousSlot != -1) {
                  InventoryUtil.setSelectedSlot(client, previousSlot);
                  previousSlot = -1;
               }
            }

            boolean rightClickHeld;
            if (config.anchorKeyMode == 1) {
               rightClickHeld = config.anchorActivateKey > 0 && KeyInput.isPressed(client, config.anchorActivateKey);
            } else if (config.anchorKeyMode == 2) {
               rightClickHeld = client.options.useKey.isPressed() || config.anchorActivateKey > 0 && KeyInput.isPressed(client, config.anchorActivateKey);
            } else {
               rightClickHeld = client.options.useKey.isPressed();
               if (!rightClickHeld && config.anchorActivateKey > 0 && config.anchorActivateKey != 1) {
                  rightClickHeld = KeyInput.isPressed(client, config.anchorActivateKey);
               }
            }

            if (!rightClickHeld) {
               if (config.anchorSwitchBack && previousSlot != -1) {
                  InventoryUtil.setSelectedSlot(client, previousSlot);
                  previousSlot = -1;
               }
            } else if (config.anchorWhileUse || !client.player.isUsingItem()) {
               if (!config.anchorLootProtect || !hasLootNearby(client, 10.0)) {
                  if (client.crosshairTarget != null && client.crosshairTarget.getType() == Type.BLOCK) {
                     BlockHitResult hit = (BlockHitResult)client.crosshairTarget;
                     BlockPos pos = hit.getBlockPos();
                     BlockState state = client.world.getBlockState(pos);
                     if (state.isOf(Blocks.RESPAWN_ANCHOR)) {
                        long now = System.currentTimeMillis();
                        int charges = (Integer)state.get(RespawnAnchorBlock.CHARGES);
                        int wantedCharges = Math.max(1, Math.min(4, config.anchorCharges));
                        if (charges < wantedCharges) {
                           int glowSlot = InventoryUtil.findHotbarSlot(client, Items.GLOWSTONE);
                           if (glowSlot == -1) {
                              return;
                           }

                           int curSlot = InventoryUtil.getSelectedSlot(client);
                           if (curSlot != glowSlot) {
                              if (previousSlot == -1) {
                                 previousSlot = curSlot;
                              }

                              InventoryUtil.setSelectedSlot(client, glowSlot);
                              lastSwapTime = now;
                              return;
                           }

                           if (now - lastSwapTime < config.anchorSwapDelay) {
                              return;
                           }

                           if (now - lastChargeTime < config.anchorChargeDelay) {
                              return;
                           }

                           client.interactionManager.interactBlock(client.player, Hand.MAIN_HAND, hit);
                           client.player.swingHand(Hand.MAIN_HAND);
                           lastChargeTime = now;
                        } else {
                           if (config.anchorOnlyCharge) {
                              return;
                           }

                           int detonateSlot = getDetonateSlot(client, config);
                           if (detonateSlot == -1) {
                              detonateSlot = InventoryUtil.getSelectedSlot(client);
                           }

                           int curSlotx = InventoryUtil.getSelectedSlot(client);
                           if (curSlotx != detonateSlot) {
                              if (previousSlot == -1) {
                                 previousSlot = curSlotx;
                              }

                              InventoryUtil.setSelectedSlot(client, detonateSlot);
                              lastSwapTime = now;
                              return;
                           }

                           if (now - lastSwapTime < config.anchorSwapDelay) {
                              return;
                           }

                           if (now - lastExplodeTime < config.anchorExplodeDelay) {
                              return;
                           }

                           client.interactionManager.interactBlock(client.player, Hand.MAIN_HAND, hit);
                           client.player.swingHand(Hand.MAIN_HAND);
                           lastExplodeTime = now;
                           pendingRestoreTicks = 2;
                        }
                     }
                  }
               }
            }
         }
      } else {
         reset();
      }
   }

   private static int getDetonateSlot(MinecraftClient client, MacroConfig config) {
      if (config.anchorExplodeSlot > 0 && config.anchorExplodeSlot <= 9) {
         return config.anchorExplodeSlot - 1;
      } else {
         if (config.anchorExplodeWith == 0) {
            int totemSlot = InventoryUtil.findHotbarSlot(client, Items.TOTEM_OF_UNDYING);
            if (totemSlot != -1) {
               return totemSlot;
            }
         }

         int nonGlow = findNonGlowstoneSlot(client);
         return nonGlow != -1 ? nonGlow : InventoryUtil.getSelectedSlot(client);
      }
   }

   private static int findNonGlowstoneSlot(MinecraftClient client) {
      if (client.player == null) {
         return -1;
      } else {
         PlayerInventory inv = client.player.getInventory();

         for (int i = 0; i < 9; i++) {
            if (!inv.getStack(i).isOf(Items.GLOWSTONE)) {
               return i;
            }
         }

         return -1;
      }
   }

   private static boolean hasLootNearby(MinecraftClient client, double radius) {
      if (client.player != null && client.world != null) {
         Box box = client.player.getBoundingBox().expand(radius);
         return !client.world.getEntitiesByClass(ItemEntity.class, box, entity -> true).isEmpty();
      } else {
         return false;
      }
   }
}
