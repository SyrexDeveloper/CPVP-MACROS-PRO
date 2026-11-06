package dev.priyme.legitcrystalmacro.gui;

public enum NineSliceSprite {
   TILE("rect_r8", 32, 4, 68, 8),
   SEARCH("rect_r10", 40, 4, 84, 10),
   KEY_CHIP("rect_r6", 24, 4, 52, 6),
   BORDER_SEARCH("border_r10", 40, 4, 84, 10),
   BORDER_CHIP("border_r6", 24, 4, 52, 6),
   BORDER_PANEL("border_r14", 56, 4, 116, 14),
   PANEL("rect_r14", 56, 4, 116, 14),
   CARD("rect_r9", 36, 4, 76, 9),
   BUTTON("rect_r7", 28, 4, 60, 7),
   CHIP("rect_r5", 20, 4, 44, 5),
   RECT_R3("rect_r3", 12, 4, 28, 3),
   RECT_R2("rect_r2", 8, 4, 20, 2),
   PILL("rect_r14", 56, 4, 116, 14),
   BORDER_CARD("border_r9", 36, 4, 76, 9),
   BORDER_BUTTON("border_r7", 28, 4, 60, 7),
   SHADOW_PANEL("shadow_panel", 112, 4, 228, 14),
   KNOB("rect_r3", 12, 4, 28, 3);

   public final String textureName;
   public final int cornerTexels;
   public final int centerTexels;
   public final int totalTexels;
   public final int radiusGui;

   private NineSliceSprite(String textureName, int cornerTexels, int centerTexels, int totalTexels, int radiusGui) {
      this.textureName = textureName;
      this.cornerTexels = cornerTexels;
      this.centerTexels = centerTexels;
      this.totalTexels = totalTexels;
      this.radiusGui = radiusGui;
   }
}
