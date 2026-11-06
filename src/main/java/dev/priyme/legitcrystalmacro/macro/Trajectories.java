package dev.priyme.legitcrystalmacro.macro;

import dev.priyme.legitcrystalmacro.config.MacroConfig;
import java.util.ArrayList;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.Camera;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.ChargedProjectilesComponent;
import net.minecraft.entity.Entity;
import net.minecraft.entity.projectile.AbstractWindChargeEntity;
import net.minecraft.entity.projectile.ExplosiveProjectileEntity;
import net.minecraft.entity.projectile.FireworkRocketEntity;
import net.minecraft.entity.projectile.FishingBobberEntity;
import net.minecraft.entity.projectile.PersistentProjectileEntity;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.entity.projectile.TridentEntity;
import net.minecraft.entity.projectile.thrown.EggEntity;
import net.minecraft.entity.projectile.thrown.EnderPearlEntity;
import net.minecraft.entity.projectile.thrown.ExperienceBottleEntity;
import net.minecraft.entity.projectile.thrown.PotionEntity;
import net.minecraft.entity.projectile.thrown.SnowballEntity;
import net.minecraft.item.BowItem;
import net.minecraft.item.CrossbowItem;
import net.minecraft.item.EggItem;
import net.minecraft.item.EnderPearlItem;
import net.minecraft.item.ExperienceBottleItem;
import net.minecraft.item.FishingRodItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.SnowballItem;
import net.minecraft.item.SplashPotionItem;
import net.minecraft.item.TridentItem;
import net.minecraft.item.WindChargeItem;
import net.minecraft.registry.tag.FluidTags;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult.Type;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.RaycastContext;
import net.minecraft.world.World;
import net.minecraft.world.RaycastContext.FluidHandling;
import net.minecraft.world.RaycastContext.ShapeType;

public final class Trajectories {
   private static final boolean MODERN = true;
   static final int MODE_POST = 0;
   static final int MODE_THROWN = 1;
   static final int MODE_ACCEL = 2;
   static final int MODE_ROCKET_FREE = 3;
   static final int MODE_ROCKET_ANGLE = 4;
   static final int MODE_FISHING = 5;
   private static final Map<Entity, Trajectories.Flight> flights = new IdentityHashMap<>();
   private static final Trajectories.Physics ARROW = new Trajectories.Physics(0.05, 0.99, 0.6, 0);
   private static final Trajectories.Physics TRIDENT = new Trajectories.Physics(0.05, 0.99, 0.99, 0);
   private static final Trajectories.Physics THROWN = new Trajectories.Physics(0.03, 0.99, 0.8, 1);
   private static final Trajectories.Physics POTION = new Trajectories.Physics(0.05, 0.99, 0.8, 1);
   private static final Trajectories.Physics XP = new Trajectories.Physics(0.07, 0.99, 0.8, 1);
   private static final Trajectories.Physics WIND = new Trajectories.Physics(0.0, 1.0, 1.0, 2, 0.0);
   private static final Trajectories.Physics FISHING = new Trajectories.Physics(0.03, 0.92, 0.92, 5);
   private static final Trajectories.Physics ROCKET_FREE = new Trajectories.Physics(0.0, 1.0, 1.0, 3);
   private static final Trajectories.Physics ROCKET_ANGLE = new Trajectories.Physics(0.0, 1.0, 1.0, 4);

   public static void reset() {
      flights.clear();
   }

   private Trajectories() {
   }

   public static List<Trajectories.Path> paths(MinecraftClient client, Camera camera, float delta) {
      MacroConfig config = MacroConfig.get();
      if (config != null && config.trajectoriesEnabled && client.player != null && client.world != null) {
         List<Trajectories.Path> paths = new ArrayList<>();
         Vec3d start = new Vec3d(client.player.getX(), client.player.getEyeY() - 0.1, client.player.getZ());
         Vec3d direction = client.player.getRotationVec(1.0F);
         List<Trajectories.Launch> launches = held(client.player.getMainHandStack(), client, camera, start, direction);
         if (launches.isEmpty()) {
            launches = held(client.player.getOffHandStack(), client, camera, start, direction);
         }

         Vec3d motion = client.player.getVelocity();
         Vec3d inherited = new Vec3d(motion.x, client.player.isOnGround() ? 0.0 : motion.y, motion.z);
         int maxTicks = MathHelper.clamp(config.trajectoriesMaxTicks, 30, 300);

         for (Trajectories.Launch launch : launches) {
            paths.add(
               simulate(client.world, client.player, launch.position, launch.velocity.add(inherited), launch.physics, maxTicks, config.trajectoriesColor)
            );
         }

         Set<Entity> live = Collections.newSetFromMap(new IdentityHashMap<>());
         if (config.trajectoriesInFlight) {
            for (Entity entity : client.world.getEntities()) {
               if (entity.isAlive() && !entity.isRemoved() && !(entity.getVelocity().lengthSquared() < 1.0E-6)) {
                  Trajectories.Physics physics = flying(entity);
                  if (physics != null) {
                     Vec3d position = new Vec3d(entity.getX(), entity.getY(), entity.getZ());
                     live.add(entity);
                     Vec3d motionNow = entity.getVelocity();
                     Trajectories.Flight cached = flights.get(entity);
                     if (cached == null
                        || cached.age != entity.age
                        || !cached.position.equals(position)
                        || !cached.velocity.equals(motionNow)
                        || cached.maxTicks != maxTicks
                        || cached.color != config.trajectoriesInFlightColor) {
                        cached = new Trajectories.Flight(
                           entity.age,
                           position,
                           motionNow,
                           maxTicks,
                           config.trajectoriesInFlightColor,
                           simulate(client.world, entity, position, motionNow, physics, maxTicks, config.trajectoriesInFlightColor)
                        );
                        flights.put(entity, cached);
                     }

                     paths.add(cached.path);
                  }
               }
            }
         }

         flights.keySet().retainAll(live);
         return paths;
      } else {
         return List.of();
      }
   }

   private static List<Trajectories.Launch> held(ItemStack stack, MinecraftClient client, Camera camera, Vec3d start, Vec3d direction) {
      Item item = stack.getItem();
      List<Trajectories.Launch> launches = new ArrayList<>();
      if (item instanceof BowItem) {
         if (!client.player.isUsingItem()) {
            return launches;
         }

         float pull = BowItem.getPullProgress(client.player.getItemUseTime());
         if (pull >= 0.1F) {
            launches.add(new Trajectories.Launch(start, direction.multiply(3.0 * pull), ARROW));
         }
      } else if (item instanceof CrossbowItem) {
         if (!CrossbowItem.isCharged(stack)) {
            return launches;
         }

         ChargedProjectilesComponent loaded = (ChargedProjectilesComponent)stack.get(DataComponentTypes.CHARGED_PROJECTILES);
         boolean firework = loaded != null && loaded.contains(Items.FIREWORK_ROCKET);
         Vec3d velocity = direction.multiply(firework ? 1.6 : 3.15);
         Trajectories.Physics physics = firework ? ROCKET_ANGLE : ARROW;
         launches.add(new Trajectories.Launch(start, velocity, physics));
         if (loaded != null && loaded.getProjectiles().size() > 1) {
            launches.add(new Trajectories.Launch(start, velocity.rotateY((float)Math.toRadians(10.0)), physics));
            launches.add(new Trajectories.Launch(start, velocity.rotateY((float)Math.toRadians(-10.0)), physics));
         }
      } else if (item instanceof TridentItem) {
         if (client.player.isUsingItem() && client.player.getItemUseTime() >= 10) {
            launches.add(new Trajectories.Launch(start, direction.multiply(2.5), TRIDENT));
         }
      } else if (item instanceof SnowballItem || item instanceof EggItem || item instanceof EnderPearlItem) {
         launches.add(new Trajectories.Launch(start, direction.multiply(1.5), THROWN));
      } else if (item instanceof WindChargeItem) {
         launches.add(new Trajectories.Launch(start, direction.multiply(1.5), WIND));
      } else if (item instanceof SplashPotionItem) {
         launches.add(new Trajectories.Launch(start, direction(camera.getPitch() - 20.0F, camera.getYaw()).multiply(0.5), POTION));
      } else if (item instanceof ExperienceBottleItem) {
         launches.add(new Trajectories.Launch(start, direction(camera.getPitch() - 20.0F, camera.getYaw()).multiply(0.7), XP));
      } else if (item instanceof FishingRodItem && client.player.fishHook == null) {
         double pitch = Math.toRadians(camera.getPitch());
         double yaw = Math.toRadians(camera.getYaw());
         double y = MathHelper.clamp(-Math.cos(pitch) / Math.sin(pitch), -5.0, 5.0);
         Vec3d velocity = new Vec3d(-Math.sin(yaw), y, Math.cos(yaw));
         double length = velocity.length();
         if (length > 1.0E-6) {
            velocity = velocity.multiply(0.6 / length + 0.5);
         }

         launches.add(new Trajectories.Launch(start, velocity, FISHING));
      }

      return launches;
   }

   static Vec3d direction(float pitch, float yaw) {
      double p = Math.toRadians(pitch);
      double y = Math.toRadians(-yaw);
      double cos = Math.cos(p);
      return new Vec3d(Math.sin(y) * cos, -Math.sin(p), Math.cos(y) * cos);
   }

   private static Trajectories.Physics flying(Entity entity) {
      if (entity instanceof TridentEntity) {
         return TRIDENT;
      } else if (entity instanceof PersistentProjectileEntity) {
         return ARROW;
      } else if (entity instanceof SnowballEntity || entity instanceof EggEntity || entity instanceof EnderPearlEntity) {
         return THROWN;
      } else if (entity instanceof ExperienceBottleEntity) {
         return XP;
      } else if (entity instanceof PotionEntity) {
         return POTION;
      } else if (entity instanceof AbstractWindChargeEntity wind) {
         return new Trajectories.Physics(0.0, 1.0, 1.0, 2, wind.accelerationPower);
      } else if (entity instanceof FishingBobberEntity) {
         return FISHING;
      } else if (entity instanceof ExplosiveProjectileEntity fireball) {
         return new Trajectories.Physics(0.0, 0.95, 0.8, 2, fireball.accelerationPower);
      } else if (entity instanceof FireworkRocketEntity firework) {
         return firework.wasShotAtAngle() ? ROCKET_ANGLE : ROCKET_FREE;
      } else {
         return null;
      }
   }

   private static Trajectories.Path simulate(World world, Entity source, Vec3d position, Vec3d velocity, Trajectories.Physics physics, int maxTicks, int color) {
      List<Vec3d> points = new ArrayList<>(maxTicks + 1);
      points.add(position);
      boolean water = !physics.fishing() && source.isTouchingWater();
      Entity owner = source instanceof ProjectileEntity projectile ? projectile.getOwner() : source;

      for (int tick = 0; tick < maxTicks; tick++) {
         Trajectories.Step step = physics.step(position, velocity, water);
         Vec3d next = step.pos;
         Vec3d nextVelocity = step.vel;
         if (Double.isFinite(next.x)
            && Double.isFinite(next.y)
            && Double.isFinite(next.z)
            && !(Math.abs(next.x) > 2.9999984E7)
            && !(Math.abs(next.z) > 2.9999984E7)
            && !(next.squaredDistanceTo(position) > 4096.0)) {
            BlockHitResult block = world.raycast(
               new RaycastContext(position, next, ShapeType.COLLIDER, physics.fishing() ? FluidHandling.ANY : FluidHandling.NONE, source)
            );
            double nearest = block.getType() == Type.MISS ? Double.MAX_VALUE : position.squaredDistanceTo(block.getPos());
            Entity target = null;
            Vec3d entityHit = null;
            int projectileAge = source instanceof ProjectileEntity ? source.age + tick : tick;
            double margin = MathHelper.clamp((projectileAge - 2) / 20.0, 0.0, 0.3);

            for (Entity entity : world.getOtherEntities(
               source, new Box(position, next).expand(1.0), e -> e.canHit() && (owner == null || !owner.isConnectedThroughVehicle(e))
            )) {
               Vec3d hit = (Vec3d)entity.getBoundingBox().expand(margin).raycast(position, next).orElse(null);
               if (hit != null && position.squaredDistanceTo(hit) < nearest) {
                  nearest = position.squaredDistanceTo(hit);
                  target = entity;
                  entityHit = hit;
               }
            }

            if (entityHit != null) {
               points.add(entityHit);
               return new Trajectories.Path(points, true, target, color);
            }

            if (block.getType() != Type.MISS) {
               points.add(block.getPos());
               return new Trajectories.Path(points, true, null, color);
            }

            if (!physics.fishing()) {
               water = world.getFluidState(BlockPos.ofFloored(next)).isIn(FluidTags.WATER);
            }

            position = next;
            velocity = nextVelocity;
            points.add(next);
            if (!(next.y < world.getBottomY() - 20)) {
               continue;
            }
         }
         break;
      }

      return new Trajectories.Path(points, false, null, color);
   }

   public static List<Vec3d> smooth(List<Vec3d> points) {
      if (points.size() < 3) {
         return points;
      } else {
         List<Vec3d> result = new ArrayList<>();
         result.add(points.getFirst());

         for (int i = 0; i < points.size() - 1; i++) {
            Vec3d a = points.get(Math.max(0, i - 1));
            Vec3d b = points.get(i);
            Vec3d c = points.get(i + 1);
            Vec3d d = points.get(Math.min(points.size() - 1, i + 2));
            int steps = Math.max(2, Math.min(10, (int)Math.ceil(b.distanceTo(c) / 0.18)));

            for (int j = 1; j <= steps; j++) {
               double t = (double)j / steps;
               result.add(new Vec3d(spline(a.x, b.x, c.x, d.x, t), spline(a.y, b.y, c.y, d.y, t), spline(a.z, b.z, c.z, d.z, t)));
            }
         }

         result.set(result.size() - 1, points.getLast());
         return result;
      }
   }

   private static double spline(double a, double b, double c, double d, double t) {
      double value = 0.5 * (2.0 * b + (-a + c) * t + (2.0 * a - 5.0 * b + 4.0 * c - d) * t * t + (-a + 3.0 * b - 3.0 * c + d) * t * t * t);
      return MathHelper.clamp(value, Math.min(b, c), Math.max(b, c));
   }

   private record Flight(int age, Vec3d position, Vec3d velocity, int maxTicks, int color, Trajectories.Path path) {
   }

   private record Launch(Vec3d position, Vec3d velocity, Trajectories.Physics physics) {
   }

   public record Path(List<Vec3d> points, boolean landed, Entity target, int color) {
   }

   record Physics(double gravity, double drag, double waterDrag, int mode, double acceleration) {
      Physics(double gravity, double drag, double waterDrag, int mode) {
         this(gravity, drag, waterDrag, mode, 0.0);
      }

      boolean fishing() {
         return this.mode == 5;
      }

      Trajectories.Step step(Vec3d pos, Vec3d vel, boolean water) {
         double resistance = water ? this.waterDrag : this.drag;
         if (this.mode == 0) {
            Vec3d nextPos = pos.add(vel);
            Vec3d nextVel = new Vec3d(vel.x * resistance, vel.y * resistance - this.gravity, vel.z * resistance);
            return new Trajectories.Step(nextPos, nextVel);
         } else if (this.mode == 1) {
            Vec3d nextVel = new Vec3d(vel.x * resistance, (vel.y - this.gravity) * resistance, vel.z * resistance);
            return new Trajectories.Step(pos.add(nextVel), nextVel);
         } else if (this.mode == 2) {
            Vec3d accel = vel.lengthSquared() < 1.0E-10 ? Vec3d.ZERO : vel.normalize().multiply(this.acceleration);
            Vec3d nextVel = vel.add(accel).multiply(resistance);
            return new Trajectories.Step(pos.add(nextVel), nextVel);
         } else if (this.mode == 3) {
            Vec3d nextVel = new Vec3d(vel.x * 1.15, vel.y + 0.04, vel.z * 1.15);
            return new Trajectories.Step(pos.add(nextVel), nextVel);
         } else if (this.mode == 4) {
            return new Trajectories.Step(pos.add(vel), vel);
         } else if (this.mode == 5) {
            Vec3d withGrav = vel.add(0.0, -this.gravity, 0.0);
            return new Trajectories.Step(pos.add(withGrav), withGrav.multiply(resistance));
         } else {
            return new Trajectories.Step(pos.add(vel), vel);
         }
      }

      Vec3d advance(Vec3d velocity, boolean water) {
         return this.step(Vec3d.ZERO, velocity, water).vel;
      }
   }

   record Step(Vec3d pos, Vec3d vel) {
   }
}
