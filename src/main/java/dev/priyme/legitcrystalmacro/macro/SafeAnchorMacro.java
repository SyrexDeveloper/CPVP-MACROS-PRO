package dev.priyme.legitcrystalmacro.macro;

import dev.priyme.legitcrystalmacro.LegitCrystalMacroClient;
import dev.priyme.legitcrystalmacro.config.MacroConfig;
import dev.priyme.legitcrystalmacro.macro.safeanchor.SafeAnchorSupport;
import dev.priyme.legitcrystalmacro.macro.safeanchor.SafeAnchorSupport.AnchorDamage;
import dev.priyme.legitcrystalmacro.macro.safeanchor.SafeAnchorSupport.AnchorGeometry;
import dev.priyme.legitcrystalmacro.macro.safeanchor.SafeAnchorSupport.AnchorRotation;
import dev.priyme.legitcrystalmacro.macro.safeanchor.SafeAnchorSupport.DelayRange;
import dev.priyme.legitcrystalmacro.macro.safeanchor.SafeAnchorSupport.Placement;
import dev.priyme.legitcrystalmacro.macro.safeanchor.SafeAnchorSupport.RotationMode;
import dev.priyme.legitcrystalmacro.macro.safeanchor.SafeAnchorSupport.RotationSupport;
import dev.priyme.legitcrystalmacro.macro.safeanchor.SafeAnchorSupport.SafeAnchorRaycastContext;
import dev.priyme.legitcrystalmacro.macro.safeanchor.SafeAnchorSupport.SafeBlock;
import dev.priyme.legitcrystalmacro.macro.safeanchor.SafeAnchorSupport.SafetyPlacement;
import dev.priyme.legitcrystalmacro.macro.safeanchor.SafeAnchorSupport.TargetMode;
import dev.priyme.legitcrystalmacro.macro.safeanchor.SafeAnchorSupport.TargetScore;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.BooleanSupplier;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.RespawnAnchorBlock;
import net.minecraft.block.ShapeContext;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult.Type;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Direction.Axis;
import net.minecraft.world.RaycastContext;
import net.minecraft.world.RaycastContext.FluidHandling;
import net.minecraft.world.RaycastContext.ShapeType;

public final class SafeAnchorMacro {
   private static final SafeAnchorMacro INSTANCE = new SafeAnchorMacro();
   private static MacroConfig config = new MacroConfig();
   private static Object lastWorld;
   private final DelayRange II = new DelayRange(() -> config.safeAnchorSwapDelayMin, () -> config.safeAnchorSwapDelayMax);
   private int Il;
   private static final int lI = 9;
   private static final int ll = 2;
   private int III;
   private boolean IIl;
   private long IlI;
   private static final long Ill = 75L;
   private long lII;
   private long lIl;
   private int llI;
   private int lll;
   private int IIlI;
   private final Map<BlockPos, Long> IllI;
   private final Set<Long> Illl;
   private static final long lIII = 3000L;
   private Placement lIIl;
   private BlockPos lIlI;
   private int llII;
   private int llIl;
   private int lllI;
   private static final int llll = 8;
   private int IIIlI;
   private boolean IIIll;
   private int IIlII;
   private boolean IIlIl;
   private boolean IIllI;
   private boolean IlIII;
   private int IlIIl;
   private long IlIll;
   private long IlllI;
   private BlockPos Illll;
   private long lIIII;
   private BlockPos lIIll;
   private boolean lIlIl;
   private Placement lIllI;
   private boolean lIlll;
   private final AnchorRotation llIII;
   private static final Direction[] I = new Direction[]{Direction.NORTH, Direction.SOUTH, Direction.WEST, Direction.EAST};
   private final DelayRange llIlI = new DelayRange(() -> config.safeAnchorPlaceDelayMin, () -> config.safeAnchorPlaceDelayMax);
   private BlockPos lllII;
   private long lllIl;
   private int llllI;
   private static final int lllll = 3;
   private int IIIIIl;
   private int IIIIlI;
   private long IIIIll;
   private BlockPos IIIllI;

   public static void tick(MinecraftClient client, MacroConfig cfg) {
      if (cfg != null) {
         config = cfg;
      }

      if (cfg != null && cfg.safeAnchorEnabled && client.player != null && client.world != null) {
         if (lastWorld != client.world) {
            INSTANCE.IlI();
            lastWorld = client.world;
         }

         SafeAnchorSupport.tick(client);
         INSTANCE.IIIII();
         INSTANCE.IlllI(client);
      } else {
         INSTANCE.IlI();
         lastWorld = null;
      }
   }

   public static void reset() {
      INSTANCE.IlI();
      lastWorld = null;
   }

   public static void beforeInteract(MinecraftClient client, Hand hand, BlockHitResult hit) {
      MacroConfig cfg = LegitCrystalMacroClient.config;
      if (cfg != null) {
         config = cfg;
         INSTANCE.llIll(client, hand, hit);
      }
   }

   public static void afterInteract(MinecraftClient client, Hand hand, BlockHitResult hit, ActionResult result) {
      INSTANCE.IIIIlI(client, hand, hit, result);
   }

   private void I(MinecraftClient var1, boolean var2) {
      if (var1 != null && var1.player != null && var1.world != null) {
         double var4 = Math.max(0.0, var1.player.getBlockInteractionRange() + 0.75);
         double var6 = var4 * var4;
         int var8 = (int)Math.ceil(var4);
         Vec3d var9 = var1.player.getEyePos();
         BlockPos var10 = var1.player.getBlockPos();
         HashSet<Long> var11 = new HashSet<>();
         BlockPos var12 = null;
         double var13 = Double.POSITIVE_INFINITY;

         for (int var15 = -var8; var15 <= var8; var15++) {
            for (int var16 = -var8; var16 <= var8; var16++) {
               for (int var17 = -var8; var17 <= var8; var17++) {
                  BlockPos var18 = var10.add(var15, var16, var17);
                  double var19 = var9.squaredDistanceTo(Vec3d.ofCenter(var18));
                  if (!(var19 > var6) && var1.world.getBlockState(var18).isOf(Blocks.RESPAWN_ANCHOR)) {
                     long var21 = var18.asLong();
                     var11.add(var21);
                     if (var2 && !this.Illl.contains(var21) && var19 < var13) {
                        var12 = var18.toImmutable();
                        var13 = var19;
                     }
                  }
               }
            }
         }

         int var25;
         label99: {
            this.Illl.clear();
            this.Illl.addAll(var11);
            label64:
            if (var12 != null) {
               if (this.lllII != null && this.lllII.equals(var12) && this.IlIIl != Integer.MIN_VALUE) {
                  var25 = var1.player.age - this.IlIIl;
                  if (var25 < 8) {
                     break label64;
                  }
               }

               var25 = 1;
               break label99;
            }

            var25 = 0;
         }

         boolean var24 = var25 != 0;
         if (var2 && var24 && this.lIIll == null && this.lIIl == null && this.lIIl()) {
            this.lllII = var12.toImmutable();
            this.IlIIl = var1.player.age;
            this.IIllIl();
            this.l(var1, var12);
         }
      } else {
         this.Illl.clear();
      }
   }

   private int II() {
      return Math.max(1, this.IlIlll(this.II) + this.IlIlll(this.llIlI) + 1);
   }

   private boolean lI(MinecraftClient var1, PlayerEntity var2) {
      return var2 != null && var1.player != null && var2 != var1.player && var2.isAlive() && !var2.isSpectator();
   }

   private boolean ll(MinecraftClient var1, Placement var2) {
      this.IIIll = true;

      boolean var3;
      try {
         var3 = SafeAnchorSupport.lIlIl(var1, var2.I());
      } finally {
         this.IIIll = false;
      }

      return var3;
   }

   private int III(MinecraftClient var1) {
      if (var1 != null && var1.player != null) {
         for (int var3 = 0; var3 < 9; var3++) {
            ItemStack var4 = var1.player.getInventory().getStack(var3);
            if (var4 != null && (var4.isOf(Items.OBSIDIAN) || var4.isOf(Items.CRYING_OBSIDIAN))) {
               return var3;
            }
         }

         return -1;
      } else {
         return -1;
      }
   }

   private void IIl(BlockPos var1) {
      if (var1 != null) {
         this.IllI.put(var1.toImmutable(), System.currentTimeMillis() + this.IIlll());
      }
   }

   private void Ill(MinecraftClient var1) {
      if (RotationMode.values()[config.safeAnchorRotationMode] == RotationMode.Il
         && this.lIIll != null
         && RotationSupport.lllIII()
         && System.currentTimeMillis() >= this.IlI) {
         this.lIlll = true;

         try {
            this.lIlIl(var1);
         } finally {
            this.lIlll = false;
         }
      }
   }

   private static double lII(Vec3d var0, Vec3d var1, Vec3d var2) {
      double var3 = var2.x - var1.x;
      double var5 = var2.z - var1.z;
      double var7 = var3 * var3 + var5 * var5;
      if (var7 <= 1.0E-9) {
         double var20 = var0.x - var1.x;
         double var21 = var0.z - var1.z;
         return var20 * var20 + var21 * var21;
      } else {
         double var9 = ((var0.x - var1.x) * var3 + (var0.z - var1.z) * var5) / var7;
         var9 = Math.max(0.0, Math.min(1.0, var9));
         double var11 = var1.x + var3 * var9;
         double var13 = var1.z + var5 * var9;
         double var15 = var0.x - var11;
         double var17 = var0.z - var13;
         return var15 * var15 + var17 * var17;
      }
   }

   private PlayerEntity lIl(MinecraftClient var1) {
      if (var1 != null && var1.world != null && var1.player != null) {
         PlayerEntity var3 = null;
         TargetScore var4 = null;
         double var5 = Double.valueOf(config.safeAnchorTargetRange) * Double.valueOf(config.safeAnchorTargetRange);

         for (PlayerEntity var8 : var1.world.getPlayers()) {
            if (this.lI(var1, var8)) {
               double var9 = var1.player.squaredDistanceTo(var8);
               if (!(var9 > var5)) {
                  double var11 = Math.sqrt(var9);
                  double var13 = this.IlIllII(var1, var8);
                  if (!(Double.valueOf(config.safeAnchorFov) < 180.0) || !(var13 > Double.valueOf(config.safeAnchorFov) * 0.5)) {
                     float var15 = var8.getHealth() + var8.getAbsorptionAmount();
                     TargetScore var16 = new TargetScore(var8, var11, var13, var15);
                     if (this.lllll(var16, var4)) {
                        var4 = var16;
                        var3 = var8;
                     }
                  }
               }
            }
         }

         return var3;
      } else {
         return null;
      }
   }

   private int llI(PlayerEntity var1, int var2) {
      int var3 = this.IlIIll(var1, Items.TOTEM_OF_UNDYING);
      if (var3 != -1) {
         return var3;
      } else {
         ItemStack var4 = var1.getInventory().getStack(var2);
         if (this.IlIllll(var4)) {
            return var2;
         } else {
            int var5 = this.IlIIll(var1, Items.RESPAWN_ANCHOR);
            if (var5 != -1) {
               return var5;
            } else {
               int var6 = this.lIIlIl(var1);
               return var6 != -1 ? var6 : var2;
            }
         }
      }
   }

   private void lll(MinecraftClient var1) {
      if (Boolean.valueOf(config.safeAnchorAutoTarget)) {
         if (!this.lIIl()) {
            this.lIl = System.currentTimeMillis();
         } else {
            PlayerEntity var2 = this.lIl(var1);
            if (var2 == null) {
               this.lIl = System.currentTimeMillis() - this.IIlll();
            } else {
               BlockPos var3 = this.IIll(var1, var2);
               if (var3 != null) {
                  this.l(var1, var3);
                  this.lIl = System.currentTimeMillis();
                  if (this.IlI <= System.currentTimeMillis()) {
                     this.IlIIII();
                  }
               } else {
                  int var4 = this.IllIlI(var1);
                  int var5 = this.lIIlI(var1);
                  if (var4 >= 0 && var5 >= 0) {
                     Placement var6 = this.IIlllI(var1, var2);
                     if (var6 == null) {
                        this.lIl = System.currentTimeMillis() - this.IIlll();
                     } else {
                        if (this.IIIlI < 0 && var1 != null && var1.player != null) {
                           this.IIIlI = SafeAnchorSupport.llllII(var1.player.getInventory());
                        }

                        this.lIl = System.currentTimeMillis();
                        if (RotationMode.values()[config.safeAnchorRotationMode] == RotationMode.II) {
                           this.lIIl = var6;
                           this.IIIIIl = var4;
                           this.III = 0;
                           this.lllIl = System.currentTimeMillis();
                           this.IIllI = true;
                           this.lII = System.currentTimeMillis();
                           this.lIlII(var1);
                        } else {
                           this.III = 0;
                           this.lllIl = System.currentTimeMillis();
                           this.IlIlIII(var1, var6, var4);
                        }
                     }
                  } else {
                     this.lIl = System.currentTimeMillis() - this.IIlll();
                  }
               }
            }
         }
      }
   }

   public void IIII(MinecraftClient var1) {
      this.IIllIl();
      this.IlIlllI();
   }

   private Placement IIIl(MinecraftClient var1, BlockPos var2) {
      BlockHitResult var3 = this.IlllII(var1);
      if (var3 == null || var2 == null || var1 == null || var1.world == null) {
         return null;
      } else if (!var1.world.getBlockState(var2).isReplaceable() && !this.IIlIlI(var1.world.getBlockState(var2))) {
         return null;
      } else {
         BlockPos var4 = var3.getBlockPos();
         if (this.Illll != null && var4.equals(this.Illll)) {
            return null;
         } else {
            BlockPos var5 = var4.offset(var3.getSide());
            if (var2.equals(var5) && this.IllIll(var1, var4)) {
               Placement var6 = new Placement(var2.toImmutable(), var3.getPos(), var3);
               return this.IlllIl(var1, var6) ? var6 : null;
            } else {
               return null;
            }
         }
      }
   }

   private void IIlI(MinecraftClient var1) {
      if (this.llI > 0) {
         this.IlI = this.IlIlIIl(var1, this.llllI);
      } else if (this.lllII(var1, this.Illll)) {
         if (Boolean.valueOf(config.safeAnchorAutoExplode)) {
            this.lIll(var1);
         } else {
            this.IIIllI(var1);
         }
      } else {
         Placement var2 = this.lIlll(var1, this.Illll);
         if (var2 != null) {
            int var3 = this.IIllII(var1);
            if (var3 >= 0) {
               this.llllI = var3;
            }

            this.lIlIl = false;
            this.lIllI = var2;
            this.lIIll = var2.II().toImmutable();
            this.IlI = this.IlIlIIl(var1, this.llllI);
         } else if (Boolean.valueOf(config.safeAnchorAutoExplode)) {
            this.lIll(var1);
         } else {
            this.IIIllI(var1);
         }
      }
   }

   private BlockPos IIll(MinecraftClient var1, PlayerEntity var2) {
      if (var1 != null && var1.world != null && var1.player != null && var2 != null) {
         double var4 = Math.max(0.0, var1.player.getBlockInteractionRange() - 0.1);
         double var6 = var4 * var4;
         Vec3d var8 = var1.player.getEyePos();
         BlockPos var9 = var1.player.getBlockPos();
         BlockPos var10 = null;
         float var11 = 0.0F;
         int var12 = (int)Math.ceil(var4);

         for (int var13 = -var12; var13 <= var12; var13++) {
            for (int var14 = -var12; var14 <= var12; var14++) {
               for (int var15 = -var12; var15 <= var12; var15++) {
                  BlockPos var16 = var9.add(var13, var14, var15);
                  if (!(var8.squaredDistanceTo(Vec3d.ofCenter(var16)) > var6)) {
                     BlockState var17 = var1.world.getBlockState(var16);
                     if (var17.isOf(Blocks.RESPAWN_ANCHOR)) {
                        Long var18 = this.IllI.get(var16);
                        if ((var18 == null || var18 <= System.currentTimeMillis()) && !this.IlII(var1, var16)) {
                           int var19 = (Integer)var17.get(RespawnAnchorBlock.CHARGES);
                           if (var19 != 0 || Boolean.valueOf(config.safeAnchorChargeGlowstone) && this.lIIlI(var1) >= 0) {
                              Placement var20 = this.llll(var16);
                              if (var20 != null && this.IlllIl(var1, var20)) {
                                 float var21 = AnchorDamage.lI(var2, var16, null, SafeAnchorSupport.position(var2));
                                 if (!(var21 <= var11)
                                    && (
                                       !Boolean.valueOf(config.safeAnchorEnforceMinDamage)
                                          || !(var21 < Double.valueOf(config.safeAnchorMinDamage).floatValue())
                                    )) {
                                    boolean var22 = this.lllII(var1, var16);
                                    Placement var23 = var22 ? null : this.lIlll(var1, var16);
                                    float var24 = !var22 && var23 == null
                                       ? AnchorDamage.lI(var1.player, var16, null, this.llIl(var1))
                                       : AnchorDamage.lI(var1.player, var16, var23 != null ? var23.II() : null, this.llIl(var1));
                                    if ((var23 != null || var22 || Boolean.valueOf(config.safeAnchorExplodeUnsafe))
                                       && (!(var24 >= var21) || Boolean.valueOf(config.safeAnchorExplodeUnsafe))
                                       && var21 > var11) {
                                       var11 = var21;
                                       var10 = var16;
                                    }
                                 }
                              }
                           }
                        }
                     }
                  }
               }
            }
         }

         return var10;
      } else {
         return null;
      }
   }

   private boolean IlII(MinecraftClient var1, BlockPos var2) {
      return this.IIIllI != null
         && var2 != null
         && this.IIIllI.equals(var2)
         && var1 != null
         && var1.player != null
         && var1.world != null
         && var1.player.age <= this.IIlI
         && var1.world.getBlockState(var2).isOf(Blocks.RESPAWN_ANCHOR);
   }

   private void IlIl(BlockPos var1) {
      if (var1 != null && var1.equals(this.IIIllI)) {
         this.IIIllI = null;
         this.IIlI = Integer.MIN_VALUE;
      }
   }

   private boolean IllI(MinecraftClient var1, BlockPos var2) {
      if (var1 != null && var1.world != null && var2 != null) {
         BlockState var3 = var1.world.getBlockState(var2);
         return var3.isOf(Blocks.RESPAWN_ANCHOR) ? (Integer)var3.get(RespawnAnchorBlock.CHARGES) < 4 : this.IIlIlI(var3);
      } else {
         return false;
      }
   }

   private boolean Illl(float var1, double var2, double var4, float var6, double var7, double var9) {
      if (var2 < var7 - 0.05) {
         return true;
      } else if (var2 > var7 + 0.05) {
         return false;
      } else if (var1 < var6 - 0.001F) {
         return true;
      } else {
         return Math.abs(var1 - var6) > 0.001F ? false : var4 < var9;
      }
   }

   private boolean lIIl() {
      double var1 = Math.max(0.0, Math.min(100.0, Double.valueOf(config.safeAnchorChance)));
      return var1 >= 100.0 || var1 > 0.0 && ThreadLocalRandom.current().nextDouble(100.0) < var1;
   }

   private Placement lIlI(MinecraftClient var1, BlockPos var2, BlockPos var3, Direction var4) {
      if (var1 != null && var1.world != null && var1.player != null) {
         Vec3d var6 = var1.player.getEyePos();
         Vec3d var7 = Vec3d.ofCenter(var3);
         Vec3d var8 = var7.add(Vec3d.of(var4.getVector()).multiply(0.5));
         Vec3d var9 = var8.subtract(Vec3d.of(var4.getVector()).multiply(0.01));
         BlockHitResult var10 = this.IlIIlIl(var1, var6, var9);
         if (var10 != null
            && var10.getType() == Type.BLOCK
            && var10.getBlockPos().equals(var3)
            && var10.getSide() == var4
            && var2.equals(var3.offset(var10.getSide()))) {
            return new Placement(var2.toImmutable(), var10.getPos(), var10);
         } else {
            Vec3d var11 = Vec3d.of(var4.getVector());
            Vec3d var12;
            Vec3d var13;
            if (var4.getAxis() == Axis.Y) {
               var12 = new Vec3d(1.0, 0.0, 0.0);
               var13 = new Vec3d(0.0, 0.0, 1.0);
            } else if (var4.getAxis() == Axis.X) {
               var12 = new Vec3d(0.0, 1.0, 0.0);
               var13 = new Vec3d(0.0, 0.0, 1.0);
            } else {
               var12 = new Vec3d(1.0, 0.0, 0.0);
               var13 = new Vec3d(0.0, 1.0, 0.0);
            }

            Vec3d var14 = var6.subtract(var8);
            double var15 = MathHelper.clamp(var14.dotProduct(var12) * 0.5, -0.3, 0.3);
            double var17 = MathHelper.clamp(var14.dotProduct(var13) * 0.5, -0.3, 0.3);
            Vec3d[] var19 = new Vec3d[]{
               var12.multiply(var15).add(var13.multiply(var17)),
               var12.multiply(0.25).add(var13.multiply(0.25)),
               var12.multiply(-0.25).add(var13.multiply(0.25)),
               var12.multiply(0.25).add(var13.multiply(-0.25)),
               var12.multiply(-0.25).add(var13.multiply(-0.25))
            };

            for (Vec3d var23 : var19) {
               Vec3d var24 = var8.add(var23);
               Vec3d var25 = var24.subtract(var11.multiply(0.01));
               BlockHitResult var26 = this.IlIIlIl(var1, var6, var25);
               if (var26 != null
                  && var26.getType() == Type.BLOCK
                  && var26.getBlockPos().equals(var3)
                  && var26.getSide() == var4
                  && var2.equals(var3.offset(var26.getSide()))) {
                  return new Placement(var2.toImmutable(), var26.getPos(), var26);
               }
            }

            if (!Boolean.valueOf(config.safeAnchorRaycast)) {
               Vec3d var27 = Vec3d.of(var4.getVector());
               if (var6.subtract(var8).dotProduct(var27) <= 0.05) {
                  return null;
               } else if (!AnchorGeometry.lll(var6, AnchorGeometry.lIl(var1.world, var3), var4)) {
                  return null;
               } else {
                  double var28 = var1.player.getBlockInteractionRange();
                  if (var6.squaredDistanceTo(var8) > var28 * var28) {
                     return null;
                  } else {
                     BlockHitResult var29 = this.IlIIlIl(var1, var6, var8);
                     if (var29 != null && var29.getType() == Type.BLOCK && !var29.getBlockPos().equals(var3) && !var29.getBlockPos().equals(var2)) {
                        return null;
                     } else {
                        BlockHitResult var31 = new BlockHitResult(var8, var4, var3, false);
                        return new Placement(var2.toImmutable(), var8, var31);
                     }
                  }
               }
            } else {
               return null;
            }
         }
      } else {
         return null;
      }
   }

   private void lIll(MinecraftClient var1) {
      if (this.Illll == null || var1 == null || var1.player == null) {
         this.IIIllI(var1);
      } else if (!this.IllII(var1, this.Illll)) {
         if (this.Illll != null) {
            this.IIl(this.Illll);
         }

         this.IIIllI(var1);
      } else {
         int var2 = this.IIIlI >= 0 ? this.IIIlI : SafeAnchorSupport.llllII(var1.player.getInventory());
         int var3 = this.llI(var1.player, var2);
         if (var3 < 0) {
            if (this.Illll != null) {
               this.IIl(this.Illll);
            }

            this.IIIllI(var1);
         } else {
            this.IIllI = false;
            this.lIIll = this.Illll.toImmutable();
            this.lIllI = this.llll(this.Illll);
            this.lIlIl = false;
            this.IIlIl = true;
            this.IIlII = 0;
            this.llllI = var3;
            long var4 = System.currentTimeMillis();
            boolean var10000;
            if (var1.player != null && var3 >= 0 && var3 < 9 && SafeAnchorSupport.lIlIIIl(var1) != var3) {
               var10000 = true;
            } else {
               var10000 = false;
            }

            long var7 = var10000 ? this.IlIIlll(this.II) : 0L;
            this.IlI = var4 + var7;
            this.IIIIlI = var1.player.age + this.II() + 2;
         }
      }
   }

   private boolean llII(MinecraftClient var1) {
      return var1 != null
         && var1.player != null
         && var1.world != null
         && var1.interactionManager != null
         && var1.currentScreen == null
         && var1.player.isAlive()
         && !var1.player.isUsingItem();
   }

   private Vec3d llIl(MinecraftClient var1) {
      if (var1 != null && var1.player != null) {
         int var2 = this.llllI >= 0 ? this.llllI : this.lIIlI(var1);
         boolean var3 = var2 >= 0 && SafeAnchorSupport.lIlIIIl(var1) != var2;
         double var4 = var3 ? (this.II.lIl() + this.II.III()) / 2.0 : 0.0;
         double var6 = (this.llIlI.lIl() + this.llIlI.III()) / 2.0;
         double var8 = var4 + var6;
         double var10 = Math.max(1.0, Math.min(10.0, var8 / 50.0));
         Vec3d var12 = var1.player.getVelocity();
         Vec3d var13 = new Vec3d(var1.player.getX(), var1.player.getY(), var1.player.getZ());
         return var13.add(var12.multiply(var10));
      } else {
         return Vec3d.ZERO;
      }
   }

   private boolean lllI(MinecraftClient var1, Placement var2, boolean var3, boolean var4, int var5) {
      if (this.IIIIII(var1)) {
         this.IlI = System.currentTimeMillis();
         return false;
      } else {
         boolean var7 = RotationSupport.lllIIl(var1, var2.I(), false);
         if (!var7 && var1.player != null) {
            Vec3d var8 = var1.player.getEyePos();
            double var9 = Math.max(0.0, var1.player.getBlockInteractionRange() - 0.05);
            Vec3d var11 = var1.player.getRotationVec(1.0F);
            if (this.Illll != null && (var3 || var4) && AnchorGeometry.lII(var1.world, this.Illll, var8, var11, var9)) {
               var7 = true;
            } else if (RotationMode.values()[config.safeAnchorRotationMode] == RotationMode.II) {
               Vec3d var12 = var8.add(var11.multiply(var9));
               BlockHitResult var13 = SafeAnchorSupport.IlllIl(var1, var1.player, var8, var12);
               if (var13 != null && var13.getType() == Type.BLOCK && var13.getBlockPos().equals(var2.I().getBlockPos())) {
                  var7 = true;
               }
            } else if (!Boolean.valueOf(config.safeAnchorRaycast)) {
               var7 = this.IlllIl(var1, var2);
            }
         }

         if (!var7) {
            this.IIllI = false;
            if (++this.IIlII > 3) {
               if (this.Illll != null) {
                  this.IIl(this.Illll);
               }

               this.IIIllI(var1);
               return false;
            } else {
               this.IlI = System.currentTimeMillis() + Math.max(5L, this.IIlll());
               return false;
            }
         } else if (!var3 && !var4 && !this.llIII(var1, var2)) {
            this.IIllI = false;
            if (++this.IIlII > 3) {
               if (this.Illll != null) {
                  this.IIl(this.Illll);
               }

               this.IIIllI(var1);
               return false;
            } else {
               this.IlI = System.currentTimeMillis() + Math.max(5L, this.IIlll());
               return false;
            }
         } else {
            boolean var14 = SafeAnchorSupport.llIll(var1, this, var5, false, () -> this.ll(var1, var2));
            if (!var14) {
               this.IIllI = false;
               if (++this.IIlII > 3) {
                  if (this.Illll != null) {
                     this.IIl(this.Illll);
                  }

                  this.IIIllI(var1);
                  return false;
               } else {
                  this.IlI = System.currentTimeMillis() + Math.max(5L, this.IIlll());
                  return false;
               }
            } else {
               this.lllI = var1.player.age;
               this.lIIII = SafeAnchorSupport.lllllI();
               this.IlllI = SafeAnchorSupport.IIlIIlI();
               if (var3) {
                  this.IIl = true;
                  this.IIllI = false;
                  this.llI = Math.max(0, this.llI - 1);
                  if (this.llI > 0) {
                     this.IlI = System.currentTimeMillis();
                  } else {
                     this.IIlI(var1);
                  }

                  this.Ill(var1);
                  return true;
               } else if (!var4) {
                  this.IIllI = false;
                  if (Boolean.valueOf(config.safeAnchorAutoExplode)) {
                     this.lIll(var1);
                     this.Ill(var1);
                     return true;
                  } else {
                     this.IIIllI(var1);
                     return true;
                  }
               } else {
                  this.IIllI = false;
                  this.IIIllI = this.Illll == null ? null : this.Illll.toImmutable();
                  this.IIlI = var1.player.age + 8;
                  this.IIIllI(var1);
                  return var14;
               }
            }
         }
      }
   }

   private Placement llll(BlockPos var1) {
      if (var1 == null) {
         return null;
      } else {
         MinecraftClient var3 = MinecraftClient.getInstance();
         if (var3 != null && var3.world != null && var3.player != null) {
            BlockHitResult var4 = this.IlllII(var3);
            if (var4 != null && var4.getBlockPos().equals(var1)) {
               Placement var5 = new Placement(var1.toImmutable(), var4.getPos(), var4);
               if (this.IlllIl(var3, var5)) {
                  return var5;
               }
            }

            Vec3d var24 = var3.player.getEyePos();
            Vec3d var6 = Vec3d.ofCenter(var1);
            BlockHitResult var7 = AnchorGeometry.ll(var3.world, var24, var1);
            if (var7 != null) {
               Vec3d var8 = Vec3d.of(var7.getSide().getVector());
               Vec3d var9 = var7.getPos().subtract(var8.multiply(0.01));
               BlockHitResult var10 = this.IlIIlIl(var3, var24, var9);
               if (var10 != null && var10.getType() == Type.BLOCK && var10.getBlockPos().equals(var1)) {
                  Placement var33 = new Placement(var1.toImmutable(), var7.getPos(), var7);
                  if (this.IlllIl(var3, var33)) {
                     return var33;
                  }
               } else if (!Boolean.valueOf(config.safeAnchorRaycast)
                  || AnchorGeometry.lII(var3.world, var1, var24, var7.getPos().subtract(var24).normalize(), var3.player.getBlockInteractionRange())) {
                  Placement var11 = new Placement(var1.toImmutable(), var7.getPos(), var7);
                  if (this.IlllIl(var3, var11)) {
                     return var11;
                  }
               }
            }

            for (Direction var34 : Direction.values()) {
               Vec3d var12 = var6.add(Vec3d.of(var34.getVector()).multiply(0.5));
               Vec3d var13 = var12.subtract(Vec3d.of(var34.getVector()).multiply(0.01));
               BlockHitResult var14 = this.IlIIlIl(var3, var24, var13);
               if (var14 != null && var14.getType() == Type.BLOCK && var14.getBlockPos().equals(var1)) {
                  Placement var15 = new Placement(var1.toImmutable(), var14.getPos(), var14);
                  if (this.IlllIl(var3, var15)) {
                     return var15;
                  }
               }
            }

            for (Direction var35 : Direction.values()) {
               Vec3d var37 = Vec3d.of(var35.getVector());
               Vec3d var40 = var6.add(var37.multiply(0.5));
               if (!(var24.subtract(var40).dotProduct(var37) <= 0.0)) {
                  Vec3d var43 = var35.getAxis() == Axis.Y
                     ? new Vec3d(1.0, 0.0, 0.0)
                     : (var35.getAxis() == Axis.X ? new Vec3d(0.0, 1.0, 0.0) : new Vec3d(1.0, 0.0, 0.0));
                  Vec3d var45 = var35.getAxis() == Axis.Y
                     ? new Vec3d(0.0, 0.0, 1.0)
                     : (var35.getAxis() == Axis.X ? new Vec3d(0.0, 1.0, 0.0) : new Vec3d(0.0, 1.0, 0.0));
                  Vec3d[] var16 = new Vec3d[]{
                     var43.multiply(0.25).add(var45.multiply(0.25)),
                     var43.multiply(-0.25).add(var45.multiply(0.25)),
                     var43.multiply(0.25).add(var45.multiply(-0.25)),
                     var43.multiply(-0.25).add(var45.multiply(-0.25))
                  };

                  for (Vec3d var20 : var16) {
                     Vec3d var21 = var40.add(var20).subtract(var37.multiply(0.01));
                     BlockHitResult var22 = this.IlIIlIl(var3, var24, var21);
                     if (var22 != null && var22.getType() == Type.BLOCK && var22.getBlockPos().equals(var1)) {
                        Placement var23 = new Placement(var1.toImmutable(), var22.getPos(), var22);
                        if (this.IlllIl(var3, var23)) {
                           return var23;
                        }
                     }
                  }
               }
            }

            if (!Boolean.valueOf(config.safeAnchorRaycast)) {
               Direction var27 = null;
               double var30 = Double.POSITIVE_INFINITY;
               Vec3d var36 = var6;

               for (Direction var46 : Direction.values()) {
                  BlockPos var47 = var1.offset(var46);
                  BlockState var48 = var3.world.getBlockState(var47);
                  if (!SafeAnchorSupport.isOpaqueFullCube(var3, var48, var47)) {
                     Vec3d var49 = var6.add(Vec3d.of(var46.getVector()).multiply(0.5));
                     Vec3d var50 = Vec3d.of(var46.getVector());
                     if (!(var24.subtract(var49).dotProduct(var50) <= 0.05) && AnchorGeometry.lll(var24, AnchorGeometry.lIl(var3.world, var1), var46)) {
                        double var51 = var24.squaredDistanceTo(var49);
                        if (var51 < var30) {
                           var30 = var51;
                           var27 = var46;
                           var36 = var49;
                        }
                     }
                  }
               }

               if (var27 != null) {
                  BlockHitResult var39 = new BlockHitResult(var36, var27, var1, false);
                  Placement var42 = new Placement(var1.toImmutable(), var36, var39);
                  if (this.IlllIl(var3, var42)) {
                     return var42;
                  }
               }
            }

            return null;
         } else {
            return null;
         }
      }
   }

   private void IIIIl(MinecraftClient var1, BlockPos var2) {
      this.IIl(var2);
      this.lIIl = null;
      this.IIIIIl = -1;
      this.III = 0;
      this.lllIl = 0L;
      this.IIllI = false;
      this.llIII.lIlI();
      this.lIl = System.currentTimeMillis() - 75L;
      if (this.IIIlI >= 0 && this.IIIlI < 9 && var1 != null && var1.player != null) {
         if (Boolean.valueOf(config.safeAnchorSwitchBack)) {
            SafeAnchorSupport.IIllll(var1, this, this.IIIlI);
         } else {
            SafeAnchorSupport.lIIllIl(var1, this, 0);
         }

         this.IIIlI = -1;
      } else {
         if (SafeAnchorSupport.IlIII(this)) {
            SafeAnchorSupport.IIIllI(this);
         }
      }
   }

   private Placement IIIlI(MinecraftClient var1, BlockPos var2) {
      BlockHitResult var3 = this.IlllII(var1);
      if (var3 != null && var2 != null && var2.equals(var3.getBlockPos())) {
         Placement var4 = new Placement(var2.toImmutable(), var3.getPos(), var3);
         return this.IlllIl(var1, var4) ? var4 : null;
      } else {
         return null;
      }
   }

   private Placement IIIll(MinecraftClient var1, BlockPos var2) {
      Direction var4 = this.IllIIIl(var1, var2);
      Direction[] var5 = new Direction[]{var4, var4.rotateYClockwise(), var4.rotateYCounterclockwise(), var4.getOpposite()};
      Vec3d var6 = this.llIl(var1);

      for (Direction var10 : var5) {
         BlockPos var11 = var2.offset(var10);
         if (this.IIlIlI(var1.world.getBlockState(var11)) && !this.IIIIll(var1, var11, var6) && !this.IlIllI(var1, var11)) {
            Placement var12 = this.lIIIll(var1, var11);
            if (var12 == null) {
               var12 = this.lIlI(var1, var11, var2, var10);
            }

            if (var12 != null) {
               return var12;
            }
         }
      }

      return null;
   }

   private boolean IIlII(MinecraftClient var1) {
      return var1 != null
         && var1.player != null
         && var1.world != null
         && this.Illll != null
         && (this.lIlIl || this.IIlIl)
         && !var1.world.getBlockState(this.Illll).isOf(Blocks.RESPAWN_ANCHOR)
         && var1.player.age <= this.llIl;
   }

   private void IIlIl(MinecraftClient var1) {
      if (this.IIIllI != null && !this.IlII(var1, this.IIIllI)) {
         this.IIIllI = null;
         this.IIlI = Integer.MIN_VALUE;
      }
   }

   private boolean IIllI() {
      return RotationMode.values()[config.safeAnchorRotationMode] == RotationMode.II;
   }

   private long IIlll() {
      return 0L;
   }

   private boolean IllII(MinecraftClient var1, BlockPos var2) {
      if (!Boolean.valueOf(config.safeAnchorEnforceMinDamage)) {
         return true;
      } else {
         double var3 = Double.valueOf(config.safeAnchorMinDamage);
         if (var3 <= 0.0) {
            return true;
         } else if (var1 != null && var1.world != null && var2 != null) {
            PlayerEntity var5 = this.lIl(var1);
            if (var5 == null) {
               return false;
            } else {
               float var6 = AnchorDamage.lI(var5, var2, null, SafeAnchorSupport.position(var5));
               return var6 >= (float)var3;
            }
         } else {
            return false;
         }
      }
   }

   private Placement lIIIl(MinecraftClient var1, BlockPos var2, List<BlockPos> var3, Vec3d var4, double var5, double var7, double var9) {
      Placement var12 = null;
      float var13 = Float.POSITIVE_INFINITY;
      double var14 = Double.POSITIVE_INFINITY;
      double var16 = Double.POSITIVE_INFINITY;
      Vec3d var18 = Vec3d.ofCenter(var2);
      float var19 = Float.NaN;

      for (BlockPos var21 : var3) {
         if (var9 > 1.0E-4) {
            Vec3d var22 = Vec3d.ofCenter(var21).subtract(var18);
            double var23 = Math.sqrt(var22.x * var22.x + var22.z * var22.z);
            double var25 = (var22.x * var5 + var22.z * var7) / Math.max(0.1, var23);
            if (var25 < 0.55) {
               continue;
            }
         }

         Placement var28 = this.lIIIll(var1, var21);
         if (var28 != null && this.IlllIl(var1, var28) && !this.IIIIll(var1, var21, var4)) {
            if (Float.isNaN(var19)) {
               var19 = AnchorDamage.lI(var1.player, var2, null, var4);
            }

            float var29 = AnchorDamage.lI(var1.player, var2, var21, var4);
            if (!(var29 > var19 - 0.05F) || !(var19 > 1.0F)) {
               double var24 = lII(Vec3d.ofCenter(var21), var18, var4);
               double var26 = var1.player.getEyePos().squaredDistanceTo(var28.l());
               if (this.Illl(var29, var24, var26, var13, var14, var16)) {
                  var12 = var28;
                  var13 = var29;
                  var14 = var24;
                  var16 = var26;
               }
            }
         }
      }

      return var12;
   }

   private int lIIlI(MinecraftClient var1) {
      for (int var3 = 0; var3 < 9; var3++) {
         ItemStack var4 = var1.player.getInventory().getStack(var3);
         if (var4 != null && var4.isOf(Items.GLOWSTONE)) {
            return var3;
         }
      }

      return -1;
   }

   private boolean lIIll(MinecraftClient var1, int var2) {
      return var1 != null && var1.player != null && var2 >= 0 && var2 < 9 && var1.player.getInventory().getStack(var2).isOf(Items.GLOWSTONE);
   }

   private void lIlII(MinecraftClient var1) {
      if (config != null && config.safeAnchorEnabled && this.llII(var1) && this.lIIl != null && this.llIII(var1, this.lIIl)) {
         if (RotationMode.values()[config.safeAnchorRotationMode] == RotationMode.II) {
            Placement var2 = this.IIIlll(var1, this.lIIl.II());
            if (var2 != null) {
               this.IIllI = false;
               this.llIII.lIlI();
               this.IlIlIII(var1, var2, this.IIIIIl);
            } else {
               float var3 = this.llIII.IIIllI(var1, this.lIIl.I(), Double.valueOf(config.safeAnchorStrength).floatValue());
               if (var3 <= 3.5F) {
                  this.IIllI = false;
                  this.llIII.lIlI();
                  this.IlIlIII(var1, this.lIIl, this.IIIIIl);
               } else if (System.currentTimeMillis() - this.lII >= 1500L) {
                  this.lIIl = null;
                  this.IIIIIl = -1;
                  this.III = 0;
                  this.lllIl = 0L;
                  this.IIllI = false;
                  this.llIII.lIlI();
               }
            }
         } else {
            this.IlIlIII(var1, this.lIIl, this.IIIIIl);
         }
      } else {
         this.lIIl = null;
         this.IIIIIl = -1;
         this.III = 0;
         this.lllIl = 0L;
         this.IIllI = false;
         this.llIII.lIlI();
      }
   }

   private void lIlIl(MinecraftClient var1) {
      boolean var3 = RotationMode.values()[config.safeAnchorRotationMode] == RotationMode.Il && this.IIIlII(var1);
      if (var3 || var1.player == null || var1.player.age != this.lllI) {
         boolean var4 = var3 ? this.lIIIII(var1) : this.IIIIII(var1);
         if (!var4 && AnchorGeometry.llI()) {
            Placement var5 = this.IlIlIlI(var1);
            if (var5 != null) {
               int var6 = this.llllI;
               if (var6 >= 0 && var6 < 9) {
                  boolean var7 = this.lIlIl;
                  boolean var8 = this.IIlIl;
                  if (var1.player != null) {
                     this.IIIIlI = var1.player.age + 2;
                  }

                  BooleanSupplier var9 = () -> this.lllI(var1, var5, var7, var8, var6);
                  if (RotationMode.values()[config.safeAnchorRotationMode] == RotationMode.Il) {
                     boolean var10000;
                     if (this.lIlll) {
                        var10000 = RotationSupport.lIIll(var1, this, 175, var5.l(), var9);
                     } else {
                        var10000 = RotationSupport.llIll(var1, this, 175, var5.l(), var9);
                     }

                     if (!var10000) {
                        this.IlI = System.currentTimeMillis();
                     }
                  } else {
                     var9.getAsBoolean();
                  }

                  return;
               }

               if (Boolean.valueOf(config.safeAnchorExplodeUnsafe) && Boolean.valueOf(config.safeAnchorAutoExplode) && this.Illll != null && !this.IIlIl) {
                  this.lIll(var1);
               } else {
                  this.IIIllI(var1);
               }
            } else if (!this.IIlII(var1)) {
               if (Boolean.valueOf(config.safeAnchorExplodeUnsafe) && Boolean.valueOf(config.safeAnchorAutoExplode) && this.Illll != null && !this.IIlIl) {
                  this.lIll(var1);
               } else {
                  if (this.IIlIl && this.Illll != null) {
                     this.IIl(this.Illll);
                  }

                  this.IIIllI(var1);
               }
            }
         } else {
            this.IlI = System.currentTimeMillis();
         }
      }
   }

   private boolean lIllI(MinecraftClient var1) {
      return var1 != null && var1.player != null && var1.options != null && var1.options.useKey != null
         ? this.lIIll != null || var1.player.age <= this.IIIIlI
         : false;
   }

   private Placement lIlll(MinecraftClient var1, BlockPos var2) {
      if (var1 != null && var1.player != null && var1.world != null && var2 != null) {
         if (SafetyPlacement.values()[config.safeAnchorSafetyPlacement] == SafetyPlacement.II) {
            return this.IIIll(var1, var2);
         } else {
            Vec3d var4 = this.llIl(var1);
            Vec3d var5 = Vec3d.ofCenter(var2);
            double var6 = var4.x - var5.x;
            double var8 = var4.z - var5.z;
            double var10 = Math.sqrt(var6 * var6 + var8 * var8);
            double var12 = var10 > 1.0E-4 ? var6 / var10 : 0.0;
            double var14 = var10 > 1.0E-4 ? var8 / var10 : 0.0;
            Direction var16 = var10 > 1.0E-4
               ? (Math.abs(var6) >= Math.abs(var8) ? (var6 > 0.0 ? Direction.EAST : Direction.WEST) : (var8 > 0.0 ? Direction.SOUTH : Direction.NORTH))
               : Direction.NORTH;
            Direction var17 = var10 > 1.0E-4
               ? (Math.abs(var6) >= Math.abs(var8) ? (var8 > 0.0 ? Direction.SOUTH : Direction.NORTH) : (var6 > 0.0 ? Direction.EAST : Direction.WEST))
               : Direction.EAST;
            boolean var18 = var10 > 1.0E-4 && Math.min(Math.abs(var6), Math.abs(var8)) / var10 > 0.3;
            ArrayList<BlockPos> var19 = new ArrayList<>();
            var19.add(var2.offset(var16, 2));
            if (var18) {
               var19.add(var2.offset(var16, 1).offset(var17, 1));
               var19.add(var2.offset(var16, 2).offset(var17, 1));
               var19.add(var2.offset(var16, 1).offset(var17, 2));
               var19.add(var2.offset(var17, 2));
            } else {
               var19.add(var2.offset(var16, 1).offset(var17, 1));
               var19.add(var2.offset(var16, 1).offset(var17.getOpposite(), 1));
               var19.add(var2.offset(var17, 2));
            }

            for (Direction var23 : I) {
               BlockPos var24 = var2.offset(var23, 2);
               if (!var19.contains(var24)) {
                  var19.add(var24);
               }
            }

            Placement var27 = this.lIIIl(var1, var2, var19, var4, var12, var14, var10);
            if (var27 != null) {
               return var27;
            } else {
               ArrayList<BlockPos> var28 = new ArrayList<>();
               var28.add(var2.offset(var16, 1));
               if (var18) {
                  var28.add(var2.offset(var16, 1).offset(var17, 1));
                  var28.add(var2.offset(var17, 1));
               } else {
                  var28.add(var2.offset(var16, 1).offset(var17, 1));
                  var28.add(var2.offset(var16, 1).offset(var17.getOpposite(), 1));
                  var28.add(var2.offset(var17, 1));
               }

               for (Direction var25 : I) {
                  BlockPos var26 = var2.offset(var25, 1);
                  if (!var28.contains(var26)) {
                     var28.add(var26);
                  }
               }

               return this.lIIIl(var1, var2, var28, var4, var12, var14, var10);
            }
         }
      } else {
         return null;
      }
   }

   private boolean llIII(MinecraftClient var1, Placement var2) {
      if (var1 != null && var1.world != null && var2 != null && var2.I() != null) {
         BlockHitResult var3 = var2.I();
         return this.IIlIlI(var1.world.getBlockState(var2.II()))
            && !this.IlIllI(var1, var2.II())
            && var2.II().equals(var3.getBlockPos().offset(var3.getSide()))
            && this.IllIll(var1, var3.getBlockPos())
            && this.IlllIl(var1, var2);
      } else {
         return false;
      }
   }

   private boolean llIIl(MinecraftClient var1) {
      return false;
   }

   public void llIll(MinecraftClient var1, Hand var2, BlockHitResult var3) {
      if (!this.IIIll) {
         this.IIllIl();
         if (config != null
            && config.safeAnchorEnabled
            && this.llII(var1)
            && var2 == Hand.MAIN_HAND
            && var3 != null
            && var3.getType() == Type.BLOCK
            && var1.player.getMainHandStack().isOf(Items.RESPAWN_ANCHOR)) {
            if (config.anchorEnabled && (AnchorMacro.isRunning() || var1.world.getBlockState(var3.getBlockPos()).isOf(Blocks.RESPAWN_ANCHOR))) {
               return;
            }

            BlockState var7 = var1.world.getBlockState(var3.getBlockPos());
            if (var7.isOf(Blocks.RESPAWN_ANCHOR)) {
               if (this.lIIll == null && this.lIIl == null && this.lIIl()) {
                  this.l(var1, var3.getBlockPos().toImmutable());
               }
            } else {
               BlockPos var8 = !var7.isReplaceable() && !this.IIlIlI(var7) ? var3.getBlockPos().offset(var3.getSide()) : var3.getBlockPos();
               if (this.IIlIlI(var1.world.getBlockState(var8))) {
                  this.llII = var1.player.age;
                  this.lIlI = var8.toImmutable();
               }
            }
         }
      }
   }

   public void IIIII() {
      MinecraftClient var1 = MinecraftClient.getInstance();
      if (this.llIIl(var1)) {
         this.IlIIIl(var1);
      } else if (config != null && config.safeAnchorEnabled && this.llII(var1)) {
         long var2 = System.currentTimeMillis();
         this.IllI.entrySet().removeIf(entry -> IllllI(var2, (Entry<BlockPos, Long>)entry));
         this.IIlIl(var1);
         if (!RotationSupport.lIIlIl() && !SafeAnchorSupport.lIlIIl(this)) {
            if (this.lIIll != null && this.IIIIll > 0L && System.currentTimeMillis() - this.IIIIll > 4000L) {
               this.IlIlllI();
            }

            if (this.lIIl != null && this.lllIl > 0L && System.currentTimeMillis() - this.lllIl > 1500L) {
               this.lIIl = null;
               this.IIIIIl = -1;
               this.III = 0;
               this.lllIl = 0L;
               this.IIllI = false;
               this.llIII.lIlI();
            }

            if (this.lIIl != null) {
               this.lIlII(var1);
            } else if (this.lIIll != null) {
               this.IlIIII();
            } else if (this.lIlI != null) {
               this.lIIlII(var1);
            } else {
               if (config.anchorEnabled && AnchorMacro.isRunning()) {
                  return;
               }

               if (this.IIllI && this.lIIl == null) {
                  this.IIllI = false;
                  this.llIII.lIlI();
               }

               if (Boolean.valueOf(config.safeAnchorAutoTarget) && System.currentTimeMillis() - this.lIl >= Math.max(this.IIlll(), 75L)) {
                  this.lll(var1);
               }
            }
         }
      }
   }

   private boolean lllII(MinecraftClient var1, BlockPos var2) {
      if (var1 != null && var1.world != null && var1.player != null && var2 != null) {
         Vec3d var3 = this.llIl(var1);
         Vec3d var4 = Vec3d.ofCenter(var2);
         double var5 = var3.x - var4.x;
         double var7 = var3.z - var4.z;
         double var9 = Math.sqrt(var5 * var5 + var7 * var7);
         if (var9 > 1.0E-4) {
            Direction var11 = Math.abs(var5) >= Math.abs(var7)
               ? (var5 > 0.0 ? Direction.EAST : Direction.WEST)
               : (var7 > 0.0 ? Direction.SOUTH : Direction.NORTH);
            BlockPos var12 = var2.offset(var11, 2);
            BlockPos var13 = var2.offset(var11, 1);
            if (this.IllIIII(var1, var12) || this.IllIIII(var1, var13)) {
               return true;
            }
         }

         return false;
      } else {
         return false;
      }
   }

   private boolean lllIl(MinecraftClient var1) {
      if (this.IlIII) {
         int var2 = this.IIlIll(var1, this.Illll);
         if (var2 < this.Il) {
            if (var1.player.age <= this.lll) {
               return true;
            } else {
               this.IlIII = false;
               this.Il = -1;
               this.lll = Integer.MIN_VALUE;
               this.IIlII = 0;
               this.IIlI(var1);
               return true;
            }
         } else {
            this.IlIII = false;
            this.Il = -1;
            this.lll = Integer.MIN_VALUE;
            this.IIlII = 0;
            this.IIlI(var1);
            return this.lIIll == null || System.currentTimeMillis() < this.IlI;
         }
      } else {
         return false;
      }
   }

   private static boolean llllI(double var0, double var2, double var4, double var6, double var8, double var10) {
      if (Math.abs(var0 - var6) > 1.0E-4) {
         return var0 < var6;
      } else {
         return Math.abs(var2 - var8) > 1.0E-4 ? var2 < var8 : var4 < var10;
      }
   }

   // $VF: Unable to simplify switch-on-enum, as the enum class was not able to be found.
   // Please report this to the Vineflower issue tracker, at https://github.com/Vineflower/vineflower/issues with a copy of the class file (if you have the rights to distribute it!)
   private boolean lllll(TargetScore var1, TargetScore var2) {
      if (var2 == null) {
         return true;
      } else {
         return switch (TargetMode.values()[config.safeAnchorTargetMode].ordinal()) {
            case 0 -> llllI(var1.II(), var1.Il(), var1.I(), var2.II(), var2.Il(), var2.I());
            case 1 -> llllI(var1.I(), var1.Il(), var1.II(), var2.I(), var2.Il(), var2.II());
            case 2 -> llllI(var1.Il(), var1.II(), var1.I(), var2.Il(), var2.II(), var2.I());
            default -> throw new IllegalStateException("Unknown target mode");
         };
      }
   }

   private boolean IIIIII(MinecraftClient var1) {
      return this.llIIl(var1)
         || SafeAnchorSupport.IlllIll(var1)
         || SafeAnchorSupport.lllII(var1)
         || SafeAnchorSupport.IIIIlI(var1)
         || SafeAnchorSupport.IllIIII();
   }

   private static boolean IIIIIl(Entity var0) {
      return !var0.isSpectator() && !var0.isRemoved();
   }

   public void IIIIlI(MinecraftClient var1, Hand var2, BlockHitResult var3, ActionResult var4) {
      if (this.llII != Integer.MIN_VALUE && !this.IIIll) {
         if (config.anchorEnabled && AnchorMacro.isRunning()) {
            this.IIllIl();
            return;
         }

         int var9 = var1.player.age - this.llII;
         if (config != null && config.safeAnchorEnabled && this.llII(var1) && var2 == Hand.MAIN_HAND && var9 >= 0 && var9 <= 10 && this.lIlI != null) {
            if (var4 != null && var4.isAccepted()) {
               BlockPos var7 = this.lIlI.toImmutable();
               this.IIllIl();
               this.IlIl(var7);
               if (var1.player != null) {
                  int var8 = SafeAnchorSupport.IIlIlII(var1);
                  if (var8 < 4) {
                     SafeAnchorSupport.setUseCooldown(var1, 4);
                  }
               }

               if (this.lIIl()) {
                  this.l(var1, var7);
               }

               return;
            }

            return;
         }

         this.IIllIl();
      }
   }

   private boolean IIIIll(MinecraftClient var1, BlockPos var2, Vec3d var3) {
      if (var1 != null && var1.player != null) {
         Box var4 = new Box(var2.getX(), var2.getY(), var2.getZ(), var2.getX() + 1.0, var2.getY() + 1.0, var2.getZ() + 1.0);
         Box var5 = var1.player.getBoundingBox();
         if (var4.intersects(var5)) {
            return true;
         } else {
            if (var3 != null) {
               Vec3d var6 = SafeAnchorSupport.position(var1.player);
               Vec3d var7 = var3.subtract(var6);
               if (var7.lengthSquared() > 0.0025) {
                  Box var8 = var5.offset(var7);
                  if (var4.intersects(var8)) {
                     return true;
                  }
               }
            }

            return false;
         }
      } else {
         return true;
      }
   }

   private boolean IIIlII(MinecraftClient var1) {
      return var1 != null
         && var1.player != null
         && var1.player.age == this.lllI
         && SafeAnchorSupport.lllllI() == this.lIIII
         && SafeAnchorSupport.IIlIIlI() == this.IlllI;
   }

   public boolean IIIlIl() {
      return this.IIIll;
   }

   private void IIIllI(MinecraftClient var1) {
      if (Boolean.valueOf(config.safeAnchorAutoTarget)) {
         this.lIl = System.currentTimeMillis();
      }

      this.IlIlllI();
      if (var1 != null && var1.player != null) {
         int var2 = SafeAnchorSupport.IIlIlII(var1);
         if (var2 < 4) {
            SafeAnchorSupport.setUseCooldown(var1, 4);
         }
      }
   }

   private Placement IIIlll(MinecraftClient var1, BlockPos var2) {
      BlockHitResult var3 = this.IlllII(var1);
      if (var3 == null || var2 == null || var1 == null || var1.world == null) {
         return null;
      } else if (!this.IIlIlI(var1.world.getBlockState(var2))) {
         return null;
      } else {
         BlockPos var4 = var3.getBlockPos();
         BlockPos var5 = var4.offset(var3.getSide());
         if (var2.equals(var5) && this.IllIll(var1, var4)) {
            Placement var6 = new Placement(var2.toImmutable(), var3.getPos(), var3);
            return this.IlllIl(var1, var6) ? var6 : null;
         } else {
            return null;
         }
      }
   }

   private boolean IIlIII(MinecraftClient var1, int var2) {
      if (var1 != null && var1.player != null && var2 >= 0 && var2 < 9) {
         ItemStack var3 = var1.player.getInventory().getStack(var2);
         return var3 != null && !var3.isEmpty() && (var3.isOf(Items.OBSIDIAN) || var3.isOf(Items.CRYING_OBSIDIAN) || var3.isOf(Items.GLOWSTONE));
      } else {
         return false;
      }
   }

   public void lIIII(MinecraftClient var1) {
      if (this.llIIl(var1)) {
         this.IlIIIl(var1);
      } else if (config != null && config.safeAnchorEnabled && this.llII(var1) && !RotationSupport.lIIlIl() && !SafeAnchorSupport.lIlIIl(this)) {
         if (this.lIIl != null) {
            this.lIlII(var1);
         } else if (this.lIIll != null) {
            this.IlIIII();
         } else if (this.lIlI != null) {
            this.lIIlII(var1);
         } else if (Boolean.valueOf(config.safeAnchorAutoTarget) && System.currentTimeMillis() - this.lIl >= Math.max(this.IIlll(), 75L)) {
            this.lll(var1);
         }
      }
   }

   static boolean IIlIIl(Box var0, Box var1, Vec3d var2) {
      return var1.stretch(var2).intersects(var0);
   }

   private boolean IIlIlI(BlockState var1) {
      return var1 != null
         && (
            var1.isAir()
               || var1.isReplaceable()
               || SafeAnchorSupport.IIllII(var1)
               || var1.isOf(Blocks.FIRE)
               || var1.isOf(Blocks.SOUL_FIRE)
               || var1.isOf(Blocks.SNOW)
         );
   }

   private int IIlIll(MinecraftClient var1, BlockPos var2) {
      if (var1 != null && var1.world != null && var2 != null) {
         BlockState var3 = var1.world.getBlockState(var2);
         return var3.isOf(Blocks.RESPAWN_ANCHOR) ? (Integer)var3.get(RespawnAnchorBlock.CHARGES) : 0;
      } else {
         return 0;
      }
   }

   private int IIllII(MinecraftClient var1) {
      int var2 = this.III(var1);
      int var3 = this.lIIlI(var1);
      SafeBlock var4 = SafeBlock.values()[config.safeAnchorSafeBlock];
      if (var4 == SafeBlock.Il) {
         return var2 >= 0 ? var2 : var3;
      } else if (var4 == SafeBlock.I) {
         return var3 >= 0 ? var3 : var2;
      } else if (var2 >= 0 && var3 >= 0) {
         return ThreadLocalRandom.current().nextBoolean() ? var2 : var3;
      } else {
         return var2 >= 0 ? var2 : var3;
      }
   }

   private SafeAnchorMacro() {
      this.IllI = new HashMap<>();
      this.Illl = new HashSet<>();
      this.llIII = new AnchorRotation();
      this.llII = this.llIl = this.lllI = this.IIIIlI = this.IIlI = this.lll = this.IlIIl = Integer.MIN_VALUE;
      this.llllI = this.IIIlI = this.IIIIIl = this.Il = -1;
      this.IlI = this.lIIII = this.IlllI = Long.MIN_VALUE;
   }

   public void IIllIl() {
      this.llII = Integer.MIN_VALUE;
      this.lIlI = null;
   }

   private Placement IIlllI(MinecraftClient var1, PlayerEntity var2) {
      if (var1 != null && var1.world != null && var1.player != null && var2 != null) {
         double var4 = Math.max(0.0, var1.player.getBlockInteractionRange() - 0.1);
         double var6 = var4 * var4;
         Vec3d var8 = var1.player.getEyePos();
         BlockPos var9 = var2.getBlockPos();
         Placement var10 = null;
         float var11 = Float.NEGATIVE_INFINITY;

         for (int var12 = -3; var12 <= 3; var12++) {
            for (int var13 = -2; var13 <= 2; var13++) {
               for (int var14 = -3; var14 <= 3; var14++) {
                  BlockPos var15 = var9.add(var12, var13, var14);
                  if (!(var8.squaredDistanceTo(Vec3d.ofCenter(var15)) > var6)
                     && this.IIlIlI(var1.world.getBlockState(var15))
                     && !this.IlIllI(var1, var15)
                     && !this.IIIIll(var1, var15, SafeAnchorSupport.position(var1.player))) {
                     Placement var16 = this.IlIIlI(var1, var15);
                     if (var16 != null && this.IlllIl(var1, var16)) {
                        float var17 = AnchorDamage.lI(var2, var15, null, SafeAnchorSupport.position(var2));
                        if (!(var17 * 2.0F <= var11)
                           && (!Boolean.valueOf(config.safeAnchorEnforceMinDamage) || !(var17 < Double.valueOf(config.safeAnchorMinDamage).floatValue()))) {
                           Placement var18 = this.lIlll(var1, var15);
                           float var19 = var18 != null
                              ? AnchorDamage.lI(var1.player, var15, var18.II(), this.llIl(var1))
                              : AnchorDamage.lI(var1.player, var15, null, this.llIl(var1));
                           if ((var18 != null || Boolean.valueOf(config.safeAnchorExplodeUnsafe))
                              && (!(var19 >= var17) || Boolean.valueOf(config.safeAnchorExplodeUnsafe))) {
                              float var20 = var17 * 2.0F - var19;
                              if (var20 > var11) {
                                 var11 = var20;
                                 var10 = var16;
                              }
                           }
                        }
                     }
                  }
               }
            }
         }

         return var10;
      } else {
         return null;
      }
   }

   private void IlIIII() {
      MinecraftClient var2 = MinecraftClient.getInstance();
      if (config != null && config.safeAnchorEnabled && this.llII(var2)) {
         if (config.anchorEnabled && AnchorMacro.isRunning()) {
            return;
         }

         if (System.currentTimeMillis() >= this.IlI) {
            if (this.IIIIll > 0L && System.currentTimeMillis() - this.IIIIll > 3000L) {
               if (this.Illll != null) {
                  this.IIl(this.Illll);
               }

               this.IIIllI(var2);
            } else if (!this.lllIl(var2)) {
               boolean var8 = this.IIlIl || (this.lIlIl ? this.lIIll(var2, this.llllI) : this.IIlIII(var2, this.llllI));
               if (var8) {
                  if (RotationMode.values()[config.safeAnchorRotationMode] == RotationMode.II && !this.IIllI) {
                     this.IIllI = true;
                     this.lII = System.currentTimeMillis();
                  }

                  if (this.IIllI) {
                     Placement var5 = this.IlIlIlI(var2);
                     if (var5 == null) {
                        this.llIII.lIlI();
                        if (!this.IIlII(var2)) {
                           if (Boolean.valueOf(config.safeAnchorExplodeUnsafe)
                              && Boolean.valueOf(config.safeAnchorAutoExplode)
                              && this.Illll != null
                              && !this.IIlIl) {
                              this.IIllI = false;
                              this.lIll(var2);
                           } else {
                              if (this.Illll != null) {
                                 this.IIl(this.Illll);
                              }

                              this.IlIlllI();
                           }
                        }
                     } else {
                        boolean var6 = this.IllIIlI(var2, var5);
                        if (var6) {
                           this.IIllI = false;
                           this.llIII.lIlI();
                           this.lIlIl(var2);
                        } else {
                           float var7 = this.llIII.IIIllI(var2, var5.I(), Double.valueOf(config.safeAnchorStrength).floatValue());
                           if (var7 <= 3.5F) {
                              this.IIllI = false;
                              this.llIII.lIlI();
                              this.lIlIl(var2);
                           } else if (var7 == Float.MAX_VALUE) {
                              this.IIllI = false;
                              if (Boolean.valueOf(config.safeAnchorExplodeUnsafe)
                                 && Boolean.valueOf(config.safeAnchorAutoExplode)
                                 && this.Illll != null
                                 && !this.IIlIl) {
                                 this.lIll(var2);
                              } else {
                                 if (this.Illll != null) {
                                    this.IIl(this.Illll);
                                 }

                                 this.IlIlllI();
                              }
                           } else if (System.currentTimeMillis() - this.lII >= 1500L) {
                              this.IIllI = false;
                              if (Boolean.valueOf(config.safeAnchorExplodeUnsafe)
                                 && Boolean.valueOf(config.safeAnchorAutoExplode)
                                 && this.Illll != null
                                 && !this.IIlIl) {
                                 this.lIll(var2);
                              } else {
                                 if (this.Illll != null) {
                                    this.IIl(this.Illll);
                                 }

                                 this.IlIlllI();
                              }
                           }
                        }
                     }
                  } else {
                     this.lIlIl(var2);
                  }
               } else if (Boolean.valueOf(config.safeAnchorExplodeUnsafe) && Boolean.valueOf(config.safeAnchorAutoExplode) && this.Illll != null) {
                  this.lIll(var2);
               } else {
                  this.IlIlllI();
               }
            }
         }
      } else {
         this.IlIlllI();
      }
   }

   public void IlIIIl(MinecraftClient var1) {
      this.IIllIl();
      this.IlIlllI();
   }

   private Placement IlIIlI(MinecraftClient var1, BlockPos var2) {
      if (var1 != null && var1.world != null && var2 != null) {
         if (!this.IIlIlI(var1.world.getBlockState(var2))) {
            return null;
         } else if (this.IlIllI(var1, var2)) {
            return null;
         } else {
            BlockHitResult var4 = AnchorGeometry.lI(var1.world, var1.player.getEyePos(), var2);
            if (var4 != null && this.IllIll(var1, var4.getBlockPos())) {
               Placement var5 = this.lIlI(var1, var2, var4.getBlockPos(), var4.getSide());
               if (var5 != null && this.IlllIl(var1, var5)) {
                  return var5;
               }
            }

            BlockPos var14 = var2.down();
            if (this.IllIll(var1, var14)) {
               Placement var6 = this.lIlI(var1, var2, var14, Direction.UP);
               if (var6 != null && this.IlllIl(var1, var6)) {
                  return var6;
               }
            }

            Direction var15 = this.IllIIIl(var1, var2);
            Direction[] var7 = new Direction[]{var15, var15.getOpposite(), var15.rotateYClockwise(), var15.rotateYCounterclockwise(), Direction.DOWN};

            for (Direction var11 : var7) {
               BlockPos var12 = var2.offset(var11.getOpposite());
               if (this.IllIll(var1, var12)) {
                  Placement var13 = this.lIlI(var1, var2, var12, var11);
                  if (var13 != null && this.IlllIl(var1, var13)) {
                     return var13;
                  }
               }
            }

            return null;
         }
      } else {
         return null;
      }
   }

   private int IlIIll(PlayerEntity var1, Item var2) {
      for (int var4 = 0; var4 < 9; var4++) {
         if (var1.getInventory().getStack(var4).isOf(var2)) {
            return var4;
         }
      }

      return -1;
   }

   private boolean IlIlII(MinecraftClient var1, Placement var2) {
      if (!this.llIII(var1, var2)) {
         return false;
      } else if (RotationMode.values()[config.safeAnchorRotationMode] != RotationMode.Il
         || RotationSupport.lllIIl(var1, var2.I(), false)
         || !Boolean.valueOf(config.safeAnchorRaycast) && this.IlllIl(var1, var2)) {
         this.IIIll = true;

         boolean var3;
         try {
            var3 = SafeAnchorSupport.lIlIl(var1, var2.I());
         } finally {
            this.IIIll = false;
         }

         return var3;
      } else {
         return false;
      }
   }

   private boolean IlIlIl(MinecraftClient var1, Placement var2, int var3) {
      if (this.IIIIII(var1)) {
         this.lIIl = var2;
         this.IIIIIl = var3;
         this.lIl = System.currentTimeMillis() - this.IIlll();
         return false;
      } else if (!this.llIII(var1, var2)) {
         this.IIIIl(var1, var2.II());
         return false;
      } else if (RotationMode.values()[config.safeAnchorRotationMode] != RotationMode.Il
         || RotationSupport.lllIIl(var1, var2.I(), false)
         || !Boolean.valueOf(config.safeAnchorRaycast) && this.IlllIl(var1, var2)) {
         boolean var5 = SafeAnchorSupport.llIll(var1, this, var3, false, () -> this.IlIlII(var1, var2));
         if (var5) {
            this.l(var1, var2.II());
            this.III = 0;
            this.lllIl = 0L;
            this.lIl = System.currentTimeMillis();
            this.lllI = var1.player.age;
            this.lIIII = SafeAnchorSupport.lllllI();
            this.IlllI = SafeAnchorSupport.IIlIIlI();
            this.Ill(var1);
         } else if (this.lIIl == null) {
            this.IIIIl(var1, var2.II());
         }

         return var5;
      } else {
         this.IIIIl(var1, var2.II());
         return false;
      }
   }

   private boolean IlIllI(MinecraftClient var1, BlockPos var2) {
      if (var1 != null && var1.world != null && var2 != null) {
         Box var4 = new Box(var2);
         if (!var1.world.getOtherEntities(null, var4, SafeAnchorMacro::IIIIIl).isEmpty()) {
            return true;
         } else {
            for (PlayerEntity var6 : var1.world.getPlayers()) {
               if (!var6.isSpectator() && !var6.isRemoved()) {
                  Box var7 = var6.getBoundingBox();
                  Vec3d var8 = SafeAnchorSupport.position(var6).subtract(SafeAnchorSupport.previousPosition(var6));
                  if (var8.lengthSquared() <= 16.0 && IIlIIl(var4, var7, var8) || IIlIIl(var4, var7, var6.getVelocity())) {
                     return true;
                  }
               }
            }

            return false;
         }
      } else {
         return true;
      }
   }

   private int IlIlll(DelayRange var1) {
      double var2 = Math.max(0.0, Math.max(var1.lIl(), var1.III()));
      return Math.max(0, (int)Math.ceil(var2 / 50.0));
   }

   private int IllIlI(MinecraftClient var1) {
      if (var1 != null && var1.player != null) {
         ItemStack var3 = var1.player.getMainHandStack();
         if (var3 != null && var3.isOf(Items.RESPAWN_ANCHOR)) {
            return SafeAnchorSupport.llllII(var1.player.getInventory());
         } else {
            for (int var4 = 0; var4 < 9; var4++) {
               ItemStack var5 = var1.player.getInventory().getStack(var4);
               if (var5 != null && var5.isOf(Items.RESPAWN_ANCHOR)) {
                  return var4;
               }
            }

            return -1;
         }
      } else {
         return -1;
      }
   }

   private boolean IllIll(MinecraftClient var1, BlockPos var2) {
      if (var1 != null && var1.world != null && var2 != null) {
         BlockState var3 = var1.world.getBlockState(var2);
         return !this.IIlIlI(var3) && !var3.getCollisionShape(var1.world, var2).isEmpty();
      } else {
         return false;
      }
   }

   private BlockHitResult IlllII(MinecraftClient var1) {
      if (var1 != null && var1.world != null && var1.player != null) {
         if (var1.crosshairTarget instanceof BlockHitResult var2
            && var2.getType() == Type.BLOCK
            && !SafeAnchorSupport.IIllII(var1.world.getBlockState(var2.getBlockPos()))) {
            return var2;
         } else {
            Vec3d var7 = var1.player.getEyePos();
            Vec3d var8 = var1.player.getRotationVec(1.0F);
            double var4 = var1.player.getBlockInteractionRange();
            BlockHitResult var6 = this.IlIIlIl(var1, var7, var7.add(var8.multiply(var4)));
            return var6 != null && var6.getType() == Type.BLOCK ? var6 : null;
         }
      } else {
         return null;
      }
   }

   private boolean IlllIl(MinecraftClient var1, Placement var2) {
      if (var1 != null && var1.player != null && var1.world != null && var2 != null) {
         double var3 = Math.max(0.0, var1.player.getBlockInteractionRange() - 0.1);
         Vec3d var5 = var1.player.getEyePos();
         if (var5.squaredDistanceTo(var2.l()) > var3 * var3) {
            return false;
         } else {
            Vec3d var6 = Vec3d.of(var2.I().getSide().getVector());
            if (var5.subtract(var2.l()).dotProduct(var6) <= 0.05) {
               return false;
            } else if (!AnchorGeometry.lll(var5, AnchorGeometry.lIl(var1.world, var2.I().getBlockPos()), var2.I().getSide())) {
               return false;
            } else if (!Boolean.valueOf(config.safeAnchorRaycast)) {
               BlockHitResult var9 = this.IlIIlIl(var1, var5, var2.l());
               return var9 == null || var9.getType() == Type.MISS || var9.getBlockPos().equals(var2.I().getBlockPos()) || var9.getBlockPos().equals(var2.II());
            } else {
               Vec3d var7 = var2.l().subtract(var6.multiply(0.03125));
               BlockHitResult var8 = this.IlIIlIl(var1, var5, var7);
               return var8 != null && var8.getType() == Type.BLOCK && var8.getBlockPos().equals(var2.I().getBlockPos()) && var8.getSide() == var2.I().getSide();
            }
         }
      } else {
         return false;
      }
   }

   private static boolean IllllI(long var0, Entry<BlockPos, Long> var2) {
      return var2.getValue() <= var0;
   }

   public boolean Illlll() {
      return config != null && config.safeAnchorEnabled && this.lIlI != null;
   }

   private boolean lIIIII(MinecraftClient var1) {
      return this.llIIl(var1) || SafeAnchorSupport.IlllIll(var1) || SafeAnchorSupport.IIIIlI(var1) || SafeAnchorSupport.IllIIII();
   }

   public boolean lIIIIl() {
      return this.IIIll;
   }

   static RaycastContext lIIIlI(Vec3d var0, Vec3d var1, ShapeContext var2) {
      return new SafeAnchorRaycastContext(var0, var1, ShapeType.OUTLINE, FluidHandling.NONE, var2);
   }

   private Placement lIIIll(MinecraftClient var1, BlockPos var2) {
      if (var1 != null && var1.world != null && var2 != null) {
         if (!var1.world.getBlockState(var2).isReplaceable() && !this.IIlIlI(var1.world.getBlockState(var2))) {
            return null;
         } else if (this.IlIllI(var1, var2)) {
            return null;
         } else {
            BlockHitResult var4 = AnchorGeometry.IlII(var1.world, var1.player.getEyePos(), var2, this.Illll);
            if (var4 != null && this.IllIll(var1, var4.getBlockPos())) {
               Placement var5 = this.lIlI(var1, var2, var4.getBlockPos(), var4.getSide());
               if (var5 != null && this.IlllIl(var1, var5)) {
                  return var5;
               }
            }

            BlockPos var14 = var2.down();
            if (this.IllIll(var1, var14) && (this.Illll == null || !var14.equals(this.Illll))) {
               Placement var6 = this.lIlI(var1, var2, var14, Direction.UP);
               if (var6 != null && this.IlllIl(var1, var6)) {
                  return var6;
               }
            }

            Direction var15 = this.IllIIIl(var1, var2);
            Direction[] var7 = new Direction[]{var15, var15.getOpposite(), var15.rotateYClockwise(), var15.rotateYCounterclockwise()};

            for (Direction var11 : var7) {
               BlockPos var12 = var2.offset(var11.getOpposite());
               if (this.IllIll(var1, var12) && (this.Illll == null || !var12.equals(this.Illll))) {
                  Placement var13 = this.lIlI(var1, var2, var12, var11);
                  if (var13 != null && this.IlllIl(var1, var13)) {
                     return var13;
                  }
               }
            }

            return null;
         }
      } else {
         return null;
      }
   }

   public void IlI() {
      this.IIllIl();
      this.IlIlllI();
      this.IIIll = false;
      this.IIIIlI = Integer.MIN_VALUE;
      this.lIl = 0L;
      this.Illl.clear();
      this.lllII = null;
      this.IlIIl = Integer.MIN_VALUE;
      this.lIIl = null;
      this.IIIIIl = -1;
      this.III = 0;
      this.lllIl = 0L;
      this.IIIIll = 0L;
      this.llIII.IIIlIl();
      this.IllI.clear();
      this.IIIllI = null;
      this.IIlI = Integer.MIN_VALUE;
   }

   private void lIIlII(MinecraftClient var1) {
      if (var1 != null && var1.player != null && var1.world != null && this.lIlI != null) {
         int var2 = var1.player.age - this.llII;
         if (var2 >= 0 && var2 <= 10) {
            if (!var1.world.getBlockState(this.lIlI).isOf(Blocks.RESPAWN_ANCHOR)) {
               return;
            }

            BlockPos var3 = this.lIlI.toImmutable();
            this.IIllIl();
            this.IlIl(var3);
            if (var1.player != null) {
               int var4 = SafeAnchorSupport.IIlIlII(var1);
               if (var4 < 4) {
                  SafeAnchorSupport.setUseCooldown(var1, 4);
               }
            }

            if (this.lIIl()) {
               this.l(var1, var3);
            }

            return;
         }

         this.IIllIl();
      } else {
         this.IIllIl();
      }
   }

   private int lIIlIl(PlayerEntity var1) {
      for (int var3 = 0; var3 < 9; var3++) {
         if (this.IlIllll(var1.getInventory().getStack(var3))) {
            return var3;
         }
      }

      return -1;
   }

   private int IlIIlII(DelayRange var1) {
      return Math.max(0, (int)Math.ceil(this.IlIIlll(var1) / 50.0));
   }

   private BlockHitResult IlIIlIl(MinecraftClient var1, Vec3d var2, Vec3d var3) {
      BlockHitResult var4 = SafeAnchorSupport.IlllIl(var1, var1.player, var2, var3);
      if (var4 == null || var4.getType() != Type.BLOCK || SafeAnchorSupport.IIllII(var1.world.getBlockState(var4.getBlockPos()))) {
         var4 = var1.world.raycast(lIIIlI(var2, var3, ShapeContext.of(var1.player)));
      }

      return var4 != null && var4.getType() == Type.BLOCK ? var4 : null;
   }

   private long IlIIlll(DelayRange var1) {
      double var2 = Math.max(0.0, Math.min(var1.lIl(), var1.III()));
      double var4 = Math.max(var2, Math.max(var1.lIl(), var1.III()));
      return var2 == var4 ? Math.round(var2) : Math.round(ThreadLocalRandom.current().nextDouble(var2, var4));
   }

   private void IlIlIII(MinecraftClient var1, Placement var2, int var3) {
      this.lIIl = null;
      this.IIIIIl = -1;
      this.IIllI = false;
      this.llIII.lIlI();
      if (var3 >= 0 && var3 < 9 && this.llIII(var1, var2)) {
         BooleanSupplier var5 = () -> this.IlIlIl(var1, var2, var3);
         if (RotationMode.values()[config.safeAnchorRotationMode] == RotationMode.Il) {
            if (!RotationSupport.llIll(var1, this, 175, var2.l(), var5) && this.lIIl == null) {
               this.IIIIl(var1, var2.II());
            }
         } else {
            var5.getAsBoolean();
         }
      } else {
         this.IIIIl(var1, var2 == null ? null : var2.II());
      }
   }

   private long IlIlIIl(MinecraftClient var1, int var2) {
      long var3 = System.currentTimeMillis();
      boolean var10000;
      if (var1 != null && var1.player != null && var2 >= 0 && var2 < 9 && SafeAnchorSupport.lIlIIIl(var1) != var2) {
         var10000 = true;
      } else {
         var10000 = false;
      }

      long var6 = var10000 ? this.IlIIlll(this.II) : 0L;
      return var3 + var6 + this.IlIIlll(this.llIlI);
   }

   private Placement IlIlIlI(MinecraftClient var1) {
      Placement var3;
      if (this.lIlIl) {
         Placement var4 = this.IIIlI(var1, this.Illll);
         var3 = var4 != null ? var4 : this.llll(this.Illll);
      } else if (this.IIlIl) {
         Placement var5 = this.IIIlI(var1, this.Illll);
         var3 = var5 != null ? var5 : this.llll(this.Illll);
      } else {
         if (!this.llIII(var1, this.lIllI)) {
            this.lIllI = this.lIlll(var1, this.Illll);
            if (this.lIllI == null) {
               return null;
            }

            this.lIIll = this.lIllI.II();
         }

         Placement var6 = this.IIIl(var1, this.lIIll);
         var3 = var6 != null ? var6 : this.lIllI;
      }

      return this.IlllIl(var1, var3) ? var3 : null;
   }

   private boolean IlIlIll() {
      return Boolean.valueOf(config.safeAnchorAutoTarget) && Boolean.valueOf(config.safeAnchorChargeGlowstone) && Boolean.valueOf(config.safeAnchorAutoExplode);
   }

   private double IlIllII(MinecraftClient var1, Entity var2) {
      if (var1 != null && var1.player != null && var2 != null) {
         Vec3d var3 = var2.getBoundingBox().getCenter();
         float[] var4 = RotationSupport.IIl(var1, var3);
         if (var4 == null) {
            return 180.0;
         } else {
            float var5 = MathHelper.wrapDegrees(var4[0] - var1.player.getYaw());
            float var6 = var4[1] - var1.player.getPitch();
            return Math.hypot(var5, var6);
         }
      } else {
         return 180.0;
      }
   }

   public void IlllI(MinecraftClient var1) {
      if (this.lIllI(var1)) {
         this.IlIllIl(var1);
      }
   }

   private void IlIllIl(MinecraftClient var1) {
      SafeAnchorSupport.lIIII(var1.options.useKey);
      var1.options.useKey.setPressed(false);
      int var2 = SafeAnchorSupport.IIlIlII(var1);
      if (var2 < 4) {
         SafeAnchorSupport.setUseCooldown(var1, 4);
      }
   }

   public void IlIlllI() {
      int var3 = -200534942;
      this.IlIll = System.currentTimeMillis() + Math.max(this.IlIIlll(this.llIlI), 75L);
      MinecraftClient var2 = MinecraftClient.getInstance();
      if (this.IIIlI >= 0 && this.IIIlI < 9 && var2 != null && var2.player != null) {
         if (Boolean.valueOf(config.safeAnchorSwitchBack)) {
            SafeAnchorSupport.IIllll(var2, this, this.IIIlI);
         } else {
            SafeAnchorSupport.lIIllIl(var2, this, 0);
         }
      } else if (SafeAnchorSupport.IlIII(this)) {
         SafeAnchorSupport.IIIllI(this);
      }

      this.Illll = null;
      this.lIIll = null;
      this.llllI = -1;
      this.IIIlI = -1;
      this.IlI = Long.MIN_VALUE;
      this.llIl = Integer.MIN_VALUE;
      this.lIlIl = false;
      this.llI = 0;
      this.IIlIl = false;
      this.lIllI = null;
      this.IIl = false;
      this.IlIII = false;
      this.Il = -1;
      this.lll = Integer.MIN_VALUE;
      this.lllI = Integer.MIN_VALUE;
      this.IIlII = 0;
      this.IIIIll = 0L;
      this.lllIl = 0L;
      this.IIIIlI = Integer.MIN_VALUE;
      this.lIIl = null;
      this.IIIIIl = -1;
      this.III = 0;
      this.IIllI = false;
      this.llIII.lIlI();
   }

   private void l(MinecraftClient var1, BlockPos var2) {
      if (this.lIIll == null && !this.IlII(var1, var2)) {
         int var4 = this.lIIlI(var1);
         int var5 = this.IIllII(var1);
         int var6 = this.IIlIll(var1, var2);
         int var7 = Math.max(0, Math.min(4, config.safeAnchorCharges) - var6);
         boolean var8 = Boolean.valueOf(config.safeAnchorChargeGlowstone) && var7 > 0 && this.IllI(var1, var2);
         if (var8 && var4 < 0) {
            if (var6 <= 0) {
               return;
            }

            var8 = false;
         }

         if (var4 >= 0 || var5 >= 0) {
            this.Illll = var2.toImmutable();
            this.llIl = var1.player.age + 8;
            if (var8 || !this.lllII(var1, var2)) {
               Placement var9 = this.lIlll(var1, var2);
               if (var9 != null || var8) {
                  int var10 = var8 ? var4 : var5;
                  if (var10 < 0) {
                     var10 = var4;
                  }

                  this.IIIIll = System.currentTimeMillis();
                  if (this.IIIlI < 0 && var1 != null && var1.player != null) {
                     this.IIIlI = SafeAnchorSupport.llllII(var1.player.getInventory());
                  }

                  this.lIIll = var8
                     ? var2.toImmutable()
                     : (var9 != null ? var9.I().getBlockPos().offset(var9.I().getSide()).toImmutable() : var2.toImmutable());
                  this.lIlIl = var8;
                  this.llI = var8 ? var7 : 0;
                  this.lIllI = var8 ? this.llll(var2) : var9;
                  this.llllI = var10;
                  this.IIlIl = false;
                  this.IlI = Math.max(var8 ? this.IlIlIIl(var1, var4) : this.IlIlIIl(var1, var10), this.IlIll);
                  this.IIIIlI = var1.player.age + this.II() + 2;
               } else if (Boolean.valueOf(config.safeAnchorExplodeUnsafe) && Boolean.valueOf(config.safeAnchorAutoExplode)) {
                  if (this.IIIlI < 0 && var1 != null && var1.player != null) {
                     this.IIIlI = SafeAnchorSupport.llllII(var1.player.getInventory());
                  }

                  this.lIll(var1);
               } else {
                  this.Illll = null;
               }
            } else if (Boolean.valueOf(config.safeAnchorAutoExplode)) {
               if (this.IIIlI < 0 && var1 != null && var1.player != null) {
                  this.IIIlI = SafeAnchorSupport.llllII(var1.player.getInventory());
               }

               this.lIll(var1);
            } else {
               this.Illll = null;
            }
         } else if (Boolean.valueOf(config.safeAnchorExplodeUnsafe) && Boolean.valueOf(config.safeAnchorAutoExplode) && var6 > 0) {
            if (this.IIIlI < 0 && var1 != null && var1.player != null) {
               this.IIIlI = SafeAnchorSupport.llllII(var1.player.getInventory());
            }

            this.Illll = var2.toImmutable();
            this.lIll(var1);
         }
      }
   }

   private boolean IlIllll(ItemStack var1) {
      return var1 == null || var1.isEmpty() || !var1.isOf(Items.GLOWSTONE);
   }

   private boolean IllIIII(MinecraftClient var1, BlockPos var2) {
      if (var1 != null && var1.world != null && var2 != null) {
         BlockState var3 = var1.world.getBlockState(var2);
         return var3.isOf(Blocks.GLOWSTONE) || var3.isOf(Blocks.OBSIDIAN) || var3.isOf(Blocks.CRYING_OBSIDIAN) || !var3.isAir() && !this.IIlIlI(var3);
      } else {
         return false;
      }
   }

   private Direction IllIIIl(MinecraftClient var1, BlockPos var2) {
      Vec3d var3 = Vec3d.ofCenter(var2);
      Vec3d var4 = this.llIl(var1);
      double var5 = var4.x - var3.x;
      double var7 = var4.z - var3.z;
      if (Math.abs(var5) >= Math.abs(var7)) {
         return var5 >= 0.0 ? Direction.EAST : Direction.WEST;
      } else {
         return var7 >= 0.0 ? Direction.SOUTH : Direction.NORTH;
      }
   }

   private boolean IllIIlI(MinecraftClient var1, Placement var2) {
      if (var2 == null) {
         return false;
      } else if (!this.lIlIl && !this.IIlIl) {
         return this.IIIl(var1, this.lIIll) != null;
      } else {
         BlockHitResult var3 = this.IlllII(var1);
         return var3 != null && this.Illll != null && this.Illll.equals(var3.getBlockPos());
      }
   }
}
