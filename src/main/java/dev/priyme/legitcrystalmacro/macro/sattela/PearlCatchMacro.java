package dev.priyme.legitcrystalmacro.macro.sattela;

import dev.priyme.legitcrystalmacro.config.MacroConfig;
import dev.priyme.legitcrystalmacro.mixin.MinecraftClientAccessor;
import dev.priyme.legitcrystalmacro.util.FriendManager;
import dev.priyme.legitcrystalmacro.util.InventoryUtil;
import dev.priyme.legitcrystalmacro.util.KeyInput;
import java.util.HashSet;
import java.util.Set;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.projectile.thrown.EnderPearlEntity;
import net.minecraft.item.Items;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.hit.HitResult.Type;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.RaycastContext;
import net.minecraft.world.RaycastContext.FluidHandling;
import net.minecraft.world.RaycastContext.ShapeType;

public final class PearlCatchMacro {
   private static final Set<Integer> handledPearlIds = new HashSet<>();
   private static final Set<Integer> myPearlIds = new HashSet<>();
   private static boolean wasKeyPressed = false;
   private static long lastThrowTime = 0L;
   private static int initialSlot = -1;
   private static float originalYaw = 0.0F;
   private static float originalPitch = 0.0F;
   private static int throwState = 0;

   private PearlCatchMacro() {
   }

   public static void reset() {
      handledPearlIds.clear();
      myPearlIds.clear();
      wasKeyPressed = false;
      initialSlot = -1;
      throwState = 0;
   }

   public static void tick(MinecraftClient client, MacroConfig config) {
      if (config == null || !config.sattelaPearlCatchEnabled) {
         reset();
      } else if (client.player == null || client.world == null || client.interactionManager == null) {
         reset();
      } else if (KeyInput.isScreenNull(client)) {
         if (throwState == 1) {
            if (initialSlot != -1) {
               InventoryUtil.setSelectedSlot(client, initialSlot);
               initialSlot = -1;
            }

            if (config.pearlCatchRestoreRotation) {
               client.player.setYaw(originalYaw);
               client.player.setPitch(originalPitch);
            }

            throwState = 0;
         } else {
            boolean isKeyDown = config.sattelaPearlCatchKey > 0 && KeyInput.isPressed(client, config.sattelaPearlCatchKey);
            boolean keyTriggered = isKeyDown && !wasKeyPressed;
            wasKeyPressed = isKeyDown;
            boolean shouldTrigger = keyTriggered || !config.sattelaPearlCatchUseKeybind;
            if (shouldTrigger) {
               long now = System.currentTimeMillis();
               if (now - lastThrowTime >= config.sattelaPearlCatchDelay) {
                  EnderPearlEntity targetPearl = findTargetPearl(client, config);
                  if (targetPearl != null) {
                     int pearlSlot = InventoryUtil.findHotbarSlot(client, Items.ENDER_PEARL);
                     if (pearlSlot != -1) {
                        Vec3d targetLanding = predictPearlLanding(client, targetPearl);
                        float[] aimAngles = solveThrowAngles(client, targetLanding);
                        if (aimAngles != null) {
                           handledPearlIds.add(targetPearl.getId());
                           initialSlot = InventoryUtil.getSelectedSlot(client);
                           originalYaw = client.player.getYaw();
                           originalPitch = client.player.getPitch();
                           InventoryUtil.setSelectedSlot(client, pearlSlot);
                           client.player.setYaw(aimAngles[0]);
                           client.player.setPitch(aimAngles[1]);
                           ((MinecraftClientAccessor)client).setItemUseCooldown(0);
                           client.interactionManager.interactItem(client.player, Hand.MAIN_HAND);
                           client.player.swingHand(Hand.MAIN_HAND);
                           lastThrowTime = now;
                           throwState = 1;
                        }
                     }
                  }
               }
            }
         }
      }
   }

   private static EnderPearlEntity findTargetPearl(MinecraftClient client, MacroConfig config) {
      if (client.player != null && client.world != null) {
         double radius = config.pearlCatchSearchRange > 0.0 ? config.pearlCatchSearchRange : 48.0;
         Box searchBox = client.player.getBoundingBox().expand(radius);
         handledPearlIds.removeIf(id -> client.world.getEntityById(id) == null);
         myPearlIds.removeIf(id -> client.world.getEntityById(id) == null);
         EnderPearlEntity bestPearl = null;
         double minDistanceSq = radius * radius;
         Vec3d myEye = client.player.getEyePos();

         for (Entity entity : client.world.getOtherEntities(client.player, searchBox)) {
            if (entity instanceof EnderPearlEntity pearl
               && !pearl.isRemoved()
               && pearl.isAlive()
               && !handledPearlIds.contains(pearl.getId())
               && !myPearlIds.contains(pearl.getId())) {
               Entity owner = pearl.getOwner();
               if (owner != null) {
                  if (owner == client.player) {
                     myPearlIds.add(pearl.getId());
                     continue;
                  }

                  if (owner instanceof LivingEntity living && FriendManager.isFriend(living.getName().getString())) {
                     continue;
                  }

                  if (config.pearlCatchHealthCheck && owner instanceof LivingEntity opponent) {
                     float myHealth = client.player.getHealth() + client.player.getAbsorptionAmount();
                     float opponentHealth = opponent.getHealth() + opponent.getAbsorptionAmount();
                     if (myHealth < opponentHealth) {
                        handledPearlIds.add(pearl.getId());
                        continue;
                     }
                  }
               } else if (pearl.age <= 3 && pearl.squaredDistanceTo(myEye) < 4.0) {
                  myPearlIds.add(pearl.getId());
                  continue;
               }

               double distSq = client.player.squaredDistanceTo(pearl);
               if (distSq < minDistanceSq) {
                  minDistanceSq = distSq;
                  bestPearl = pearl;
               }
            }
         }

         return bestPearl;
      } else {
         return null;
      }
   }

   private static Vec3d predictPearlLanding(MinecraftClient client, EnderPearlEntity pearl) {
      Vec3d pos = new Vec3d(pearl.getX(), pearl.getY(), pearl.getZ());
      Vec3d vel = pearl.getVelocity();

      for (int i = 0; i < 120; i++) {
         Vec3d next = pos.add(vel);
         HitResult hit = client.world.raycast(new RaycastContext(pos, next, ShapeType.COLLIDER, FluidHandling.NONE, pearl));
         if (hit.getType() != Type.MISS) {
            return hit.getPos();
         }

         pos = next;
         vel = vel.multiply(0.99).subtract(0.0, 0.03, 0.0);
      }

      return pos;
   }

   private static float[] solveThrowAngles(MinecraftClient client, Vec3d target) {
      if (client.player == null) {
         return null;
      } else {
         Vec3d myEye = client.player.getEyePos();
         Vec3d diff = target.subtract(myEye);
         float yaw = (float)Math.toDegrees(Math.atan2(diff.z, diff.x)) - 90.0F;
         float bestPitch = -45.0F;
         double minErrSq = Double.MAX_VALUE;

         for (float pitch = -85.0F; pitch <= 45.0F; pitch += 2.5F) {
            float f = (float)(-Math.sin(Math.toRadians(yaw)) * Math.cos(Math.toRadians(pitch)));
            float g = (float)(-Math.sin(Math.toRadians(pitch)));
            float h = (float)(Math.cos(Math.toRadians(yaw)) * Math.cos(Math.toRadians(pitch)));
            Vec3d v = new Vec3d(f, g, h).normalize().multiply(1.5);
            Vec3d simPos = myEye;
            Vec3d simVel = v;

            for (int step = 0; step < 90; step++) {
               Vec3d next = simPos.add(simVel);
               HitResult hit = client.world.raycast(new RaycastContext(simPos, next, ShapeType.COLLIDER, FluidHandling.NONE, client.player));
               if (hit.getType() != Type.MISS) {
                  double errSq = hit.getPos().squaredDistanceTo(target);
                  if (errSq < minErrSq) {
                     minErrSq = errSq;
                     bestPitch = pitch;
                  }
                  break;
               }

               double errSq = next.squaredDistanceTo(target);
               if (errSq < minErrSq) {
                  minErrSq = errSq;
                  bestPitch = pitch;
               }

               simPos = next;
               simVel = simVel.multiply(0.99).subtract(0.0, 0.03, 0.0);
            }
         }

         return new float[]{yaw, bestPitch};
      }
   }
}
