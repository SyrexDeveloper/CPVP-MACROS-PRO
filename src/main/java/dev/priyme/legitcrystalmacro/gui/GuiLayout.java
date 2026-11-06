package dev.priyme.legitcrystalmacro.gui;

public final class GuiLayout {
   public final float x;
   public final float y;
   public final float scale;
   public final boolean attached;

   private GuiLayout(float x, float y, float scale, boolean attached) {
      this.x = x;
      this.y = y;
      this.scale = scale;
      this.attached = attached;
   }

   public static float ease(float t) {
      float f = Math.max(0.0F, Math.min(1.0F, t));
      return 1.0F - (1.0F - f) * (1.0F - f) * (1.0F - f);
   }

   public static GuiLayout fit(int width, int height, boolean drawer, float animation) {
      return fit(width, height, drawer ? 1.0F : 0.0F, animation);
   }

   public static GuiLayout fit(int width, int height, float drawer, float animation) {
      float base = Math.min(1.0F, Math.min(Math.max(1, width - 16) / 640.0F, Math.max(1, height - 16) / 384.0F));
      int joined = 872;
      boolean attached = drawer > 0.0F && joined * base <= width - 16;
      float total = 640.0F + (attached ? 232.0F * ease(drawer) : 0.0F);
      float scale = base * animation;
      return new GuiLayout(Math.round((width - total * scale) / 2.0F), Math.round((height - 384.0F * scale) / 2.0F), scale, attached);
   }

   public double localX(double px) {
      return (px - this.x) / this.scale;
   }

   public double localY(double py) {
      return (py - this.y) / this.scale;
   }

   public int drawerX(float progress) {
      return (this.attached ? 648 : 416) + Math.round((1.0F - ease(progress)) * 24.0F);
   }
}
