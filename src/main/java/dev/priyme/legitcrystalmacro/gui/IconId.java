package dev.priyme.legitcrystalmacro.gui;

public enum IconId {
   SWORDS,
   SHIELD,
   HAMMER,
   CHEVRONS_UP,
   USER,
   SETTINGS,
   FOLDER,
   SEARCH,
   DIAMOND,
   ANCHOR,
   HEART,
   ZAP,
   CIRCLE,
   CHEVRON_DOWN,
   CLOSE;

   public int atlasX() {
      return this.ordinal() % 4 * 64;
   }

   public int atlasY() {
      return this.ordinal() / 4 * 64;
   }

   public static IconId category(String category) {
      return switch (category) {
         case "CPvP" -> SWORDS;
         case "UHC" -> SHIELD;
         case "Mace" -> HAMMER;
         case "Movement" -> CHEVRONS_UP;
         case "Player" -> USER;
         case "Configs" -> FOLDER;
         default -> SETTINGS;
      };
   }

   public static IconId module(String module) {
      return switch (module) {
         case "Crystal Macro" -> DIAMOND;
         case "Anchor Macro", "Double Anchor", "Cobweb Placer", "Pressure Web", "Plate Web" -> ANCHOR;
         case "Safe Anchor", "NoInteract", "Shield Breaker" -> SHIELD;
         case "Hover Totem", "Jump Reset", "Hover Potion" -> HEART;
         case "Crystal Tweaks", "Fast XP", "Key WindCharge", "Perfect Windcharge" -> ZAP;
         case "NoSlow", "STab Sprint Reset", "Tap Reset", "Auto Sprint", "Speed Bridge", "Trajectories" -> CHEVRONS_UP;
         case "Aim Assist", "Pearl Catch" -> SEARCH;
         case "TriggerBot", "Spear Reach", "Lunge Macro", "Spear Lunge" -> SWORDS;
         case "Key Pearl" -> DIAMOND;
         case "Stun Slam", "Breach Swap", "Mace Swap", "Auto Mace" -> HAMMER;
         case "Panic Disconnect" -> CLOSE;
         case "Name Protect" -> USER;
         case "Delay Randomizer" -> SETTINGS;
         default -> CIRCLE;
      };
   }
}
