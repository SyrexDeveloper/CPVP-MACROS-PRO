package dev.priyme.legitcrystalmacro.gui;

public final class Theme {
   public static final Palette PALETTE = Palette.CRIMSON;
   public static final int ACCENT = PALETTE.accent;
   public static final int ACCENT_HOVER = PALETTE.light;
   public static final int ACCENT_MUTED = PALETTE.accentSurface;
   public static final int ACCENT_BORDER = PALETTE.accent;
   public static final int OVERLAY_BG = 1140850688;
   public static final int PANEL_BG = PALETTE.panel;
   public static final int PANEL_SHINE = PALETTE.settingsBorder;
   public static final int SHADOW = 402653184;
   public static final int CARD_BG = PALETTE.card;
   public static final int CARD_HOVER_BG = PALETTE.hover;
   public static final int CARD_ACTIVE_BG = PALETTE.activeCard;
   public static final int CARD_ACTIVE_HOVER = PALETTE.activeHover;
   public static final int CARD_ACTIVE_TITLE = PALETTE.light;
   public static final int CARD_PRESS_OVERLAY = 1140850688;
   public static final int BUTTON_BG = PALETTE.tile;
   public static final int BUTTON_HOVER_BG = PALETTE.hover;
   public static final int SEARCH_BG = PALETTE.card;
   public static final int SEPARATOR = PALETTE.hover;
   public static final int TOGGLE_OFF = PALETTE.tile;
   public static final int TOGGLE_ON = PALETTE.accent;
   public static final int TOGGLE_KNOB = -1;
   public static final int WHITE = -1;
   public static final int TEXT_PRIMARY = PALETTE.text;
   public static final int TEXT_SECONDARY = PALETTE.muted;
   public static final int TEXT_MUTED = PALETTE.muted;
   public static final int TEXT_DESCRIPTION = PALETTE.description;
   public static final int TEXT_SECTION = PALETTE.section;
   public static final int TEXT_KEY = PALETTE.keyText;
   public static final int SETTINGS_BG = PALETTE.settings;
   public static final int SETTINGS_BORDER = PALETTE.settingsBorder;
   public static final int RADIUS_PANEL = 14;
   public static final int RADIUS_CARD = 9;
   public static final int RADIUS_BUTTON = 7;
   public static final int RADIUS_CHIP = 6;
   public static final int RADIUS_TOGGLE = 7;
   public static final int RADIUS_TILE = 8;
   public static final int RADIUS_SEARCH = 10;
   public static final int PANEL_WIDTH = 640;
   public static final int PANEL_HEIGHT = 384;
   public static final int SIDEBAR_WIDTH = 164;
   public static final int CONTENT_PADDING = 12;
   public static final int CONTENT_GUTTER = 8;
   public static final int CARD_HEIGHT = 44;
   public static final int HEADER_HEIGHT = 28;
   public static final int DRAWER_WIDTH = 224;
   private static final int STEPS = 32;
   private static final int PRESS_STEPS = 16;
   private static final int[] CARDS = new int[16384];
   public static final int[] TITLES = ramp(TEXT_PRIMARY, ACCENT_HOVER);
   public static final int[] ICON_TILES = ramp(BUTTON_BG, ACCENT);
   public static final int[] ICONS = ramp(TEXT_MUTED, WHITE);
   public static final int[] TOGGLES = ramp(TOGGLE_OFF, TOGGLE_ON);
   public static final int[] KNOBS = ramp(TEXT_MUTED, WHITE);
   public static final int[] RINGS = ramp(ACCENT & 16777215, ACCENT);
   public static final int[] SIDEBAR = ramp(CARD_BG, ACCENT);
   public static final int[] PILLS = ramp(CARD_BG, ACCENT_MUTED);
   public static final int[] PILL_TEXT = ramp(TEXT_MUTED, ACCENT_HOVER);

   private Theme() {
   }

   public static int state(int[] colors, float progress) {
      return colors[Math.round(Math.max(0.0F, Math.min(1.0F, progress)) * (colors.length - 1))];
   }

   public static int card(float active, float hover, float pressed) {
      int a = Math.round(Math.max(0.0F, Math.min(1.0F, active)) * 31.0F);
      int h = Math.round(Math.max(0.0F, Math.min(1.0F, hover)) * 31.0F);
      int p = Math.round(Math.max(0.0F, Math.min(1.0F, pressed)) * 15.0F);
      return CARDS[(a * 32 + h) * 16 + p];
   }

   private static int[] ramp(int from, int to) {
      int[] colors = new int[32];

      for (int i = 0; i < 32; i++) {
         colors[i] = mix(from, to, i / 31.0F);
      }

      return colors;
   }

   private static int mix(int from, int to, float t) {
      int result = 0;

      for (int shift = 0; shift <= 24; shift += 8) {
         int a = from >>> shift & 0xFF;
         int b = to >>> shift & 0xFF;
         result |= Math.round(a + (b - a) * t) << shift;
      }

      return result;
   }

   static {
      for (int a = 0; a < 32; a++) {
         for (int h = 0; h < 32; h++) {
            int base = mix(mix(CARD_BG, CARD_HOVER_BG, h / 31.0F), mix(CARD_ACTIVE_BG, CARD_ACTIVE_HOVER, h / 31.0F), a / 31.0F);

            for (int p = 0; p < 16; p++) {
               CARDS[(a * 32 + h) * 16 + p] = mix(base, -16777216, 0.12F * p / 15.0F);
            }
         }
      }
   }
}
