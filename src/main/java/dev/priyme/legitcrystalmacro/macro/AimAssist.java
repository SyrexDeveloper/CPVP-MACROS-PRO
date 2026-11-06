package dev.priyme.legitcrystalmacro.macro;

import dev.priyme.legitcrystalmacro.config.MacroConfig;
import dev.priyme.legitcrystalmacro.util.FriendManager;
import dev.priyme.legitcrystalmacro.util.KeyInput;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

public final class AimAssist {
   private AimAssist() {
   }

   public static void tick(MinecraftClient client, MacroConfig config) {
      if (config != null && config.aimAssistEnabled) {
         if (client.player != null && client.world != null) {
            if (KeyInput.isScreenNull(client)) {
               if (config.aimAssistOnlyWhileMoving) {
                  boolean isMoving = client.options.forwardKey.isPressed()
                     || client.options.backKey.isPressed()
                     || client.options.leftKey.isPressed()
                     || client.options.rightKey.isPressed()
                     || client.player.getVelocity().horizontalLengthSquared() > 5.0E-4;
                  if (!isMoving) {
                     return;
                  }
               }

               Vec3d eyePos = client.player.getEyePos();
               Vec3d lookVec = client.player.getRotationVec(1.0F).normalize();
               double maxRange = Math.max(1.0, config.aimAssistRange);
               Box searchBox = client.player.getBoundingBox().expand(maxRange);
               LivingEntity bestTarget = null;
               Vec3d bestBone = null;
               double bestAngle = config.aimAssistFov;
               boolean onHitbox = false;

               for (Entity e : client.world.getOtherEntities(client.player, searchBox)) {
                  if (e instanceof LivingEntity living
                     && living.isAlive()
                     && !living.isRemoved()
                     && !(
                        living instanceof PlayerEntity player
                           ? player.isCreative()
                              || player.isSpectator()
                              || FriendManager.isFriend(player.getName().getString())
                              || config.aimAssistTargetMode == 1
                           : config.aimAssistTargetMode == 0
                     )) {
                     double distSq = client.player.squaredDistanceTo(living);
                     if (!(distSq > maxRange * maxRange)) {
                        Box box = living.getBoundingBox().expand(config.aimAssistDeadzone);
                        Vec3d reachEnd = eyePos.add(lookVec.multiply(maxRange));
                        boolean hitHitbox = box.raycast(eyePos, reachEnd).isPresent();
                        Vec3d[] bones = new Vec3d[]{
                           living.getEyePos(),
                           new Vec3d(living.getX(), living.getY() + living.getHeight() * 0.75, living.getZ()),
                           new Vec3d(living.getX(), living.getY() + living.getHeight() * 0.5, living.getZ()),
                           new Vec3d(living.getX(), living.getY() + living.getHeight() * 0.15, living.getZ())
                        };
                        Vec3d selectedBone;
                        if (config.aimAssistBoneMode == 1) {
                           selectedBone = bones[0];
                        } else if (config.aimAssistBoneMode == 2) {
                           selectedBone = bones[1];
                        } else {
                           Vec3d closest = bones[0];
                           double maxDot = -Double.MAX_VALUE;

                           for (Vec3d b : bones) {
                              Vec3d dir = b.subtract(eyePos);
                              double l = dir.length();
                              if (!(l < 1.0E-4)) {
                                 double dot = lookVec.dotProduct(dir.multiply(1.0 / l));
                                 if (dot > maxDot) {
                                    maxDot = dot;
                                    closest = b;
                                 }
                              }
                           }

                           selectedBone = closest;
                        }

                        if (hitHitbox) {
                           bestTarget = living;
                           bestBone = selectedBone;
                           onHitbox = true;
                           bestAngle = 0.0;
                           break;
                        }

                        Vec3d toTarget = selectedBone.subtract(eyePos);
                        double dist = toTarget.length();
                        if (!(dist < 1.0E-4)) {
                           double dot = MathHelper.clamp(lookVec.dotProduct(toTarget.multiply(1.0 / dist)), -1.0, 1.0);
                           double angle = Math.toDegrees(Math.acos(dot));
                           if (angle <= config.aimAssistFov && angle < bestAngle) {
                              bestAngle = angle;
                              bestTarget = living;
                              bestBone = selectedBone;
                           }
                        }
                     }
                  }
               }

               if (bestTarget != null && bestBone != null) {
                  double dx = bestBone.x - eyePos.x;
                  double dy = bestBone.y - eyePos.y;
                  double dz = bestBone.z - eyePos.z;
                  double distXZ = Math.sqrt(dx * dx + dz * dz);
                  float targetYaw = (float)Math.toDegrees(Math.atan2(dz, dx)) - 90.0F;
                  float targetPitch = (float)(-Math.toDegrees(Math.atan2(dy, distXZ)));
                  float curYaw = client.player.getYaw();
                  float curPitch = client.player.getPitch();
                  float diffYaw = MathHelper.wrapDegrees(targetYaw - curYaw);
                  float diffPitch = MathHelper.wrapDegrees(targetPitch - curPitch);
                  double t = 1.0 - bestAngle / Math.max(1.0, config.aimAssistFov);
                  t = MathHelper.clamp(t, 0.0, 1.0);
                  double smoothFactor = t * t * (3.0 - 2.0 * t);
                  double trackingMult = onHitbox ? config.aimAssistTrackingScale : 1.0;
                  float speed = (float)(config.aimAssistSmoothingSpeed * smoothFactor * config.aimAssistMicroAdjustment * trackingMult);
                  float stepYaw;
                  float stepPitch;
                  if (config.aimAssistGcdSmoothing) {
                     double sens = (Double)client.options.getMouseSensitivity().getValue() * 0.6 + 0.2;
                     double gcd = sens * sens * sens * 8.0 * 0.15;
                     if (gcd < 0.001) {
                        gcd = 0.001;
                     }

                     stepYaw = (float)(Math.round(diffYaw * speed / gcd) * gcd);
                     stepPitch = (float)(Math.round(diffPitch * speed * config.aimAssistPitchWeight / gcd) * gcd);
                  } else {
                     stepYaw = diffYaw * speed;
                     stepPitch = (float)(diffPitch * speed * config.aimAssistPitchWeight);
                  }

                  if (Math.abs(stepYaw) > 0.001F || Math.abs(stepPitch) > 0.001F) {
                     client.player.setYaw(curYaw + stepYaw);
                     client.player.setPitch(MathHelper.clamp(curPitch + stepPitch, -90.0F, 90.0F));
                  }
               }
            }
         }
      }
   }
}
