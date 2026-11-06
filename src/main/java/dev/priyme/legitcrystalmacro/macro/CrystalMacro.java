package dev.priyme.legitcrystalmacro.macro;

import dev.priyme.legitcrystalmacro.config.MacroConfig;
import dev.priyme.legitcrystalmacro.util.BlockInteract;
import dev.priyme.legitcrystalmacro.util.DelayUtil;
import dev.priyme.legitcrystalmacro.util.InventoryUtil;
import dev.priyme.legitcrystalmacro.util.KeyInput;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;
import net.minecraft.block.Blocks;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.decoration.EndCrystalEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.tag.ItemTags;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.hit.HitResult.Type;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;

public final class CrystalMacro {
   private static final double LOOT_RADIUS = 10.0;
   private static final Random random = new Random();
   private static int placeDelayCounter;
   private static int breakDelayCounter;
   private static int switchDelayCounter;
   private static int cooldownCounter;
   private static long tickCounter;
   private static boolean placingObsidian;
   private static BlockHitResult pendingObsidian;
   private static BlockPos pendingCrystalBase;
   private static long pendingCrystalDeadline;
   private static boolean useWasHeld;
   private static long pausedUntil;
   private static final Set<Integer> knownDeadEntities = new HashSet<>();
   private static int previousSlotBeforeObsidian = -1;

   private CrystalMacro() {
   }

   public static void reset() {
      placeDelayCounter = 0;
      breakDelayCounter = 0;
      switchDelayCounter = 0;
      cooldownCounter = 0;
      tickCounter = 0L;
      placingObsidian = false;
      pendingObsidian = null;
      pendingCrystalBase = null;
      pendingCrystalDeadline = 0L;
      useWasHeld = false;
      pausedUntil = 0L;
      knownDeadEntities.clear();
      previousSlotBeforeObsidian = -1;
   }

   public static void tick(MinecraftClient client, MacroConfig config) {
      if (config != null && (config.crystalMacroEnabled || config.crystalEnabled)) {
         if (client.player == null || client.world == null || client.interactionManager == null || client.getWindow() == null) {
            reset();
         } else if (!KeyInput.isScreenNull(client)) {
            reset();
         } else {
            tickCounters();
            if (config.crystalStopOnKill) {
               checkStopOnKill(client, config);
               if (System.currentTimeMillis() < pausedUntil) {
                  return;
               }
            }

            if (!client.player.isUsingItem()) {
               boolean useHeld;
               if (config.crystalKeyMode == 1) {
                  useHeld = config.crystalActivateKey > 0 && KeyInput.isPressed(client, config.crystalActivateKey);
               } else if (config.crystalKeyMode == 2) {
                  useHeld = client.options.useKey.isPressed() || config.crystalActivateKey > 0 && KeyInput.isPressed(client, config.crystalActivateKey);
               } else {
                  useHeld = client.options.useKey.isPressed();
                  if (!useHeld && config.crystalActivateKey > 0 && config.crystalActivateKey != 1) {
                     useHeld = KeyInput.isPressed(client, config.crystalActivateKey);
                  }
               }

               if (!useHeld) {
                  useWasHeld = false;
                  pendingCrystalBase = null;
                  cooldownCounter = 0;
                  // PRO FIX: tus birakildiginda yarim kalan obsidyen islemi temizlenir
                  if (placingObsidian) {
                     placingObsidian = false;
                     pendingObsidian = null;
                     switchDelayCounter = 0;
                     if (previousSlotBeforeObsidian >= 0) {
                        InventoryUtil.setSelectedSlot(client, previousSlotBeforeObsidian);
                        previousSlotBeforeObsidian = -1;
                     }
                  }
               } else {
                  if (!useWasHeld) {
                     useWasHeld = true;
                     cooldownCounter = 0;
                  }

                  if (placingObsidian) {
                     if (switchDelayCounter <= 0 && pendingObsidian != null) {
                        finishObsidian(client, config);
                     }
                  } else if (pendingCrystalBase != null) {
                     continueObsidianSequence(client, config);
                  } else {
                     Hand crystalHand = getCrystalHand(client, config);
                     if (crystalHand != null) {
                        double targetCps = Math.max(1.0, (double)config.crystalMaxCps);
                        int cycles;
                        if (targetCps <= 20.0) {
                           int intervalTicks = Math.max(1, (int)Math.round(20.0 / targetCps));
                           if (tickCounter % intervalTicks != 0L) {
                              return;
                           }

                           cycles = 1;
                        } else {
                           double cyclesPerTick = targetCps / 20.0;
                           int base = (int)Math.floor(cyclesPerTick);
                           double fraction = cyclesPerTick - base;
                           cycles = base + (random.nextDouble() < fraction ? 1 : 0);
                        }

                        for (int i = 0; i < cycles; i++) {
                           executeCycle(client, config, crystalHand);
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

   private static void executeCycle(MinecraftClient client, MacroConfig config, Hand crystalHand) {
      HitResult target = client.crosshairTarget;
      if (target instanceof BlockHitResult blockHit) {
         onBlock(client, config, blockHit, crystalHand);
      } else if (target instanceof EntityHitResult entityHit) {
         onEntity(client, config, entityHit, crystalHand);
      }
   }

   private static Hand getCrystalHand(MinecraftClient client, MacroConfig config) {
      if (client.player == null) {
         return null;
      } else if (client.player.getMainHandStack().isOf(Items.END_CRYSTAL)) {
         return Hand.MAIN_HAND;
      } else {
         return client.player.getOffHandStack().isOf(Items.END_CRYSTAL) ? Hand.OFF_HAND : null;
      }
   }

   private static void tickCounters() {
      if (placeDelayCounter > 0) {
         placeDelayCounter--;
      }

      if (breakDelayCounter > 0) {
         breakDelayCounter--;
      }

      if (switchDelayCounter > 0) {
         switchDelayCounter--;
      }

      if (cooldownCounter > 0) {
         cooldownCounter--;
      }

      tickCounter++;
   }

   private static void checkStopOnKill(MinecraftClient client, MacroConfig config) {
      if (client.world != null && client.player != null) {
         double range = config.crystalStopRange;
         Box area = new Box(
            client.player.getX() - range,
            client.player.getY() - range,
            client.player.getZ() - range,
            client.player.getX() + range,
            client.player.getY() + range,
            client.player.getZ() + range
         );

         for (Entity entity : client.world.getOtherEntities(client.player, area)) {
            if (entity instanceof LivingEntity living
               && living != client.player
               && (living.isDead() || living.getHealth() <= 0.0F)
               && knownDeadEntities.add(living.getId())) {
               pausedUntil = Math.max(pausedUntil, System.currentTimeMillis() + (long)(config.crystalStopTime * 1000.0));
               break;
            }
         }

         knownDeadEntities.removeIf(id -> client.world.getEntityById(id) == null);
      }
   }

   private static void onBlock(MinecraftClient client, MacroConfig config, BlockHitResult blockHit, Hand crystalHand) {
      if (blockHit.getType() == Type.BLOCK) {
         if (!placingObsidian) {
            BlockPos pos = blockHit.getBlockPos();
            boolean solidEnough = BlockInteract.isBlockAt(client, pos, Blocks.OBSIDIAN) || BlockInteract.isBlockAt(client, pos, Blocks.BEDROCK);
            if (!solidEnough) {
               if (config.crystalPlaceObsidian && cooldownCounter <= 0) {
                  startObsidian(client, config, blockHit);
               }
            } else if (!lootNearby(client, pos.up())) {
               EndCrystalEntity existing = getCrystalOnBlock(client, pos);
               if (existing != null && breakDelayCounter <= 0) {
                  client.interactionManager.attackEntity(client.player, existing);
                  client.player.swingHand(Hand.MAIN_HAND);
                  breakDelayCounter = DelayUtil.withRandomness(config.crystalBreakDelay, config);
               }

               if (existing == null && placeDelayCounter <= 0 && canPlaceCrystal(client, pos)
                     && selfSafe(client, config, pos)) {
                  ActionResult res = BlockInteract.interactBlock(client, crystalHand, blockHit);
                  if (res.isAccepted()) {
                     client.player.swingHand(crystalHand);
                     KeyInput.setItemUseCooldown(client, 0);
                     placeDelayCounter = DelayUtil.withRandomness(config.crystalPlaceDelay, config);
                  }
               }
            }
         }
      }
   }

   /**
    * PRO 3.6.2 - kendine hasar guvenligi. Kristal/patlama merkezi oyuncuya cok
    * yakinsa yerlestirme yapilmaz (varsayilan 2.0 blok, ayarlardan degisir).
    */
   private static boolean selfSafe(MinecraftClient client, MacroConfig config, BlockPos pos) {
      if (client.player == null || config == null || !config.proCrystalSelfSafe) {
         return true;
      }
      Vec3d center = new Vec3d((double)pos.getX() + 0.5, (double)pos.getY() + 1.0, (double)pos.getZ() + 0.5);
      return client.player.getEyePos().distanceTo(center) >= config.proCrystalMinSelfDistance;
   }

   private static void startObsidian(MinecraftClient client, MacroConfig config, BlockHitResult blockHit) {
      if (client.player != null && client.world != null) {
         int slot = InventoryUtil.findHotbarSlot(client, Items.OBSIDIAN);
         if (slot != -1) {
            previousSlotBeforeObsidian = InventoryUtil.getSelectedSlot(client);
            InventoryUtil.setSelectedSlot(client, slot);
            placingObsidian = true;
            pendingObsidian = blockHit;
            switchDelayCounter = DelayUtil.withRandomness(config.crystalObsidianSwitchDelay, config);
         }
      }
   }

   private static void finishObsidian(MinecraftClient client, MacroConfig config) {
      BlockHitResult hit = pendingObsidian;
      placingObsidian = false;
      pendingObsidian = null;
      if (hit == null) {
         restoreObsidianSlot(client);
         return;
      }

      // BlockItem replaces a replaceable target, otherwise places on its clicked face.
      BlockPos clicked = hit.getBlockPos();
      BlockPos base = client.world.getBlockState(clicked).isReplaceable()
         ? clicked : clicked.offset(hit.getSide());
      ActionResult result = BlockInteract.interactBlock(client, Hand.MAIN_HAND, hit);
      restoreObsidianSlot(client);
      cooldownCounter = Math.max(1, (int)(config.crystalObsidianCooldown * 20.0));
      if (!result.isAccepted()) {
         return;
      }
      client.player.swingHand(Hand.MAIN_HAND);
      // Wait for the world update and crystal entity spawn; do not reuse the old hit.
      pendingCrystalBase = base.toImmutable();
      pendingCrystalDeadline = tickCounter + 40;
   }

   private static void restoreObsidianSlot(MinecraftClient client) {
      if (previousSlotBeforeObsidian >= 0) {
         InventoryUtil.setSelectedSlot(client, previousSlotBeforeObsidian);
         previousSlotBeforeObsidian = -1;
      }
   }

   private static void continueObsidianSequence(MinecraftClient client, MacroConfig config) {
      BlockPos base = pendingCrystalBase;
      Vec3d top = new Vec3d(base.getX() + 0.5, base.getY() + 1.0, base.getZ() + 0.5);
      if (tickCounter > pendingCrystalDeadline
            || client.player.getEyePos().squaredDistanceTo(top) > 20.25) {
         pendingCrystalBase = null;
         return;
      }
      if (!BlockInteract.isBlockAt(client, base, Blocks.OBSIDIAN)) {
         return;
      }
      if (lootNearby(client, base.up()) || !selfSafe(client, config, base)) {
         pendingCrystalBase = null;
         return;
      }
      EndCrystalEntity crystal = getCrystalOnBlock(client, base);
      if (crystal != null) {
         if (breakDelayCounter <= 0) {
            client.interactionManager.attackEntity(client.player, crystal);
            client.player.swingHand(Hand.MAIN_HAND);
            breakDelayCounter = DelayUtil.withRandomness(config.crystalBreakDelay, config);
            pendingCrystalBase = null;
         }
         return;
      }
      Hand hand = getCrystalHand(client, config);
      if (hand == null || placeDelayCounter > 0 || !canPlaceCrystal(client, base)) {
         return;
      }
      BlockHitResult hit = new BlockHitResult(top, Direction.UP, base, false);
      ActionResult result = BlockInteract.interactBlock(client, hand, hit);
      if (result.isAccepted()) {
         client.player.swingHand(hand);
         KeyInput.setItemUseCooldown(client, 0);
         // At most one placement per tick, even when the configured delay is zero.
         placeDelayCounter = Math.max(1, DelayUtil.withRandomness(config.crystalPlaceDelay, config));
      }
   }

   private static void onEntity(MinecraftClient client, MacroConfig config, EntityHitResult entityHit, Hand crystalHand) {
      if (!placingObsidian) {
         if (client.player != null && client.interactionManager != null) {
            Entity entity = entityHit.getEntity();
            if (entity != null && !entity.isRemoved() && entity.isAlive()) {
               if (entity instanceof EndCrystalEntity crystal || entity.getClass().getSimpleName().contains("Slime")) {
                  if (!lootNearby(client, entity.getBlockPos())) {
                     if (breakDelayCounter <= 0) {
                        client.interactionManager.attackEntity(client.player, entity);
                        client.player.swingHand(Hand.MAIN_HAND);
                        breakDelayCounter = DelayUtil.withRandomness(config.crystalBreakDelay, config);
                     }

                     BlockPos below = entity.getBlockPos().down();
                     if (placeDelayCounter <= 0
                        && selfSafe(client, config, below)
                        && (BlockInteract.isBlockAt(client, below, Blocks.OBSIDIAN) || BlockInteract.isBlockAt(client, below, Blocks.BEDROCK))) {
                        BlockHitResult bhr = new BlockHitResult(
                           new Vec3d(below.getX() + 0.5, below.getY() + 1.0, below.getZ() + 0.5), Direction.UP, below, false
                        );
                        ActionResult res = BlockInteract.interactBlock(client, crystalHand, bhr);
                        if (res.isAccepted()) {
                           client.player.swingHand(crystalHand);
                           KeyInput.setItemUseCooldown(client, 0);
                           placeDelayCounter = DelayUtil.withRandomness(config.crystalPlaceDelay, config);
                        }
                     }
                  }
               }
            }
         }
      }
   }

   private static EndCrystalEntity getCrystalOnBlock(MinecraftClient client, BlockPos pos) {
      if (client.world != null && client.player != null) {
         BlockPos above = pos.up();
         Box box = new Box(above.getX() - 0.1, above.getY(), above.getZ() - 0.1, above.getX() + 1.1, above.getY() + 2.0, above.getZ() + 1.1);

         for (Entity e : client.world.getOtherEntities(client.player, box)) {
            if (e instanceof EndCrystalEntity crystal && crystal.isAlive() && !crystal.isRemoved()) {
               return crystal;
            }
         }

         return null;
      } else {
         return null;
      }
   }

   private static boolean canPlaceCrystal(MinecraftClient client, BlockPos pos) {
      if (client.world == null
            || !(BlockInteract.isBlockAt(client, pos, Blocks.OBSIDIAN)
               || BlockInteract.isBlockAt(client, pos, Blocks.BEDROCK))) {
         return false;
      } else {
         BlockPos above = pos.up();
         if (!client.world.getBlockState(above).isAir()) {
            return false;
         } else {
            int x = above.getX();
            int y = above.getY();
            int z = above.getZ();
            return client.world.getOtherEntities(null, new Box(x, y, z, x + 1.0, y + 2.0, z + 1.0)).isEmpty();
         }
      }
   }

   private static boolean lootNearby(MinecraftClient client, BlockPos pos) {
      if (client.world != null && pos != null) {
         Box area = new Box(pos.getX() - 10.0, pos.getY() - 10.0, pos.getZ() - 10.0, pos.getX() + 10.0, pos.getY() + 10.0, pos.getZ() + 10.0);

         for (Entity entity : client.world.getOtherEntities(null, area)) {
            if (entity instanceof ItemEntity item) {
               ItemStack stack = item.getStack();
               if (!stack.isEmpty()) {
                  if (stack.isOf(Items.ELYTRA)) {
                     return true;
                  }

                  if (stack.isIn(ItemTags.SWORDS)) {
                     return true;
                  }

                  if (stack.isIn(ItemTags.HEAD_ARMOR) || stack.isIn(ItemTags.CHEST_ARMOR) || stack.isIn(ItemTags.LEG_ARMOR) || stack.isIn(ItemTags.FOOT_ARMOR)) {
                     return true;
                  }
               }
            }
         }

         return false;
      } else {
         return false;
      }
   }
}
