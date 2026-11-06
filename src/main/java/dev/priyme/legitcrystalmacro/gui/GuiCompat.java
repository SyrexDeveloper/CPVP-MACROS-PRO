package dev.priyme.legitcrystalmacro.gui;

public final class GuiCompat {
   private GuiCompat() {
   }

   public static void drawRect(GuiGraphicsContext ctx, int x, int y, int width, int height, int color) {
      if (width > 0 && height > 0 && ctx != null) {
         ctx.fill(x, y, width, height, color);
      }
   }

   public static void drawRectWithBorder(GuiGraphicsContext ctx, int x, int y, int width, int height, int borderThickness, int borderColor, int bgColor) {
      if (width > 0 && height > 0 && ctx != null) {
         int thickness = Math.max(1, borderThickness);
         ctx.fill(x, y, width, height, borderColor);
         if (width > thickness * 2 && height > thickness * 2) {
            ctx.fill(x + thickness, y + thickness, width - thickness * 2, height - thickness * 2, bgColor);
         }
      }
   }

   public static void drawRoundedRect(GuiGraphicsContext ctx, int x, int y, int width, int height, int radius, int color) {
      if (width > 0 && height > 0 && ctx != null) {
         NineSliceSprite sprite = selectSprite(radius, height);
         ctx.drawNineSlice(sprite, x, y, width, height, color);
      }
   }

   public static void drawRoundedRectWithBorder(
      GuiGraphicsContext ctx, int x, int y, int width, int height, int radius, int borderThickness, int borderColor, int bgColor
   ) {
      if (width > 0 && height > 0 && ctx != null) {
         drawRoundedRect(ctx, x, y, width, height, radius, bgColor);
         NineSliceSprite borderSprite = radius >= 8 ? NineSliceSprite.BORDER_CARD : NineSliceSprite.BORDER_BUTTON;
         ctx.drawNineSlice(borderSprite, x, y, width, height, borderColor);
      }
   }

   public static void drawDropShadow(GuiGraphicsContext ctx, int x, int y, int width, int height, int radius, int shadowSize) {
      if (width > 0 && height > 0 && ctx != null) {
         int pad = 14;
         ctx.drawNineSlice(NineSliceSprite.SHADOW_PANEL, x - pad, y - pad + 3, width + 2 * pad, height + 2 * pad, Theme.SHADOW);
      }
   }

   public static void drawSmoothToggle(GuiGraphicsContext ctx, int x, int y, int width, int height, float progress, int activeColor, int idleColor) {
      if (width > 0 && height > 0 && ctx != null) {
         int trackColor = Theme.state(Theme.TOGGLES, progress);
         ctx.drawNineSlice(NineSliceSprite.PILL, x, y, width, height, trackColor);
         int knobH = Math.max(4, height - 4);
         int travel = width - knobH - 4;
         if (travel < 0) {
            travel = 0;
         }

         int knobX = x + 2 + Math.round(progress * travel);
         int knobY = y + (height - knobH) / 2;
         ctx.drawNineSlice(NineSliceSprite.PILL, knobX, knobY, knobH, knobH, Theme.state(Theme.KNOBS, progress));
      }
   }

   private static NineSliceSprite selectSprite(int radius, int height) {
      if (radius >= 12) {
         return NineSliceSprite.PANEL;
      } else if (radius == 10) {
         return NineSliceSprite.SEARCH;
      } else if (radius == 8) {
         return NineSliceSprite.TILE;
      } else if (radius == 6) {
         return NineSliceSprite.KEY_CHIP;
      } else if (radius >= 8) {
         return NineSliceSprite.CARD;
      } else if (radius >= 6) {
         return NineSliceSprite.BUTTON;
      } else if (radius >= 4) {
         return NineSliceSprite.CHIP;
      } else if (radius == 3) {
         return NineSliceSprite.RECT_R3;
      } else {
         return radius <= 2 ? NineSliceSprite.RECT_R2 : NineSliceSprite.RECT_R3;
      }
   }
}
