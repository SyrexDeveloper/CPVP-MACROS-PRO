package dev.priyme.legitcrystalmacro.macro.safeanchor;

import dev.priyme.legitcrystalmacro.config.MacroConfig;
import dev.priyme.legitcrystalmacro.mixin.MinecraftClientAccessor;
import dev.priyme.legitcrystalmacro.util.InventoryUtil;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.BooleanSupplier;
import java.util.function.IntSupplier;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.ShapeContext;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.Items;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket.LookAndOnGround;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult.Type;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;
import net.minecraft.world.RaycastContext;
import net.minecraft.world.World;
import net.minecraft.world.RaycastContext.FluidHandling;
import net.minecraft.world.RaycastContext.ShapeType;

public final class SafeAnchorSupport {
   private static Object slotOwner;
   private static long slotChanges;
   private static long interactions;
   private static int observedSlot = -1;
   private static boolean interacting;
   private static int interactionTick = Integer.MIN_VALUE;
   private static MinecraftClient activeClient;

   private SafeAnchorSupport() {
   }

   public static Vec3d position(Entity entity) {
      return new Vec3d(entity.getX(), entity.getY(), entity.getZ());
   }

   public static Vec3d previousPosition(Entity entity) {
      return new Vec3d(entity.lastX, entity.lastY, entity.lastZ);
   }

   public static boolean isOpaqueFullCube(MinecraftClient client, BlockState state, BlockPos pos) {
      return state.isOpaqueFullCube();
   }

   public static void observeInteraction(MinecraftClient client, ActionResult result) {
      if (!interacting && client.player != null && result != null && result.isAccepted()) {
         interactions++;
         interactionTick = client.player.age;
      }
   }

   public static void tick(MinecraftClient client) {
      activeClient = client;
      int slot = InventoryUtil.getSelectedSlot(client);
      if (slot != observedSlot) {
         observedSlot = slot;
         slotChanges++;
      }
   }

   public static int llllII(PlayerInventory inventory) {
      return InventoryUtil.getSelectedSlot(MinecraftClient.getInstance());
   }

   public static int lIlIIIl(MinecraftClient client) {
      return InventoryUtil.getSelectedSlot(client);
   }

   public static long lllllI() {
      return slotChanges;
   }

   public static long IIlIIlI() {
      return interactions;
   }

   public static int IIlIlII(MinecraftClient client) {
      return ((MinecraftClientAccessor)client).getItemUseCooldown();
   }

   public static void setUseCooldown(MinecraftClient client, int ticks) {
      ((MinecraftClientAccessor)client).setItemUseCooldown(ticks);
   }

   public static boolean IlIII(Object owner) {
      return slotOwner == owner;
   }

   public static void IIIllI(Object owner) {
      if (slotOwner == owner) {
         slotOwner = null;
      }
   }

   public static void IIIllI(MinecraftClient client, Object owner) {
      IIIllI(owner);
   }

   public static boolean lIlIIl(Object owner) {
      return slotOwner != null && slotOwner != owner;
   }

   public static void IIllll(MinecraftClient client, Object owner, int slot) {
      if (client.player != null) {
         InventoryUtil.setSelectedSlot(client, slot);
      }

      IIIllI(owner);
   }

   public static void lIIllIl(MinecraftClient client, Object owner, int ignored) {
      IIIllI(owner);
   }

   public static boolean llIll(MinecraftClient client, Object owner, int slot, boolean restore, BooleanSupplier action) {
      if (client.player != null && client.getNetworkHandler() != null && slot >= 0 && slot <= 8 && !lIlIIl(owner)) {
         int previous = InventoryUtil.getSelectedSlot(client);
         slotOwner = owner;
         if (previous != slot) {
            InventoryUtil.setSelectedSlot(client, slot);
            observedSlot = slot;
            slotChanges++;
         }

         boolean var6;
         try {
            var6 = action.getAsBoolean();
         } finally {
            if (restore) {
               IIllll(client, owner, previous);
            }
         }

         return var6;
      } else {
         return false;
      }
   }

   public static boolean lIlIl(MinecraftClient client, BlockHitResult hit) {
      if (client.player != null && client.interactionManager != null && hit != null && !interacting) {
         MacroConfig cfg = MacroConfig.get();
         if (cfg != null
            && cfg.safeAnchorEnabled
            && !cfg.safeAnchorExplodeUnsafe
            && !client.player.isCreative()
            && !client.world.getRegistryKey().equals(World.NETHER)
            && client.world.getBlockState(hit.getBlockPos()).isOf(Blocks.RESPAWN_ANCHOR)
            && !client.player.getMainHandStack().isOf(Items.GLOWSTONE)) {
            if (!client.player.isOnGround() || client.world.getBlockState(client.player.getBlockPos().down()).getBlock().getBlastResistance() < 600.0F) {
               return false;
            }

            float selfDamage = SafeAnchorSupport.AnchorDamage.lI(client.player, hit.getBlockPos(), null, position(client.player));
            if (selfDamage + 1.0F >= client.player.getHealth() + client.player.getAbsorptionAmount()) {
               return false;
            }
         }

         int cooldown = IIlIlII(client);
         boolean accepted = false;
         interacting = true;

         boolean var6;
         try {
            ((MinecraftClientAccessor)client).setItemUseCooldown(0);
            ActionResult result = client.interactionManager.interactBlock(client.player, Hand.MAIN_HAND, hit);
            accepted = result != null && result.isAccepted();
            if (accepted) {
               client.player.swingHand(Hand.MAIN_HAND);
               interactions++;
               interactionTick = client.player.age;
            }

            var6 = accepted;
         } finally {
            ((MinecraftClientAccessor)client).setItemUseCooldown(accepted ? Math.max(cooldown, 4) : cooldown);
            interacting = false;
         }

         return var6;
      } else {
         return false;
      }
   }

   public static boolean IIllII(BlockState state) {
      return state == null || state.isAir() || state.isOf(Blocks.WATER) || state.isOf(Blocks.LAVA);
   }

   public static BlockHitResult IlllIl(MinecraftClient client, Entity entity, Vec3d start, Vec3d end) {
      return client.world.raycast(new RaycastContext(start, end, ShapeType.OUTLINE, FluidHandling.NONE, entity));
   }

   public static boolean IlllIll(MinecraftClient client) {
      return client.options.useKey.isPressed() || client.options.attackKey.isPressed();
   }

   public static boolean lllII(MinecraftClient client) {
      return client.player != null && client.player.age == interactionTick && !interacting;
   }

   public static boolean IIIIlI(MinecraftClient client) {
      return client.player != null && client.player.isUsingItem();
   }

   public static boolean IllIIII() {
      return interacting;
   }

   public static void lIIII(KeyBinding key) {
      while (key.wasPressed()) {
      }
   }

   public static final class AnchorDamage {
      public static float lI(LivingEntity entity, BlockPos anchor, BlockPos shield, Vec3d predicted) {
         MinecraftClient client = MinecraftClient.getInstance();
         if (entity != null && anchor != null && client.world != null) {
            Vec3d origin = Vec3d.ofCenter(anchor);
            Vec3d current = new Vec3d(entity.getX(), entity.getY(), entity.getZ());
            Vec3d position = predicted == null ? current : predicted;
            double normalizedDistance = Math.sqrt(position.squaredDistanceTo(origin)) / 10.0;
            if (normalizedDistance > 1.0) {
               return 0.0F;
            } else {
               Box box = entity.getBoundingBox().offset(position.subtract(current));
               double exposure = exposure(client, entity, origin, box, anchor, shield);
               double impact = (1.0 - normalizedDistance) * exposure;
               float damage = (float)((impact * impact + impact) * 0.5 * 7.0 * 10.0 + 1.0);

               damage = switch (client.world.getDifficulty()) {
                  case PEACEFUL -> 0.0F;
                  case EASY -> Math.min(damage * 0.5F + 1.0F, damage);
                  case HARD -> damage * 1.5F;
                  default -> damage;
               };
               float armor = entity.getArmor();
               float toughness = (float)entity.getAttributeValue(EntityAttributes.ARMOR_TOUGHNESS);
               float effectiveArmor = MathHelper.clamp(armor - damage / (2.0F + toughness / 4.0F), armor * 0.2F, 20.0F);
               return damage * (1.0F - effectiveArmor / 25.0F);
            }
         } else {
            return 0.0F;
         }
      }

      private static float exposure(MinecraftClient client, Entity entity, Vec3d origin, Box box, BlockPos anchor, BlockPos shield) {
         double stepX = 1.0 / (box.getLengthX() * 2.0 + 1.0);
         double stepY = 1.0 / (box.getLengthY() * 2.0 + 1.0);
         double stepZ = 1.0 / (box.getLengthZ() * 2.0 + 1.0);
         double offsetX = (1.0 - Math.floor(1.0 / stepX) * stepX) * 0.5;
         double offsetZ = (1.0 - Math.floor(1.0 / stepZ) * stepZ) * 0.5;
         int visible = 0;
         int total = 0;

         for (double x = 0.0; x <= 1.0; x += stepX) {
            for (double y = 0.0; y <= 1.0; y += stepY) {
               for (double z = 0.0; z <= 1.0; z += stepZ) {
                  Vec3d point = new Vec3d(
                     MathHelper.lerp(x, box.minX, box.maxX) + offsetX, MathHelper.lerp(y, box.minY, box.maxY), MathHelper.lerp(z, box.minZ, box.maxZ) + offsetZ
                  );
                  boolean blocked = shield != null && intersects(point, origin, new Box(shield));
                  if (!blocked) {
                     BlockHitResult hit = client.world.raycast(new RaycastContext(point, origin, ShapeType.COLLIDER, FluidHandling.NONE, entity));
                     if (hit.getType() == Type.MISS || hit.getBlockPos().equals(anchor)) {
                        visible++;
                     }
                  }

                  total++;
               }
            }
         }

         return total == 0 ? 0.0F : (float)visible / total;
      }

      private static boolean intersects(Vec3d a, Vec3d b, Box box) {
         double tMin = 0.0;
         double tMax = 1.0;
         double[] starts = new double[]{a.x, a.y, a.z};
         double[] deltas = new double[]{b.x - a.x, b.y - a.y, b.z - a.z};
         double[] mins = new double[]{box.minX, box.minY, box.minZ};
         double[] maxs = new double[]{box.maxX, box.maxY, box.maxZ};

         for (int axis = 0; axis < 3; axis++) {
            if (Math.abs(deltas[axis]) < 1.0E-9) {
               if (starts[axis] < mins[axis] || starts[axis] > maxs[axis]) {
                  return false;
               }
            } else {
               double first = (mins[axis] - starts[axis]) / deltas[axis];
               double second = (maxs[axis] - starts[axis]) / deltas[axis];
               tMin = Math.max(tMin, Math.min(first, second));
               tMax = Math.min(tMax, Math.max(first, second));
               if (tMin > tMax) {
                  return false;
               }
            }
         }

         return true;
      }
   }

   public static final class AnchorGeometry {
      public static boolean llI() {
         return !SafeAnchorSupport.interacting;
      }

      public static Box lIl(World world, BlockPos pos) {
         VoxelShape shape = world.getBlockState(pos).getOutlineShape(world, pos);
         return shape.isEmpty() ? new Box(pos) : shape.getBoundingBox().offset(pos);
      }

      public static boolean lII(World world, BlockPos pos, Vec3d eyes, Vec3d direction, double reach) {
         return world != null
            && pos != null
            && eyes != null
            && direction != null
            && lIl(world, pos).raycast(eyes, eyes.add(direction.multiply(reach))).isPresent();
      }

      public static boolean lll(Vec3d eyes, Box box, Direction side) {
         if (eyes == null || box == null || side == null) {
            return false;
         } else if (box.contains(eyes)) {
            return true;
         } else {
            return switch (side) {
               case UP -> eyes.y >= box.maxY;
               case DOWN -> eyes.y <= box.minY;
               case NORTH -> eyes.z <= box.minZ;
               case SOUTH -> eyes.z >= box.maxZ;
               case WEST -> eyes.x <= box.minX;
               case EAST -> eyes.x >= box.maxX;
               default -> throw new MatchException(null, null);
            };
         }
      }

      private static Vec3d face(Box box, Direction side) {
         Vec3d c = box.getCenter();

         return switch (side) {
            case UP -> new Vec3d(c.x, box.maxY, c.z);
            case DOWN -> new Vec3d(c.x, box.minY, c.z);
            case NORTH -> new Vec3d(c.x, c.y, box.minZ);
            case SOUTH -> new Vec3d(c.x, c.y, box.maxZ);
            case WEST -> new Vec3d(box.minX, c.y, c.z);
            case EAST -> new Vec3d(box.maxX, c.y, c.z);
            default -> throw new MatchException(null, null);
         };
      }

      private static BlockHitResult faceHit(World world, Vec3d eyes, BlockPos pos, Direction side) {
         return lll(eyes, new Box(pos), side) ? new BlockHitResult(face(lIl(world, pos), side), side, pos, false) : null;
      }

      private static BlockHitResult closer(Vec3d eyes, BlockHitResult a, BlockHitResult b) {
         return b == null || a != null && !(b.getPos().squaredDistanceTo(eyes) < a.getPos().squaredDistanceTo(eyes)) ? a : b;
      }

      public static BlockHitResult ll(World world, Vec3d eyes, BlockPos pos) {
         BlockHitResult best = null;

         for (Direction side : Direction.values()) {
            BlockPos neighbor = pos.offset(side);
            if (world.getBlockState(neighbor).getCollisionShape(world, neighbor).isEmpty()) {
               best = closer(eyes, best, faceHit(world, eyes, pos, side));
            }
         }

         return best;
      }

      public static BlockHitResult lI(World world, Vec3d eyes, BlockPos pos) {
         return IlII(world, eyes, pos, null);
      }

      public static BlockHitResult IlII(World world, Vec3d eyes, BlockPos pos, BlockPos excluded) {
         BlockHitResult best = null;

         for (Direction side : Direction.values()) {
            BlockPos neighbor = pos.offset(side.getOpposite());
            BlockState state = world.getBlockState(neighbor);
            if (!neighbor.equals(excluded)
               && !state.isAir()
               && state.isSolidBlock(world, neighbor)
               && state.getFluidState().isEmpty()
               && !state.getCollisionShape(world, neighbor).isEmpty()) {
               best = closer(eyes, best, faceHit(world, eyes, neighbor, side));
            }
         }

         return best;
      }
   }

   public static final class AnchorRotation {
      private long lastStep;

      public void lIlI() {
         this.lastStep = 0L;
      }

      public void lIII() {
         this.lIlI();
      }

      public void IIIlIl() {
         this.lIlI();
      }

      public float IIIllI(MinecraftClient client, BlockHitResult hit, float strength) {
         float[] target = SafeAnchorSupport.RotationSupport.IIl(client, hit.getPos());
         if (target == null) {
            return Float.MAX_VALUE;
         } else {
            long now = System.nanoTime();
            float dt = this.lastStep == 0L ? 0.05F : MathHelper.clamp((float)(now - this.lastStep) / 1.0E9F, 0.0033333334F, 0.055555556F);
            this.lastStep = now;
            float yawDelta = MathHelper.wrapDegrees(target[0] - client.player.getYaw());
            float pitchDelta = target[1] - client.player.getPitch();
            float step = Math.min(1.0F, Math.max(1.0F, strength) / 100.0F * dt * 20.0F);
            double sensitivity = (Double)client.options.getMouseSensitivity().getValue() * 0.6 + 0.2;
            float increment = (float)(sensitivity * sensitivity * sensitivity * 8.0 * 0.15);
            float yawStep = (float)(Math.floor(Math.abs(yawDelta * step) / increment + 1.0E-4) * increment);
            float pitchStep = (float)(Math.floor(Math.abs(pitchDelta * step) / increment + 1.0E-4) * increment);
            client.player.setYaw(client.player.getYaw() + Math.copySign(yawStep, yawDelta));
            client.player.setPitch(MathHelper.clamp(client.player.getPitch() + Math.copySign(pitchStep, pitchDelta), -90.0F, 90.0F));
            return (float)Math.hypot(MathHelper.wrapDegrees(target[0] - client.player.getYaw()), target[1] - client.player.getPitch());
         }
      }
   }

   public record DelayRange(IntSupplier minimum, IntSupplier maximum) {
      public double lIl() {
         return this.minimum.getAsInt();
      }

      public double III() {
         return this.maximum.getAsInt();
      }

      public double II() {
         int low = Math.min(this.minimum.getAsInt(), this.maximum.getAsInt());
         int high = Math.max(this.minimum.getAsInt(), this.maximum.getAsInt());
         return low == high ? low : ThreadLocalRandom.current().nextDouble(low, high);
      }
   }

   public record Placement(BlockPos pos, Vec3d point, BlockHitResult hit) {
      public BlockHitResult I() {
         return this.hit;
      }

      public BlockPos II() {
         return this.pos;
      }

      public Vec3d l() {
         return this.point;
      }
   }

   public static enum RotationMode {
      Il,
      II;
   }

   public static final class RotationSupport {
      private static float yaw;
      private static float pitch;
      private static boolean active;

      public static boolean lIIlIl() {
         return false;
      }

      public static boolean lllIII() {
         return active;
      }

      public static float[] IIl(MinecraftClient client, Vec3d point) {
         if (client.player != null && point != null) {
            Vec3d delta = point.subtract(client.player.getEyePos());
            return new float[]{
               (float)(Math.toDegrees(Math.atan2(delta.z, delta.x)) - 90.0),
               MathHelper.clamp((float)(-Math.toDegrees(Math.atan2(delta.y, Math.hypot(delta.x, delta.z)))), -90.0F, 90.0F)
            };
         } else {
            return null;
         }
      }

      public static boolean lllIIl(MinecraftClient client, BlockHitResult target, boolean strictSide) {
         if (active && client.player != null && target != null) {
            Vec3d eyes = client.player.getEyePos();
            Vec3d end = eyes.add(Vec3d.fromPolar(pitch, yaw).multiply(Math.max(0.0, client.player.getBlockInteractionRange() - 0.05)));
            BlockHitResult hit = SafeAnchorSupport.IlllIl(client, client.player, eyes, end);
            return hit.getType() == Type.BLOCK && hit.getBlockPos().equals(target.getBlockPos()) && (!strictSide || hit.getSide() == target.getSide());
         } else {
            return false;
         }
      }

      public static boolean llIll(MinecraftClient client, Object owner, int priority, Vec3d point, BooleanSupplier action) {
         float[] angles = IIl(client, point);
         if (angles == null) {
            return false;
         } else {
            float oldYaw = client.player.getYaw();
            float oldPitch = client.player.getPitch();
            yaw = angles[0];
            pitch = angles[1];
            active = true;
            client.player.setYaw(yaw);
            client.player.setPitch(pitch);
            client.player.networkHandler.sendPacket(new LookAndOnGround(yaw, pitch, client.player.isOnGround(), client.player.horizontalCollision));

            boolean var8;
            try {
               var8 = action.getAsBoolean();
            } finally {
               active = false;
               client.player.setYaw(oldYaw);
               client.player.setPitch(oldPitch);
            }

            return var8;
         }
      }

      public static boolean lIIll(MinecraftClient client, Object owner, int priority, Vec3d point, BooleanSupplier action) {
         return llIll(client, owner, priority, point, action);
      }
   }

   public static class SafeAnchorRaycastContext extends RaycastContext {
      public SafeAnchorRaycastContext(Vec3d start, Vec3d end, ShapeType shape, FluidHandling fluid, ShapeContext context) {
         super(start, end, shape, fluid, context);
      }

      public VoxelShape getBlockShape(BlockState state, BlockView world, BlockPos pos) {
         return SafeAnchorSupport.IIllII(state) ? VoxelShapes.empty() : super.getBlockShape(state, world, pos);
      }
   }

   public static enum SafeBlock {
      I,
      Il,
      lI;
   }

   public static enum SafetyPlacement {
      Il,
      II;
   }

   public static enum TargetMode {
      I,
      II,
      Il;
   }

   public record TargetScore(PlayerEntity player, double distance, double angle, float health) {
      public float I() {
         return this.health;
      }

      public double II() {
         return this.angle;
      }

      public double Il() {
         return this.distance;
      }
   }
}
