package dev.priyme.legitcrystalmacro.gui;

public enum Palette {
   CRIMSON(
      -16053491,
      -15461353,
      -14869214,
      -14540248,
      -1250064,
      -7500394,
      -7697773,
      -10329493,
      -8618875,
      -2024120,
      -154449,
      -12972007,
      -13298401,
      -12773855,
      -15658732,
      -14737627
   );

   public final int panel;
   public final int card;
   public final int hover;
   public final int tile;
   public final int text;
   public final int muted;
   public final int description;
   public final int section;
   public final int keyText;
   public final int accent;
   public final int light;
   public final int accentSurface;
   public final int activeCard;
   public final int activeHover;
   public final int settings;
   public final int settingsBorder;
   public final int white = -1;
   public final int black = -16777216;
   public final int overlay = 1140850688;
   public final int shadow = 402653184;

   private Palette(
      int panel,
      int card,
      int hover,
      int tile,
      int text,
      int muted,
      int description,
      int section,
      int keyText,
      int accent,
      int light,
      int accentSurface,
      int activeCard,
      int activeHover,
      int settings,
      int settingsBorder
   ) {
      this.panel = panel;
      this.card = card;
      this.hover = hover;
      this.tile = tile;
      this.text = text;
      this.muted = muted;
      this.description = description;
      this.section = section;
      this.keyText = keyText;
      this.accent = accent;
      this.light = light;
      this.accentSurface = accentSurface;
      this.activeCard = activeCard;
      this.activeHover = activeHover;
      this.settings = settings;
      this.settingsBorder = settingsBorder;
   }
}
