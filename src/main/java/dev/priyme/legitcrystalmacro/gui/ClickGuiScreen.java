package dev.priyme.legitcrystalmacro.gui;

import dev.priyme.legitcrystalmacro.config.MacroConfig;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class ClickGuiScreen extends CompatScreen {
   private static final int PANEL_WIDTH = 640;
   private static final int PANEL_HEIGHT = 384;
   private static final int SIDEBAR_WIDTH = 164;
   private static final int GRID_WIDTH = 452;
   private static final int CARD_WIDTH = 222;
   private static final int CONTENT_TOP = 48;
   private static final int CONTENT_HEIGHT = 304;
   private static final String[] CATEGORIES = new String[]{"CPvP", "UHC", "Mace", "Movement", "Player", "Other"};
   private final Map<String, List<ClickGuiScreen.Module>> modules = new LinkedHashMap<>();
   private final Map<ClickGuiScreen.Module, ClickGuiScreen.Visual> visuals = new IdentityHashMap<>();
   private final Map<ClickGuiScreen.Module, ClickGuiScreen.TextCache> texts = new IdentityHashMap<>();
   private final Map<String, Float> sidebarStates = new HashMap<>();
   private final Map<String, Float> pillStates = new HashMap<>();
   private final Map<ClickGuiScreen.Setting, Float> toggleStates = new IdentityHashMap<>();
   private final Map<String, String> counts = new HashMap<>();
   private List<ClickGuiScreen.Module> visible = List.of();
   private List<String> filterLabels = List.of("All");
   private List<ClickGuiScreen.Pill> filterPills = List.of();
   private String activeCategory = "CPvP";
   private String activeFilter = "All";
   private String searchText = "";
   private String headingCount = "";
   private boolean searchActive = false;
   private boolean textDirty = true;
   private boolean closing = false;
   private boolean draggingDrawerScroll = false;
   private final ConfigProfilesPanel configProfiles = new ConfigProfilesPanel(() -> this.textDirty = true);
   private boolean nativeTextFocused;
   private ClickGuiScreen.Module selectedModule;
   private ClickGuiScreen.Module bindingModule;
   private ClickGuiScreen.Module lastDrawnModule;
   private ClickGuiScreen.Module hoverModule;
   private ClickGuiScreen.Setting bindingSetting;
   private ClickGuiScreen.Setting draggingSetting;
   private Float draggingVisualRatio;
   private long lastFrameNs;
   private long hoverStartNs;
   private float dt = 0.016F;
   private float openProgress;
   private float closeProgress;
   private float drawerAnim;
   private float opacity = 1.0F;
   private double scrollOffset;
   private double targetScrollOffset;
   private double drawerScrollOffset;
   private double targetDrawerScrollOffset;
   private int maxScroll;
   private int maxDrawerScroll;
   private int lastDrawerClipTop;
   private int lastDrawerTotalContentH;
   private GuiLayout layout;
   private GuiGraphicsContext lastContext;
   private int measuredFills;
   private int measuredTextures;
   private int measuredTexts;
   private int measuredMatrices;
   private long measuredTimeNs;
   private int lastScreenWidth;
   private int lastScreenHeight;
   private List<ClickGuiScreen.SettingRow> settingRows = List.of();
   private List<String> drawerDescription = List.of();
   private final Map<ClickGuiScreen.Setting, String> settingLabels = new IdentityHashMap<>();
   private final Map<ClickGuiScreen.Setting, ClickGuiScreen.NumberText> numberTexts = new IdentityHashMap<>();
   private final Map<Integer, String> keyNames = new HashMap<>();
   private ClickGuiScreen.Module rowsModule;
   private static final int DRAWER_TOP = 76;
   private static final int DRAWER_CLIP = 298;

   public ClickGuiScreen() {
      this.initModules();
      this.refreshView();
   }

   public ClickGuiScreen(Object ignored) {
      this();
   }

   @Override
   protected void onGuiInitialized(GuiGraphicsContext context) {
      this.lastContext = context;
      this.configProfiles.invalidateText();
      this.prepareTexts(context);
      this.rowsModule = null;
      this.prepareSettings(context, this.selectedModule != null ? this.selectedModule : this.lastDrawnModule);
      this.lastScreenWidth = this.getScreenWidth();
      this.lastScreenHeight = this.getScreenHeight();
   }

   private void startClose() {
      if (!this.closing) {
         this.closing = true;
         this.closeProgress = 0.0F;
      }
   }

   private void setSearchActive(boolean active) {
      this.searchActive = active;
      this.syncTextInput();
   }

   private void syncTextInput() {
      boolean focused = this.searchActive || this.configView() && this.configProfiles.hasTextFocus();
      if (focused != this.nativeTextFocused) {
         this.nativeTextFocused = focused;
         this.setSearchInputFocused(focused);
      }
   }

   private boolean configView() {
      return this.activeCategory.equals("Configs") && !this.searching();
   }

   private boolean searching() {
      return !this.searchText.trim().isEmpty();
   }

   private void refreshView() {
      List<ClickGuiScreen.Module> category = this.modules.getOrDefault(this.activeCategory, List.of());
      this.filterLabels = ModuleFilters.pills(category);
      List<ClickGuiScreen.Module> result = new ArrayList<>();
      if (this.searching()) {
         String q = this.searchText.trim().toLowerCase(Locale.ROOT);

         for (String cat : CATEGORIES) {
            for (ClickGuiScreen.Module m : this.modules.getOrDefault(cat, List.of())) {
               if (ModuleFilters.search(m, q)) {
                  result.add(m);
               }
            }
         }
      } else {
         for (ClickGuiScreen.Module mx : category) {
            if (ModuleFilters.matches(mx, this.filterLabels, this.activeFilter)) {
               result.add(mx);
            }
         }
      }

      this.visible = List.copyOf(result);
      this.headingCount = this.visible.size() + " modules";
      this.maxScroll = Math.max(0, (this.visible.size() + 1) / 2 * 52 - 8 - 304);
      this.scrollOffset = Math.max(0.0, Math.min(this.scrollOffset, (double)this.maxScroll));
      this.targetScrollOffset = Math.max(0.0, Math.min(this.targetScrollOffset, (double)this.maxScroll));
      this.textDirty = true;
      this.hoverModule = null;

      for (String cat : CATEGORIES) {
         this.counts.put(cat, Integer.toString(this.modules.getOrDefault(cat, List.of()).size()));
      }

      if (this.lastContext != null) {
         this.prepareTexts(this.lastContext);
      }
   }

   private static float damp(float value, float target, float dt) {
      float next = value + (target - value) * (1.0F - (float)Math.exp(-18.0F * dt));
      return Math.abs(next - target) < 0.002F ? target : next;
   }

   private static boolean in(double mx, double my, int x, int y, int w, int h) {
      return mx >= x && mx < x + w && my >= y && my < y + h;
   }

   private boolean drawerBlocks(double x, double y) {
      return (this.selectedModule != null || this.drawerAnim > 0.0F) && this.layout != null && in(x, y, this.layout.drawerX(this.drawerAnim), 0, 224, 384);
   }

   private String ellipsis(GuiGraphicsContext c, String value, int width, boolean small) {
      int raw = small ? c.getTextWidthSmall(value) : c.getTextWidthMedium(value);
      if (raw <= width) {
         return value;
      } else {
         int ell = small ? c.getTextWidthSmall("…") : c.getTextWidthMedium("…");
         int end = value.length();

         while (end > 0 && (small ? c.getTextWidthSmall(value.substring(0, end)) : c.getTextWidthMedium(value.substring(0, end))) > width - ell) {
            end = value.offsetByCodePoints(end, -1);
         }

         return width >= ell ? value.substring(0, end) + "…" : "";
      }
   }

   private void prepareTexts(GuiGraphicsContext c) {
      for (List<ClickGuiScreen.Module> list : this.modules.values()) {
         for (ClickGuiScreen.Module m : list) {
            int key = m.getKey == null ? -1 : m.getKey.get();
            String k = key > 0 ? this.getKeyName(key) : "";
            int kw = k.isEmpty() ? 0 : c.getTextWidthSmall(k) + 10;
            int titleWidth = 172 - (kw == 0 ? 0 : kw + 8);
            String title = this.ellipsis(c, m.name, titleWidth, false);
            String desc = this.ellipsis(c, m.description, 172, true);
            this.texts
               .put(
                  m,
                  new ClickGuiScreen.TextCache(
                     title, desc, !desc.equals(m.description) || !title.equals(m.name), c.wrapTextSmall(m.description, 232), key, k, kw
                  )
               );
            this.visuals.computeIfAbsent(m, ignored -> new ClickGuiScreen.Visual());
         }
      }

      if (!this.searching() && !this.activeCategory.equals("Configs") && this.filterLabels.size() > 2) {
         int available = 452 - c.getTextWidthHeading(this.activeCategory) - c.getTextWidthSmall(this.headingCount) - 28;
         int max = Math.max(20, (available - 6) / this.filterLabels.size());
         List<ClickGuiScreen.Pill> pills = new ArrayList<>();
         int width = 6;

         for (String label : this.filterLabels) {
            width += Math.min(max, c.getTextWidthSmall(label) + 16);
         }

         int x = 628 - width + 3;

         for (String label : this.filterLabels) {
            int w = Math.min(max, c.getTextWidthSmall(label) + 16);
            pills.add(new ClickGuiScreen.Pill(label, this.ellipsis(c, label, w - 12, true), x, w));
            x += w;
         }

         this.filterPills = List.copyOf(pills);
      } else {
         this.filterPills = List.of();
      }

      this.textDirty = false;
   }

   private void initModules() {
      MacroConfig cfg = MacroConfig.get();
      if (cfg != null) {
         List<ClickGuiScreen.Module> cpvpList = new ArrayList<>();
         ClickGuiScreen.Module zCrystal = new ClickGuiScreen.Module("Crystal Macro", "Places and breaks end crystals.", () -> cfg.crystalEnabled, v -> {
            cfg.crystalEnabled = v;
            cfg.save();
         });
         zCrystal.addSetting(new ClickGuiScreen.Setting("Activation Key", ClickGuiScreen.SettingType.KEYBIND, () -> cfg.crystalActivateKey, v -> {
            cfg.crystalActivateKey = (Integer)v;
            cfg.save();
         }));
         zCrystal.addSetting(new ClickGuiScreen.Setting("Key Mode", ClickGuiScreen.SettingType.MODE, () -> cfg.crystalKeyMode, v -> {
            cfg.crystalKeyMode = (Integer)v;
            cfg.save();
         }, new String[]{"Use", "Cust", "Both"}));
         zCrystal.addSetting(new ClickGuiScreen.Setting("Crystals / Sec (CPS)", ClickGuiScreen.SettingType.NUMBER, () -> cfg.crystalMaxCps, v -> {
            cfg.crystalMaxCps = (Integer)v;
            cfg.crystalMinCps = Math.min(cfg.crystalMinCps, (Integer)v);
            cfg.save();
         }, 1, 50));
         zCrystal.addSetting(new ClickGuiScreen.Setting("Place Delay (ticks)", ClickGuiScreen.SettingType.NUMBER, () -> cfg.crystalPlaceDelay, v -> {
            cfg.crystalPlaceDelay = (Integer)v;
            cfg.save();
         }, 0, 10));
         zCrystal.addSetting(new ClickGuiScreen.Setting("Break Delay (ticks)", ClickGuiScreen.SettingType.NUMBER, () -> cfg.crystalBreakDelay, v -> {
            cfg.crystalBreakDelay = (Integer)v;
            cfg.save();
         }, 0, 10));
         zCrystal.addSetting(new ClickGuiScreen.Setting("Place Obsidian", ClickGuiScreen.SettingType.BOOLEAN, () -> cfg.crystalPlaceObsidian, v -> {
            cfg.crystalPlaceObsidian = (Boolean)v;
            cfg.save();
         }));
         zCrystal.addSetting(
            new ClickGuiScreen.Setting("Obsidian Switch Delay", ClickGuiScreen.SettingType.NUMBER, () -> cfg.crystalObsidianSwitchDelay, v -> {
               cfg.crystalObsidianSwitchDelay = (Integer)v;
               cfg.save();
            }, 0, 20)
         );
         zCrystal.addSetting(
            new ClickGuiScreen.Setting("Obsidian Cooldown (s)", ClickGuiScreen.SettingType.NUMBER, () -> (int)cfg.crystalObsidianCooldown, v -> {
               cfg.crystalObsidianCooldown = ((Number)v).doubleValue();
               cfg.save();
            }, 0, 10)
         );
         zCrystal.addSetting(new ClickGuiScreen.Setting("Self Safe (PRO)", ClickGuiScreen.SettingType.BOOLEAN, () -> cfg.proCrystalSelfSafe, v -> {
            cfg.proCrystalSelfSafe = (Boolean)v;
            cfg.save();
         }));
         zCrystal.addSetting(new ClickGuiScreen.Setting("Min Self Distance (PRO)", ClickGuiScreen.SettingType.NUMBER, () -> (int)Math.round(cfg.proCrystalMinSelfDistance * 10.0), v -> {
            cfg.proCrystalMinSelfDistance = (double)(Integer)v / 10.0;
            cfg.save();
         }, 0, 80));
         cpvpList.add(zCrystal);
         ClickGuiScreen.Module zAnchor = new ClickGuiScreen.Module("Anchor Macro", "Charges and detonates respawn anchors.", () -> cfg.anchorEnabled, v -> {
            cfg.anchorEnabled = v;
            cfg.save();
         });
         zAnchor.addSetting(new ClickGuiScreen.Setting("Key Mode", ClickGuiScreen.SettingType.MODE, () -> cfg.anchorKeyMode, v -> {
            cfg.anchorKeyMode = (Integer)v;
            cfg.save();
         }, new String[]{"Use", "Cust", "Both"}));
         zAnchor.addSetting(new ClickGuiScreen.Setting("Activation Key", ClickGuiScreen.SettingType.KEYBIND, () -> cfg.anchorActivateKey, v -> {
            cfg.anchorActivateKey = (Integer)v;
            cfg.save();
         }));
         zAnchor.addSetting(new ClickGuiScreen.Setting("Charges", ClickGuiScreen.SettingType.NUMBER, () -> cfg.anchorCharges, v -> {
            cfg.anchorCharges = (Integer)v;
            cfg.save();
         }, 1, 4));
         zAnchor.addSetting(new ClickGuiScreen.Setting("Explode With", ClickGuiScreen.SettingType.MODE, () -> cfg.anchorExplodeWith, v -> {
            cfg.anchorExplodeWith = (Integer)v;
            cfg.save();
         }, new String[]{"Totem", "Glowstone", "Current"}));
         zAnchor.addSetting(new ClickGuiScreen.Setting("Explode Slot", ClickGuiScreen.SettingType.NUMBER, () -> cfg.anchorExplodeSlot, v -> {
            cfg.anchorExplodeSlot = (Integer)v;
            cfg.save();
         }, 0, 9));
         zAnchor.addSetting(new ClickGuiScreen.Setting("Swap Delay (ms)", ClickGuiScreen.SettingType.NUMBER, () -> cfg.anchorSwapDelay, v -> {
            cfg.anchorSwapDelay = (Integer)v;
            cfg.save();
         }, 0, 300));
         zAnchor.addSetting(new ClickGuiScreen.Setting("Charge Delay (ms)", ClickGuiScreen.SettingType.NUMBER, () -> cfg.anchorChargeDelay, v -> {
            cfg.anchorChargeDelay = (Integer)v;
            cfg.save();
         }, 0, 300));
         zAnchor.addSetting(new ClickGuiScreen.Setting("Explode Delay (ms)", ClickGuiScreen.SettingType.NUMBER, () -> cfg.anchorExplodeDelay, v -> {
            cfg.anchorExplodeDelay = (Integer)v;
            cfg.save();
         }, 0, 300));
         zAnchor.addSetting(new ClickGuiScreen.Setting("Only Charge", ClickGuiScreen.SettingType.BOOLEAN, () -> cfg.anchorOnlyCharge, v -> {
            cfg.anchorOnlyCharge = (Boolean)v;
            cfg.save();
         }));
         zAnchor.addSetting(new ClickGuiScreen.Setting("Switch Back", ClickGuiScreen.SettingType.BOOLEAN, () -> cfg.anchorSwitchBack, v -> {
            cfg.anchorSwitchBack = (Boolean)v;
            cfg.save();
         }));
         cpvpList.add(zAnchor);
         ClickGuiScreen.Module safeAnchor = new ClickGuiScreen.Module(
            "Safe Anchor", "Places and shields anchors; safe detonation requires stable footing and survivable damage.", () -> cfg.safeAnchorEnabled, v -> {
               cfg.safeAnchorEnabled = v;
               cfg.save();
            }
         );
         safeAnchor.addSetting(new ClickGuiScreen.Setting("Auto Target", ClickGuiScreen.SettingType.BOOLEAN, () -> cfg.safeAnchorAutoTarget, v -> {
            cfg.safeAnchorAutoTarget = (Boolean)v;
            cfg.save();
         }));
         safeAnchor.addSetting(new ClickGuiScreen.Setting("Target Mode", ClickGuiScreen.SettingType.MODE, () -> cfg.safeAnchorTargetMode, v -> {
            cfg.safeAnchorTargetMode = (Integer)v;
            cfg.save();
         }, new String[]{"FOV", "Health", "Distance"}));
         safeAnchor.addSetting(
            new ClickGuiScreen.Setting("Target Range", ClickGuiScreen.SettingType.NUMBER, () -> (int)(cfg.safeAnchorTargetRange * 2.0), v -> {
               cfg.safeAnchorTargetRange = ((Number)v).doubleValue() / 2.0;
               cfg.save();
            }, 4, 30, 0.5)
         );
         safeAnchor.addSetting(new ClickGuiScreen.Setting("FOV", ClickGuiScreen.SettingType.NUMBER, () -> (int)(cfg.safeAnchorFov / 5.0), v -> {
            cfg.safeAnchorFov = ((Number)v).doubleValue() * 5.0;
            cfg.save();
         }, 2, 36, 5.0));
         safeAnchor.addSetting(new ClickGuiScreen.Setting("Chance", ClickGuiScreen.SettingType.NUMBER, () -> (int)cfg.safeAnchorChance, v -> {
            cfg.safeAnchorChance = ((Number)v).doubleValue();
            cfg.save();
         }, 0, 100));
         safeAnchor.addSetting(new ClickGuiScreen.Setting("Rotation Mode", ClickGuiScreen.SettingType.MODE, () -> cfg.safeAnchorRotationMode, v -> {
            cfg.safeAnchorRotationMode = (Integer)v;
            cfg.save();
         }, new String[]{"Silent", "Legit"}));
         safeAnchor.addSetting(new ClickGuiScreen.Setting("Strength", ClickGuiScreen.SettingType.NUMBER, () -> (int)cfg.safeAnchorStrength, v -> {
            cfg.safeAnchorStrength = ((Number)v).doubleValue();
            cfg.save();
         }, 1, 100));
         safeAnchor.addSetting(new ClickGuiScreen.Setting("Raycast", ClickGuiScreen.SettingType.BOOLEAN, () -> cfg.safeAnchorRaycast, v -> {
            cfg.safeAnchorRaycast = (Boolean)v;
            cfg.save();
         }));
         safeAnchor.addSetting(new ClickGuiScreen.Setting("Charge Glowstone", ClickGuiScreen.SettingType.BOOLEAN, () -> cfg.safeAnchorChargeGlowstone, v -> {
            cfg.safeAnchorChargeGlowstone = (Boolean)v;
            cfg.save();
         }));
         safeAnchor.addSetting(new ClickGuiScreen.Setting("Charges", ClickGuiScreen.SettingType.NUMBER, () -> cfg.safeAnchorCharges, v -> {
            cfg.safeAnchorCharges = (Integer)v;
            cfg.save();
         }, 1, 4));
         safeAnchor.addSetting(new ClickGuiScreen.Setting("Auto Explode", ClickGuiScreen.SettingType.BOOLEAN, () -> cfg.safeAnchorAutoExplode, v -> {
            cfg.safeAnchorAutoExplode = (Boolean)v;
            cfg.save();
         }));
         safeAnchor.addSetting(new ClickGuiScreen.Setting("Explode unsafe", ClickGuiScreen.SettingType.BOOLEAN, () -> cfg.safeAnchorExplodeUnsafe, v -> {
            cfg.safeAnchorExplodeUnsafe = (Boolean)v;
            cfg.save();
         }));
         safeAnchor.addSetting(
            new ClickGuiScreen.Setting("Enforce Min Damage", ClickGuiScreen.SettingType.BOOLEAN, () -> cfg.safeAnchorEnforceMinDamage, v -> {
               cfg.safeAnchorEnforceMinDamage = (Boolean)v;
               cfg.save();
            })
         );
         safeAnchor.addSetting(new ClickGuiScreen.Setting("Min Damage", ClickGuiScreen.SettingType.NUMBER, () -> (int)(cfg.safeAnchorMinDamage * 2.0), v -> {
            cfg.safeAnchorMinDamage = ((Number)v).doubleValue() / 2.0;
            cfg.save();
         }, 1, 40, 0.5));
         safeAnchor.addSetting(new ClickGuiScreen.Setting("Safe Block", ClickGuiScreen.SettingType.MODE, () -> cfg.safeAnchorSafeBlock, v -> {
            cfg.safeAnchorSafeBlock = (Integer)v;
            cfg.save();
         }, new String[]{"Glowstone", "Obsidian", "Random"}));
         safeAnchor.addSetting(new ClickGuiScreen.Setting("Safety Placement", ClickGuiScreen.SettingType.MODE, () -> cfg.safeAnchorSafetyPlacement, v -> {
            cfg.safeAnchorSafetyPlacement = (Integer)v;
            cfg.save();
         }, new String[]{"Adaptive", "Adjacent"}));
         safeAnchor.addSetting(new ClickGuiScreen.Setting("Swap Delay Min (ms)", ClickGuiScreen.SettingType.NUMBER, () -> cfg.safeAnchorSwapDelayMin, v -> {
            cfg.safeAnchorSwapDelayMin = (Integer)v;
            cfg.save();
         }, 0, 300));
         safeAnchor.addSetting(new ClickGuiScreen.Setting("Swap Delay Max (ms)", ClickGuiScreen.SettingType.NUMBER, () -> cfg.safeAnchorSwapDelayMax, v -> {
            cfg.safeAnchorSwapDelayMax = (Integer)v;
            cfg.save();
         }, 0, 300));
         safeAnchor.addSetting(new ClickGuiScreen.Setting("Place Delay Min (ms)", ClickGuiScreen.SettingType.NUMBER, () -> cfg.safeAnchorPlaceDelayMin, v -> {
            cfg.safeAnchorPlaceDelayMin = (Integer)v;
            cfg.save();
         }, 0, 500));
         safeAnchor.addSetting(new ClickGuiScreen.Setting("Place Delay Max (ms)", ClickGuiScreen.SettingType.NUMBER, () -> cfg.safeAnchorPlaceDelayMax, v -> {
            cfg.safeAnchorPlaceDelayMax = (Integer)v;
            cfg.save();
         }, 0, 500));
         safeAnchor.addSetting(new ClickGuiScreen.Setting("Switch Back", ClickGuiScreen.SettingType.BOOLEAN, () -> cfg.safeAnchorSwitchBack, v -> {
            cfg.safeAnchorSwitchBack = (Boolean)v;
            cfg.save();
         }));
         cpvpList.add(safeAnchor);
         ClickGuiScreen.Module dblAnchor = new ClickGuiScreen.Module("Double Anchor", "Places two anchors in sequence.", () -> cfg.doubleAnchorEnabled, v -> {
            cfg.doubleAnchorEnabled = v;
            cfg.save();
         });
         dblAnchor.addSetting(new ClickGuiScreen.Setting("Switch Delay", ClickGuiScreen.SettingType.NUMBER, () -> cfg.doubleAnchorSwitchDelay, v -> {
            cfg.doubleAnchorSwitchDelay = (Integer)v;
            cfg.save();
         }, 0, 20));
         dblAnchor.addSetting(new ClickGuiScreen.Setting("Totem Slot", ClickGuiScreen.SettingType.NUMBER, () -> cfg.doubleAnchorTotemSlot, v -> {
            cfg.doubleAnchorTotemSlot = (Integer)v;
            cfg.save();
         }, 1, 9));
         cpvpList.add(dblAnchor);
         ClickGuiScreen.Module zHover = new ClickGuiScreen.Module(
            "Hover Totem", "Moves a hovered totem to offhand or hotbar.", () -> cfg.hoverTotemEnabled, v -> {
               cfg.hoverTotemEnabled = v;
               cfg.save();
            }
         );
         zHover.addSetting(new ClickGuiScreen.Setting("Delay (ms)", ClickGuiScreen.SettingType.NUMBER, () -> cfg.hoverTotemDelayMs, v -> {
            cfg.hoverTotemDelayMs = (Integer)v;
            cfg.save();
         }, 0, 500));
         zHover.addSetting(new ClickGuiScreen.Setting("Hotbar Refill", ClickGuiScreen.SettingType.BOOLEAN, () -> cfg.hoverTotemHotbar, v -> {
            cfg.hoverTotemHotbar = (Boolean)v;
            cfg.save();
         }));
         zHover.addSetting(new ClickGuiScreen.Setting("Refill Slot", ClickGuiScreen.SettingType.NUMBER, () -> cfg.hoverTotemHotbarSlot, v -> {
            cfg.hoverTotemHotbarSlot = (Integer)v;
            cfg.save();
         }, 1, 9));
         cpvpList.add(zHover);
         ClickGuiScreen.Module tweaks = new ClickGuiScreen.Module(
            "Crystal Tweaks", "Adjusts crystal timing and cooldown.", () -> cfg.crystalTweaksEnabled, v -> {
               cfg.crystalTweaksEnabled = v;
               cfg.save();
            }
         );
         tweaks.addSetting(new ClickGuiScreen.Setting("Zero Delay", ClickGuiScreen.SettingType.BOOLEAN, () -> cfg.crystalTweaksZeroDelay, v -> {
            cfg.crystalTweaksZeroDelay = (Boolean)v;
            cfg.save();
         }));
         cpvpList.add(tweaks);
         ClickGuiScreen.Module noInteract = new ClickGuiScreen.Module(
            "NoInteract", "Prevents opening chests and storage blocks.", () -> cfg.noInteractEnabled, v -> {
               cfg.noInteractEnabled = v;
               cfg.save();
            }
         );
         // ================= PRO 3.6.2 modulleri =================
         ClickGuiScreen.Module proTotem = new ClickGuiScreen.Module("Auto Totem Pro",
            "Can esigin altina dusunce envanterdeki totemi offhand'e takar.", () -> cfg.proAutoTotemEnabled, v -> {
               cfg.proAutoTotemEnabled = v;
               cfg.save();
            });
         proTotem.addSetting(new ClickGuiScreen.Setting("Health Threshold", ClickGuiScreen.SettingType.NUMBER, () -> cfg.proAutoTotemHealth, v -> {
            cfg.proAutoTotemHealth = (Integer)v;
            cfg.save();
         }, 1, 20));
         proTotem.addSetting(new ClickGuiScreen.Setting("Swap Delay (ticks)", ClickGuiScreen.SettingType.NUMBER, () -> cfg.proAutoTotemDelay, v -> {
            cfg.proAutoTotemDelay = (Integer)v;
            cfg.save();
         }, 0, 20));
         cpvpList.add(proTotem);

         ClickGuiScreen.Module proShield = new ClickGuiScreen.Module("Shield Swap Pro",
            "Can dusukken kalkani offhand'e alip kaldirir, can gelince indirir.", () -> cfg.proShieldSwapEnabled, v -> {
               cfg.proShieldSwapEnabled = v;
               cfg.save();
            });
         proShield.addSetting(new ClickGuiScreen.Setting("Health Threshold", ClickGuiScreen.SettingType.NUMBER, () -> cfg.proShieldHealth, v -> {
            cfg.proShieldHealth = (Integer)v;
            cfg.save();
         }, 1, 20));
         cpvpList.add(proShield);

         ClickGuiScreen.Module proAnchorAura = new ClickGuiScreen.Module("Anchor Aura Pro",
            "Cevredeki respawn anchor'larini otomatik sarj eder ve guvenliyse patlatir.", () -> cfg.proAnchorAuraEnabled, v -> {
               cfg.proAnchorAuraEnabled = v;
               cfg.save();
            });
         proAnchorAura.addSetting(new ClickGuiScreen.Setting("Radius", ClickGuiScreen.SettingType.NUMBER, () -> cfg.proAnchorAuraRange, v -> {
            cfg.proAnchorAuraRange = (Integer)v;
            cfg.save();
         }, 1, 6));
         proAnchorAura.addSetting(new ClickGuiScreen.Setting("Delay (ticks)", ClickGuiScreen.SettingType.NUMBER, () -> cfg.proAnchorAuraDelay, v -> {
            cfg.proAnchorAuraDelay = (Integer)v;
            cfg.save();
         }, 0, 20));
         proAnchorAura.addSetting(new ClickGuiScreen.Setting("Safe Distance", ClickGuiScreen.SettingType.NUMBER, () -> cfg.proAnchorAuraSafeDistance, v -> {
            cfg.proAnchorAuraSafeDistance = (Integer)v;
            cfg.save();
         }, 0, 8));
         cpvpList.add(proAnchorAura);
         // =============== PRO 3.6.2 modulleri sonu ===============
         cpvpList.add(noInteract);
         zCrystal.subcategory = tweaks.subcategory = "Crystal";
         zAnchor.subcategory = safeAnchor.subcategory = dblAnchor.subcategory = "Anchor";
         zHover.subcategory = "Totem";
         this.modules.put("CPvP", cpvpList);
         List<ClickGuiScreen.Module> uhcList = new ArrayList<>();
         ClickGuiScreen.Module cobweb = new ClickGuiScreen.Module(
            "Cobweb Placer", "Places cobwebs below feet to trap approaching opponents.", () -> cfg.cobwebPlaceEnabled, v -> {
               cfg.cobwebPlaceEnabled = v;
               cfg.save();
            }
         );
         cobweb.addSetting(new ClickGuiScreen.Setting("Web Delay (ms)", ClickGuiScreen.SettingType.NUMBER, () -> cfg.cobwebPlaceDelay, v -> {
            cfg.cobwebPlaceDelay = (Integer)v;
            cfg.save();
         }, 0, 500));
         cobweb.addSetting(new ClickGuiScreen.Setting("Return Slot", ClickGuiScreen.SettingType.NUMBER, () -> cfg.cobwebReturnSlot, v -> {
            cfg.cobwebReturnSlot = (Integer)v;
            cfg.save();
         }, 1, 9));
         uhcList.add(cobweb);
         ClickGuiScreen.Module plateWeb = new ClickGuiScreen.Module(
            "Pressure Web", "Places a cobweb directly above pressure plates upon placement.", () -> cfg.plateWebEnabled, v -> {
               cfg.plateWebEnabled = v;
               cfg.save();
            }
         );
         plateWeb.addSetting(new ClickGuiScreen.Setting("Delay (ms)", ClickGuiScreen.SettingType.NUMBER, () -> cfg.plateWebDelay, v -> {
            cfg.plateWebDelay = (Integer)v;
            cfg.save();
         }, 0, 500));
         plateWeb.addSetting(new ClickGuiScreen.Setting("Return Slot", ClickGuiScreen.SettingType.NUMBER, () -> cfg.plateWebReturnSlot, v -> {
            cfg.plateWebReturnSlot = (Integer)v;
            cfg.save();
         }, 0, 9));
         uhcList.add(plateWeb);
         ClickGuiScreen.Module sTab = new ClickGuiScreen.Module(
            "STab Sprint Reset", "Resets sprint state on hit (S-Tap) to maximize knockback dealt to opponents.", () -> cfg.sTabSprintResetEnabled, v -> {
               cfg.sTabSprintResetEnabled = v;
               cfg.save();
            }
         );
         uhcList.add(sTab);
         ClickGuiScreen.Module zTapReset = new ClickGuiScreen.Module(
            "Tap Reset", "Hit sprint reset macro. Performs S-Tap or W-Tap timing upon hitting opponents.", () -> cfg.tapResetEnabled, v -> {
               cfg.tapResetEnabled = v;
               cfg.save();
            }
         );
         zTapReset.addSetting(new ClickGuiScreen.Setting("Tap Mode", ClickGuiScreen.SettingType.MODE, () -> cfg.tapResetWTap ? 1 : 0, v -> {
            int m = (Integer)v;
            cfg.tapResetSTap = m == 0;
            cfg.tapResetWTap = m == 1;
            cfg.save();
         }, new String[]{"S-Tap", "W-Tap"}));
         zTapReset.addSetting(new ClickGuiScreen.Setting("Tap Duration (ms)", ClickGuiScreen.SettingType.NUMBER, () -> cfg.tapResetDuration, v -> {
            cfg.tapResetDuration = (Integer)v;
            cfg.save();
         }, 20, 500));
         uhcList.add(zTapReset);
         ClickGuiScreen.Module zAim = new ClickGuiScreen.Module(
            "Aim Assist", "Smooth target aim assist. Gently locks crosshair onto nearby enemies.", () -> cfg.aimAssistEnabled, v -> {
               cfg.aimAssistEnabled = v;
               cfg.save();
            }
         );
         zAim.addSetting(new ClickGuiScreen.Setting("Target Mode", ClickGuiScreen.SettingType.MODE, () -> cfg.aimAssistTargetMode, v -> {
            cfg.aimAssistTargetMode = (Integer)v;
            cfg.save();
         }, new String[]{"Players", "Mobs", "All"}));
         zAim.addSetting(new ClickGuiScreen.Setting("Bone Mode", ClickGuiScreen.SettingType.MODE, () -> cfg.aimAssistBoneMode, v -> {
            cfg.aimAssistBoneMode = (Integer)v;
            cfg.save();
         }, new String[]{"Closest", "Head", "Chest"}));
         zAim.addSetting(new ClickGuiScreen.Setting("Range", ClickGuiScreen.SettingType.NUMBER, () -> (int)(cfg.aimAssistRange * 10.0), v -> {
            cfg.aimAssistRange = ((Number)v).doubleValue() / 10.0;
            cfg.save();
         }, 20, 120));
         zAim.addSetting(new ClickGuiScreen.Setting("FOV", ClickGuiScreen.SettingType.NUMBER, () -> (int)cfg.aimAssistFov, v -> {
            cfg.aimAssistFov = ((Number)v).doubleValue();
            cfg.save();
         }, 1, 35));
         zAim.addSetting(new ClickGuiScreen.Setting("Smoothing", ClickGuiScreen.SettingType.NUMBER, () -> (int)(cfg.aimAssistSmoothingSpeed * 100.0), v -> {
            cfg.aimAssistSmoothingSpeed = ((Number)v).doubleValue() / 100.0;
            cfg.save();
         }, 10, 100));
         zAim.addSetting(new ClickGuiScreen.Setting("GCD Smoothing", ClickGuiScreen.SettingType.BOOLEAN, () -> cfg.aimAssistGcdSmoothing, v -> {
            cfg.aimAssistGcdSmoothing = (Boolean)v;
            cfg.save();
         }));
         zAim.addSetting(
            new ClickGuiScreen.Setting("Tracking Scale", ClickGuiScreen.SettingType.NUMBER, () -> (int)(cfg.aimAssistTrackingScale * 100.0), v -> {
               cfg.aimAssistTrackingScale = ((Number)v).doubleValue() / 100.0;
               cfg.save();
            }, 50, 150)
         );
         zAim.addSetting(new ClickGuiScreen.Setting("Pitch Weight", ClickGuiScreen.SettingType.NUMBER, () -> (int)(cfg.aimAssistPitchWeight * 100.0), v -> {
            cfg.aimAssistPitchWeight = ((Number)v).doubleValue() / 100.0;
            cfg.save();
         }, 50, 150));
         zAim.addSetting(new ClickGuiScreen.Setting("Deadzone", ClickGuiScreen.SettingType.NUMBER, () -> (int)(cfg.aimAssistDeadzone * 100.0), v -> {
            cfg.aimAssistDeadzone = ((Number)v).doubleValue() / 100.0;
            cfg.save();
         }, 0, 50));
         zAim.addSetting(
            new ClickGuiScreen.Setting("Micro Adjust", ClickGuiScreen.SettingType.NUMBER, () -> (int)(cfg.aimAssistMicroAdjustment * 100.0), v -> {
               cfg.aimAssistMicroAdjustment = ((Number)v).doubleValue() / 100.0;
               cfg.save();
            }, 5, 60)
         );
         zAim.addSetting(new ClickGuiScreen.Setting("Only While Moving", ClickGuiScreen.SettingType.BOOLEAN, () -> cfg.aimAssistOnlyWhileMoving, v -> {
            cfg.aimAssistOnlyWhileMoving = (Boolean)v;
            cfg.save();
         }));
         uhcList.add(zAim);
         ClickGuiScreen.Module zTrigger = new ClickGuiScreen.Module(
            "TriggerBot", "Triggerbot. Automatically attacks targets when crosshair hovers over enemy player.", () -> cfg.triggerBotEnabled, v -> {
               cfg.triggerBotEnabled = v;
               cfg.save();
            }
         );
         zTrigger.addSetting(new ClickGuiScreen.Setting("Min CPS", ClickGuiScreen.SettingType.NUMBER, () -> cfg.triggerBotMinCps, v -> {
            cfg.triggerBotMinCps = (Integer)v;
            cfg.save();
         }, 1, 20));
         zTrigger.addSetting(new ClickGuiScreen.Setting("Max CPS", ClickGuiScreen.SettingType.NUMBER, () -> cfg.triggerBotMaxCps, v -> {
            cfg.triggerBotMaxCps = (Integer)v;
            cfg.save();
         }, 1, 20));
         zTrigger.addSetting(new ClickGuiScreen.Setting("Attack Delay", ClickGuiScreen.SettingType.NUMBER, () -> cfg.triggerBotDelay, v -> {
            cfg.triggerBotDelay = (Integer)v;
            cfg.save();
         }, 0, 1000));
         zTrigger.addSetting(new ClickGuiScreen.Setting("Random Delay", ClickGuiScreen.SettingType.NUMBER, () -> cfg.triggerBotRandomDelay, v -> {
            cfg.triggerBotRandomDelay = (Integer)v;
            cfg.save();
         }, 0, 500));
         zTrigger.addSetting(
            new ClickGuiScreen.Setting("Cooldown Threshold", ClickGuiScreen.SettingType.NUMBER, () -> (int)(cfg.triggerBotCooldownThreshold * 100.0F), v -> {
               cfg.triggerBotCooldownThreshold = ((Number)v).floatValue() / 100.0F;
               cfg.save();
            }, 50, 100)
         );
         zTrigger.addSetting(new ClickGuiScreen.Setting("Hit Chance (%)", ClickGuiScreen.SettingType.NUMBER, () -> cfg.triggerBotHitChance, v -> {
            cfg.triggerBotHitChance = (Integer)v;
            cfg.save();
         }, 1, 100));
         zTrigger.addSetting(new ClickGuiScreen.Setting("Crits Only", ClickGuiScreen.SettingType.BOOLEAN, () -> cfg.triggerBotCritOnly, v -> {
            cfg.triggerBotCritOnly = (Boolean)v;
            cfg.save();
         }));
         zTrigger.addSetting(new ClickGuiScreen.Setting("Weapons Only", ClickGuiScreen.SettingType.BOOLEAN, () -> cfg.triggerBotWeaponsOnly, v -> {
            cfg.triggerBotWeaponsOnly = (Boolean)v;
            cfg.save();
         }));
         zTrigger.addSetting(new ClickGuiScreen.Setting("Reach", ClickGuiScreen.SettingType.NUMBER, () -> (int)(cfg.triggerBotReach * 10.0), v -> {
            cfg.triggerBotReach = ((Number)v).doubleValue() / 10.0;
            cfg.save();
         }, 20, 60));
         zTrigger.addSetting(new ClickGuiScreen.Setting("Players Only", ClickGuiScreen.SettingType.BOOLEAN, () -> cfg.triggerBotPlayersOnly, v -> {
            cfg.triggerBotPlayersOnly = (Boolean)v;
            cfg.save();
         }));
         uhcList.add(zTrigger);
         this.modules.put("UHC", uhcList);
         List<ClickGuiScreen.Module> maceList = new ArrayList<>();
         ClickGuiScreen.Module zKeyPearl = new ClickGuiScreen.Module(
            "Key Pearl", "Instant key pearl. Throws Ender Pearls instantly when hotkey is pressed.", () -> cfg.keyPearlEnabled, v -> {
               cfg.keyPearlEnabled = v;
               cfg.save();
            }
         );
         zKeyPearl.addSetting(new ClickGuiScreen.Setting("Keybind", ClickGuiScreen.SettingType.KEYBIND, () -> cfg.keyPearlKey, v -> {
            cfg.keyPearlKey = (Integer)v;
            cfg.save();
         }));
         zKeyPearl.addSetting(new ClickGuiScreen.Setting("Throw Delay", ClickGuiScreen.SettingType.NUMBER, () -> cfg.keyPearlDelay, v -> {
            cfg.keyPearlDelay = (Integer)v;
            cfg.save();
         }, 0, 1000));
         zKeyPearl.addSetting(new ClickGuiScreen.Setting("Switch Back", ClickGuiScreen.SettingType.BOOLEAN, () -> cfg.keyPearlSwitchBack, v -> {
            cfg.keyPearlSwitchBack = (Boolean)v;
            cfg.save();
         }));
         maceList.add(zKeyPearl);
         ClickGuiScreen.Module pearlCatch = new ClickGuiScreen.Module(
            "Pearl Catch", "Intercepts thrown Ender Pearls by tracking trajectory and counter-throwing pearl.", () -> cfg.sattelaPearlCatchEnabled, v -> {
               cfg.sattelaPearlCatchEnabled = v;
               cfg.save();
            }
         );
         pearlCatch.addSetting(new ClickGuiScreen.Setting("Auto Trigger", ClickGuiScreen.SettingType.BOOLEAN, () -> !cfg.sattelaPearlCatchUseKeybind, v -> {
            cfg.sattelaPearlCatchUseKeybind = !(Boolean)v;
            cfg.save();
         }));
         pearlCatch.addSetting(new ClickGuiScreen.Setting("Keybind", ClickGuiScreen.SettingType.KEYBIND, () -> cfg.sattelaPearlCatchKey, v -> {
            cfg.sattelaPearlCatchKey = (Integer)v;
            cfg.save();
         }));
         pearlCatch.addSetting(new ClickGuiScreen.Setting("Combo Delay (ms)", ClickGuiScreen.SettingType.NUMBER, () -> cfg.sattelaPearlCatchDelay, v -> {
            cfg.sattelaPearlCatchDelay = (Integer)v;
            cfg.save();
         }, 0, 500));
         pearlCatch.addSetting(new ClickGuiScreen.Setting("Search Range", ClickGuiScreen.SettingType.NUMBER, () -> (int)cfg.pearlCatchSearchRange, v -> {
            cfg.pearlCatchSearchRange = ((Number)v).doubleValue();
            cfg.save();
         }, 10, 100));
         pearlCatch.addSetting(new ClickGuiScreen.Setting("Health Check", ClickGuiScreen.SettingType.BOOLEAN, () -> cfg.pearlCatchHealthCheck, v -> {
            cfg.pearlCatchHealthCheck = (Boolean)v;
            cfg.save();
         }));
         pearlCatch.addSetting(new ClickGuiScreen.Setting("Restore Rotation", ClickGuiScreen.SettingType.BOOLEAN, () -> cfg.pearlCatchRestoreRotation, v -> {
            cfg.pearlCatchRestoreRotation = (Boolean)v;
            cfg.save();
         }));
         maceList.add(pearlCatch);
         ClickGuiScreen.Module stunSlam = new ClickGuiScreen.Module(
            "Stun Slam", "Disables opponent's shield mid-air with Axe and hits with Mace (Sattela combo).", () -> cfg.sattelaStunSlamEnabled, v -> {
               cfg.sattelaStunSlamEnabled = v;
               cfg.save();
            }
         );
         stunSlam.addSetting(new ClickGuiScreen.Setting("Auto Trigger", ClickGuiScreen.SettingType.BOOLEAN, () -> cfg.sattelaStunSlamAuto, v -> {
            cfg.sattelaStunSlamAuto = (Boolean)v;
            cfg.save();
         }));
         stunSlam.addSetting(new ClickGuiScreen.Setting("Keybind", ClickGuiScreen.SettingType.KEYBIND, () -> cfg.sattelaStunSlamKey, v -> {
            cfg.sattelaStunSlamKey = (Integer)v;
            cfg.save();
         }));
         stunSlam.addSetting(new ClickGuiScreen.Setting("Combo Delay (ms)", ClickGuiScreen.SettingType.NUMBER, () -> cfg.sattelaStunSlamDelay, v -> {
            cfg.sattelaStunSlamDelay = (Integer)v;
            cfg.save();
         }, 0, 300));
         stunSlam.addSetting(new ClickGuiScreen.Setting("Reach", ClickGuiScreen.SettingType.NUMBER, () -> (int)(cfg.sattelaStunSlamReach * 10.0), v -> {
            cfg.sattelaStunSlamReach = ((Number)v).doubleValue() / 10.0;
            cfg.save();
         }, 20, 60));
         stunSlam.addSetting(new ClickGuiScreen.Setting("Shield Only", ClickGuiScreen.SettingType.BOOLEAN, () -> cfg.sattelaStunSlamShieldOnly, v -> {
            cfg.sattelaStunSlamShieldOnly = (Boolean)v;
            cfg.save();
         }));
         maceList.add(stunSlam);
         ClickGuiScreen.Module breachSwap = new ClickGuiScreen.Module(
            "Breach Swap",
            "Hits target with weapon and switches to Breach Mace in same tick for massive damage (Sattela combo).",
            () -> cfg.sattelaBreachSwapEnabled,
            v -> {
               cfg.sattelaBreachSwapEnabled = v;
               cfg.save();
            }
         );
         breachSwap.addSetting(new ClickGuiScreen.Setting("Keybind", ClickGuiScreen.SettingType.KEYBIND, () -> cfg.sattelaBreachSwapKey, v -> {
            cfg.sattelaBreachSwapKey = (Integer)v;
            cfg.save();
         }));
         breachSwap.addSetting(new ClickGuiScreen.Setting("Combo Delay (ms)", ClickGuiScreen.SettingType.NUMBER, () -> cfg.sattelaBreachSwapDelay, v -> {
            cfg.sattelaBreachSwapDelay = (Integer)v;
            cfg.save();
         }, 0, 500));
         maceList.add(breachSwap);
         ClickGuiScreen.Module maceSwap = new ClickGuiScreen.Module(
            "Mace Swap", "Automatically swaps to best Mace upon landing smash attacks or airborne hits.", () -> cfg.maceSwapEnabled, v -> {
               cfg.maceSwapEnabled = v;
               cfg.save();
            }
         );
         maceSwap.addSetting(new ClickGuiScreen.Setting("Keybind", ClickGuiScreen.SettingType.KEYBIND, () -> cfg.maceSwapKey, v -> {
            cfg.maceSwapKey = (Integer)v;
            cfg.save();
         }));
         maceSwap.addSetting(new ClickGuiScreen.Setting("Auto Trigger", ClickGuiScreen.SettingType.BOOLEAN, () -> cfg.maceSwapAuto, v -> {
            cfg.maceSwapAuto = (Boolean)v;
            cfg.save();
         }));
         maceSwap.addSetting(
            new ClickGuiScreen.Setting("Min Fall Distance", ClickGuiScreen.SettingType.NUMBER, () -> (int)(cfg.maceSwapMinFallDistance * 10.0), v -> {
               cfg.maceSwapMinFallDistance = ((Number)v).doubleValue() / 10.0;
               cfg.save();
            }, 5, 50)
         );
         maceSwap.addSetting(new ClickGuiScreen.Setting("Reach", ClickGuiScreen.SettingType.NUMBER, () -> (int)(cfg.maceSwapReach * 10.0), v -> {
            cfg.maceSwapReach = ((Number)v).doubleValue() / 10.0;
            cfg.save();
         }, 20, 60));
         maceSwap.addSetting(new ClickGuiScreen.Setting("Switch Delay (ms)", ClickGuiScreen.SettingType.NUMBER, () -> cfg.maceSwapSwitchDelay, v -> {
            cfg.maceSwapSwitchDelay = (Integer)v;
            cfg.save();
         }, 0, 500));
         maceSwap.addSetting(new ClickGuiScreen.Setting("Breach Priority", ClickGuiScreen.SettingType.BOOLEAN, () -> cfg.maceSwapBreachPriority, v -> {
            cfg.maceSwapBreachPriority = (Boolean)v;
            cfg.save();
         }));
         maceSwap.addSetting(new ClickGuiScreen.Setting("Shield Stun Slam", ClickGuiScreen.SettingType.BOOLEAN, () -> cfg.maceSwapStunSlam, v -> {
            cfg.maceSwapStunSlam = (Boolean)v;
            cfg.save();
         }));
         maceSwap.addSetting(new ClickGuiScreen.Setting("Swap Back", ClickGuiScreen.SettingType.BOOLEAN, () -> cfg.maceSwapSwitchBack, v -> {
            cfg.maceSwapSwitchBack = (Boolean)v;
            cfg.save();
         }));
         maceList.add(maceSwap);
         ClickGuiScreen.Module autoMace = new ClickGuiScreen.Module(
            "Auto Mace", "Detects airborne falling states and automatically executes smash attacks.", () -> cfg.autoMaceEnabled, v -> {
               cfg.autoMaceEnabled = v;
               cfg.save();
            }
         );
         autoMace.addSetting(
            new ClickGuiScreen.Setting("Min Fall Distance", ClickGuiScreen.SettingType.NUMBER, () -> (int)(cfg.autoMaceMinFallDistance * 10.0), v -> {
               cfg.autoMaceMinFallDistance = ((Number)v).doubleValue() / 10.0;
               cfg.save();
            }, 5, 50)
         );
         autoMace.addSetting(new ClickGuiScreen.Setting("Max Reach", ClickGuiScreen.SettingType.NUMBER, () -> (int)(cfg.autoMaceReach * 10.0), v -> {
            cfg.autoMaceReach = ((Number)v).doubleValue() / 10.0;
            cfg.save();
         }, 20, 60));
         autoMace.addSetting(new ClickGuiScreen.Setting("Max FOV", ClickGuiScreen.SettingType.NUMBER, () -> (int)cfg.autoMaceFov, v -> {
            cfg.autoMaceFov = ((Number)v).doubleValue();
            cfg.save();
         }, 10, 360));
         autoMace.addSetting(new ClickGuiScreen.Setting("Cooldown", ClickGuiScreen.SettingType.NUMBER, () -> cfg.autoMaceCooldown, v -> {
            cfg.autoMaceCooldown = (Integer)v;
            cfg.save();
         }, 0, 1000));
         autoMace.addSetting(new ClickGuiScreen.Setting("Switch Delay (ms)", ClickGuiScreen.SettingType.NUMBER, () -> cfg.autoMaceSwitchDelay, v -> {
            cfg.autoMaceSwitchDelay = (Integer)v;
            cfg.save();
         }, 0, 500));
         autoMace.addSetting(new ClickGuiScreen.Setting("Swap Back", ClickGuiScreen.SettingType.BOOLEAN, () -> cfg.autoMaceSwitchBack, v -> {
            cfg.autoMaceSwitchBack = (Boolean)v;
            cfg.save();
         }));
         maceList.add(autoMace);
         ClickGuiScreen.Module spearReach = new ClickGuiScreen.Module(
            "Spear Reach",
            "Swaps a held sword to a spear for sword attributes at spear range, then optionally swaps back.",
            () -> cfg.spearReachEnabled,
            v -> {
               cfg.spearReachEnabled = v;
               cfg.save();
            }
         );
         spearReach.addSetting(new ClickGuiScreen.Setting("Keybind", ClickGuiScreen.SettingType.KEYBIND, () -> cfg.spearReachKey, v -> {
            cfg.spearReachKey = (Integer)v;
            cfg.save();
         }));
         spearReach.addSetting(new ClickGuiScreen.Setting("Auto Trigger", ClickGuiScreen.SettingType.BOOLEAN, () -> cfg.spearReachAuto, v -> {
            cfg.spearReachAuto = (Boolean)v;
            cfg.save();
         }));
         spearReach.addSetting(new ClickGuiScreen.Setting("Max Reach", ClickGuiScreen.SettingType.NUMBER, () -> (int)(cfg.spearReachDistance * 10.0), v -> {
            cfg.spearReachDistance = ((Number)v).doubleValue() / 10.0;
            cfg.save();
         }, 20, 70));
         spearReach.addSetting(new ClickGuiScreen.Setting("Distance Mode", ClickGuiScreen.SettingType.MODE, () -> cfg.spearReachDistanceMode, v -> {
            cfg.spearReachDistanceMode = (Integer)v;
            cfg.save();
         }, new String[]{"2D", "3D", "Box"}));
         spearReach.addSetting(new ClickGuiScreen.Setting("Delay (ms)", ClickGuiScreen.SettingType.NUMBER, () -> cfg.spearReachDelay, v -> {
            cfg.spearReachDelay = (Integer)v;
            cfg.save();
         }, 0, 500));
         spearReach.addSetting(new ClickGuiScreen.Setting("Swap Back", ClickGuiScreen.SettingType.BOOLEAN, () -> cfg.spearReachSwitchBack, v -> {
            cfg.spearReachSwitchBack = (Boolean)v;
            cfg.save();
         }));
         maceList.add(spearReach);
         ClickGuiScreen.Module lungeMacro = new ClickGuiScreen.Module(
            "Lunge Macro", "Swaps to Spear, performs lunge attack, and automatically swaps back to previous weapon.", () -> cfg.lungeMacroEnabled, v -> {
               cfg.lungeMacroEnabled = v;
               cfg.save();
            }
         );
         lungeMacro.addSetting(new ClickGuiScreen.Setting("Keybind", ClickGuiScreen.SettingType.KEYBIND, () -> cfg.lungeMacroKey, v -> {
            cfg.lungeMacroKey = (Integer)v;
            cfg.save();
         }));
         lungeMacro.addSetting(new ClickGuiScreen.Setting("Continuous", ClickGuiScreen.SettingType.BOOLEAN, () -> cfg.lungeContinuous, v -> {
            cfg.lungeContinuous = (Boolean)v;
            cfg.save();
         }));
         lungeMacro.addSetting(new ClickGuiScreen.Setting("Auto Delay (ms)", ClickGuiScreen.SettingType.NUMBER, () -> cfg.lungeContinuousDelay, v -> {
            cfg.lungeContinuousDelay = (Integer)v;
            cfg.save();
         }, 0, 2000));
         lungeMacro.addSetting(new ClickGuiScreen.Setting("Swap Back", ClickGuiScreen.SettingType.BOOLEAN, () -> cfg.lungeSwapBack, v -> {
            cfg.lungeSwapBack = (Boolean)v;
            cfg.save();
         }));
         lungeMacro.addSetting(new ClickGuiScreen.Setting("Swap Delay", ClickGuiScreen.SettingType.NUMBER, () -> cfg.lungeSwapBackDelay, v -> {
            cfg.lungeSwapBackDelay = (Integer)v;
            cfg.save();
         }, 1, 10));
         lungeMacro.addSetting(new ClickGuiScreen.Setting("Randomize", ClickGuiScreen.SettingType.BOOLEAN, () -> cfg.lungeRandomize, v -> {
            cfg.lungeRandomize = (Boolean)v;
            cfg.save();
         }));
         maceList.add(lungeMacro);
         ClickGuiScreen.Module spearLunge = new ClickGuiScreen.Module(
            "Spear Lunge", "Applies directional movement impulse when holding Spear or Trident.", () -> cfg.spearLungeEnabled, v -> {
               cfg.spearLungeEnabled = v;
               cfg.save();
            }
         );
         spearLunge.addSetting(new ClickGuiScreen.Setting("Keybind", ClickGuiScreen.SettingType.KEYBIND, () -> cfg.spearLungeKey, v -> {
            cfg.spearLungeKey = (Integer)v;
            cfg.save();
         }));
         spearLunge.addSetting(new ClickGuiScreen.Setting("Min Hunger", ClickGuiScreen.SettingType.NUMBER, () -> (int)cfg.spearLungeMinHunger, v -> {
            cfg.spearLungeMinHunger = ((Number)v).doubleValue();
            cfg.save();
         }, 0, 20));
         spearLunge.addSetting(new ClickGuiScreen.Setting("Cooldown (ms)", ClickGuiScreen.SettingType.NUMBER, () -> cfg.spearLungeCooldown, v -> {
            cfg.spearLungeCooldown = (Integer)v;
            cfg.save();
         }, 0, 2000));
         spearLunge.addSetting(new ClickGuiScreen.Setting("Power", ClickGuiScreen.SettingType.NUMBER, () -> (int)(cfg.spearLungePower * 10.0), v -> {
            cfg.spearLungePower = ((Number)v).doubleValue() / 10.0;
            cfg.save();
         }, 5, 30));
         spearLunge.addSetting(
            new ClickGuiScreen.Setting("Upward Power", ClickGuiScreen.SettingType.NUMBER, () -> (int)(cfg.spearLungeUpwardPower * 10.0), v -> {
               cfg.spearLungeUpwardPower = ((Number)v).doubleValue() / 10.0;
               cfg.save();
            }, 0, 10)
         );
         maceList.add(spearLunge);
         this.modules.put("Mace", maceList);
         List<ClickGuiScreen.Module> movementList = new ArrayList<>();
         ClickGuiScreen.Module autoSprint = new ClickGuiScreen.Module(
            "Auto Sprint", "Automatically keeps sprint active during combat and movement", () -> cfg.movementAutoSprintEnabled, v -> {
               cfg.movementAutoSprintEnabled = v;
               cfg.save();
            }
         );
         movementList.add(autoSprint);
         ClickGuiScreen.Module speedBridge = new ClickGuiScreen.Module(
            "Speed Bridge", "Auto sneaks at edge of blocks for fast bridging", () -> cfg.movementSpeedBridgeEnabled, v -> {
               cfg.movementSpeedBridgeEnabled = v;
               cfg.save();
            }
         );
         speedBridge.addSetting(new ClickGuiScreen.Setting("Sneak Delay", ClickGuiScreen.SettingType.NUMBER, () -> cfg.movementSpeedBridgeDelay, v -> {
            cfg.movementSpeedBridgeDelay = (Integer)v;
            cfg.save();
         }, 10, 200));
         movementList.add(speedBridge);
         ClickGuiScreen.Module keyWindCharge = new ClickGuiScreen.Module(
            "Key WindCharge", "Automates quick 90 degree downward wind charge burst and jump boost.", () -> cfg.keyWindChargeEnabled, v -> {
               cfg.keyWindChargeEnabled = v;
               cfg.save();
            }
         );
         keyWindCharge.addSetting(new ClickGuiScreen.Setting("Keybind", ClickGuiScreen.SettingType.KEYBIND, () -> cfg.keyWindChargeKey, v -> {
            cfg.keyWindChargeKey = (Integer)v;
            cfg.save();
         }));
         keyWindCharge.addSetting(new ClickGuiScreen.Setting("Cooldown (ms)", ClickGuiScreen.SettingType.NUMBER, () -> cfg.keyWindChargeCooldown, v -> {
            cfg.keyWindChargeCooldown = (Integer)v;
            cfg.save();
         }, 0, 2000));
         keyWindCharge.addSetting(new ClickGuiScreen.Setting("Jump Delay (ms)", ClickGuiScreen.SettingType.NUMBER, () -> cfg.keyWindChargeJumpDelay, v -> {
            cfg.keyWindChargeJumpDelay = (Integer)v;
            cfg.save();
         }, 0, 500));
         keyWindCharge.addSetting(new ClickGuiScreen.Setting("Restore Pitch", ClickGuiScreen.SettingType.BOOLEAN, () -> cfg.keyWindChargeRestorePitch, v -> {
            cfg.keyWindChargeRestorePitch = (Boolean)v;
            cfg.save();
         }));
         keyWindCharge.addSetting(new ClickGuiScreen.Setting("Switch Back", ClickGuiScreen.SettingType.BOOLEAN, () -> cfg.keyWindChargeSwitchBack, v -> {
            cfg.keyWindChargeSwitchBack = (Boolean)v;
            cfg.save();
         }));
         movementList.add(keyWindCharge);
         ClickGuiScreen.Module perfectWindcharge = new ClickGuiScreen.Module(
            "Perfect Windcharge",
            "Detects incoming or active wind charge at feet and automatically triggers perfect jump boost.",
            () -> cfg.perfectWindChargeEnabled,
            v -> {
               cfg.perfectWindChargeEnabled = v;
               cfg.save();
            }
         );
         perfectWindcharge.addSetting(
            new ClickGuiScreen.Setting("Trigger Range", ClickGuiScreen.SettingType.NUMBER, () -> (int)(cfg.perfectWindChargeTriggerRange * 10.0), v -> {
               cfg.perfectWindChargeTriggerRange = ((Number)v).doubleValue() / 10.0;
               cfg.save();
            }, 5, 50)
         );
         movementList.add(perfectWindcharge);
         ClickGuiScreen.Module jumpReset = new ClickGuiScreen.Module("Jump Reset", "Jumps after a hit to reduce knockback.", () -> cfg.jumpResetEnabled, v -> {
            cfg.jumpResetEnabled = v;
            cfg.save();
         });
         jumpReset.addSetting(new ClickGuiScreen.Setting("Mode", ClickGuiScreen.SettingType.MODE, () -> cfg.jumpResetMode, v -> {
            cfg.jumpResetMode = (Integer)v;
            cfg.save();
         }, new String[]{"Immediate", "Next Tick"}));
         jumpReset.addSetting(new ClickGuiScreen.Setting("Players Only", ClickGuiScreen.SettingType.BOOLEAN, () -> cfg.jumpResetPlayersOnly, v -> {
            cfg.jumpResetPlayersOnly = (Boolean)v;
            cfg.save();
         }));
         jumpReset.addSetting(new ClickGuiScreen.Setting("Only Facing Target", ClickGuiScreen.SettingType.BOOLEAN, () -> cfg.jumpResetFacingOnly, v -> {
            cfg.jumpResetFacingOnly = (Boolean)v;
            cfg.save();
         }));
         jumpReset.addSetting(new ClickGuiScreen.Setting("Chance (%)", ClickGuiScreen.SettingType.NUMBER, () -> cfg.jumpResetChance, v -> {
            cfg.jumpResetChance = (Integer)v;
            cfg.save();
         }, 0, 100));
         jumpReset.addSetting(new ClickGuiScreen.Setting("Cooldown (ticks)", ClickGuiScreen.SettingType.NUMBER, () -> cfg.jumpResetCooldown, v -> {
            cfg.jumpResetCooldown = (Integer)v;
            cfg.save();
         }, 0, 20));
         movementList.add(jumpReset);
         ClickGuiScreen.Module noSlow = new ClickGuiScreen.Module(
            "NoSlow", "Maintains full movement speed while consuming items or interacting.", () -> cfg.noSlowEnabled, v -> {
               cfg.noSlowEnabled = v;
               cfg.save();
            }
         );
         noSlow.addSetting(new ClickGuiScreen.Setting("Mode", ClickGuiScreen.SettingType.MODE, () -> cfg.noSlowMode, v -> {
            cfg.noSlowMode = (Integer)v;
            cfg.save();
         }, new String[]{"Vanilla", "Grim"}));
         movementList.add(noSlow);
         this.modules.put("Movement", movementList);
         List<ClickGuiScreen.Module> playerList = new ArrayList<>();
         ClickGuiScreen.Module panic = new ClickGuiScreen.Module(
            "Panic Disconnect", "Disconnects immediately from server when pressing panic key", () -> cfg.playerPanicEnabled, v -> {
               cfg.playerPanicEnabled = v;
               cfg.save();
            }
         );
         panic.addSetting(new ClickGuiScreen.Setting("Keybind", ClickGuiScreen.SettingType.KEYBIND, () -> cfg.playerPanicKey, v -> {
            cfg.playerPanicKey = (Integer)v;
            cfg.save();
         }));
         playerList.add(panic);
         ClickGuiScreen.Module nameProtect = new ClickGuiScreen.Module(
            "Name Protect", "Hides your username in chat, TAB list and overhead", () -> cfg.playerNameProtectEnabled, v -> {
               cfg.playerNameProtectEnabled = v;
               cfg.save();
            }
         );
         playerList.add(nameProtect);
         ClickGuiScreen.Module hoverPotion = new ClickGuiScreen.Module(
            "Hover Potion", "Automatically swaps hovered potions into designated hotbar slot inside inventory screens.", () -> cfg.hoverPotionEnabled, v -> {
               cfg.hoverPotionEnabled = v;
               cfg.save();
            }
         );
         hoverPotion.addSetting(new ClickGuiScreen.Setting("Delay (ms)", ClickGuiScreen.SettingType.NUMBER, () -> cfg.hoverPotionDelay, v -> {
            cfg.hoverPotionDelay = (Integer)v;
            cfg.save();
         }, 0, 500));
         hoverPotion.addSetting(new ClickGuiScreen.Setting("Target Slot", ClickGuiScreen.SettingType.NUMBER, () -> cfg.hoverPotionTargetSlot, v -> {
            cfg.hoverPotionTargetSlot = (Integer)v;
            cfg.save();
         }, 1, 9));
         playerList.add(hoverPotion);
         this.modules.put("Player", playerList);
         List<ClickGuiScreen.Module> otherList = new ArrayList<>();
         ClickGuiScreen.Module zFastXp = new ClickGuiScreen.Module(
            "Fast XP", "Rapid Experience Bottle thrower. Continuously throws XP bottles at maximum server speed.", () -> cfg.fastXpEnabled, v -> {
               cfg.fastXpEnabled = v;
               cfg.save();
            }
         );
         zFastXp.addSetting(new ClickGuiScreen.Setting("Throw Delay", ClickGuiScreen.SettingType.NUMBER, () -> cfg.fastXpDelay, v -> {
            cfg.fastXpDelay = (Integer)v;
            cfg.save();
         }, 1, 4));
         zFastXp.addSetting(new ClickGuiScreen.Setting("Switch Delay (ms)", ClickGuiScreen.SettingType.NUMBER, () -> cfg.fastXpSwitchDelay, v -> {
            cfg.fastXpSwitchDelay = (Integer)v;
            cfg.save();
         }, 0, 300));
         otherList.add(zFastXp);
         ClickGuiScreen.Module zShield = new ClickGuiScreen.Module(
            "Shield Breaker",
            "Automated shield breaker. Automatically switches to axe and breaks enemy blocking shields, then returns.",
            () -> cfg.shieldBreakerEnabled,
            v -> {
               cfg.shieldBreakerEnabled = v;
               cfg.save();
            }
         );
         zShield.addSetting(new ClickGuiScreen.Setting("Auto Swap Back", ClickGuiScreen.SettingType.BOOLEAN, () -> cfg.shieldBreakerAutoSwapBack, v -> {
            cfg.shieldBreakerAutoSwapBack = (Boolean)v;
            cfg.save();
         }));
         zShield.addSetting(new ClickGuiScreen.Setting("Kill Switch", ClickGuiScreen.SettingType.BOOLEAN, () -> cfg.shieldBreakerKillSwitch, v -> {
            cfg.shieldBreakerKillSwitch = (Boolean)v;
            cfg.save();
         }));
         zShield.addSetting(new ClickGuiScreen.Setting("Axe Delay", ClickGuiScreen.SettingType.NUMBER, () -> cfg.shieldBreakerAxeSwitchDelay, v -> {
            cfg.shieldBreakerAxeSwitchDelay = (Integer)v;
            cfg.save();
         }, 0, 10));
         zShield.addSetting(new ClickGuiScreen.Setting("Weapon Delay", ClickGuiScreen.SettingType.NUMBER, () -> cfg.shieldBreakerWeaponSwitchDelay, v -> {
            cfg.shieldBreakerWeaponSwitchDelay = (Integer)v;
            cfg.save();
         }, 0, 10));
         zShield.addSetting(new ClickGuiScreen.Setting("Cooldown", ClickGuiScreen.SettingType.NUMBER, () -> cfg.shieldBreakerCycleCooldown, v -> {
            cfg.shieldBreakerCycleCooldown = (Integer)v;
            cfg.save();
         }, 0, 20));
         otherList.add(zShield);
         ClickGuiScreen.Module delayRand = new ClickGuiScreen.Module(
            "Delay Randomizer",
            "Humanizes macro click delays with Gaussian statistical variation to bypass anti-cheat checks.",
            () -> cfg.randomizeDelays,
            v -> {
               cfg.randomizeDelays = v;
               cfg.save();
            }
         );
         delayRand.addSetting(new ClickGuiScreen.Setting("Range", ClickGuiScreen.SettingType.NUMBER, () -> cfg.randomizationRange, v -> {
            cfg.randomizationRange = (Integer)v;
            cfg.save();
         }, 1, 5));
         delayRand.addSetting(new ClickGuiScreen.Setting("Gaussian", ClickGuiScreen.SettingType.BOOLEAN, () -> cfg.humanizerGaussian, v -> {
            cfg.humanizerGaussian = (Boolean)v;
            cfg.save();
         }));
         otherList.add(delayRand);
         ClickGuiScreen.Module trajectories = new ClickGuiScreen.Module(
            "Trajectories", "Shows projectile flight paths, landing points and targets.", () -> cfg.trajectoriesEnabled, v -> {
               cfg.trajectoriesEnabled = v;
               cfg.save();
            }
         );
         trajectories.addSetting(new ClickGuiScreen.Setting("Max Ticks", ClickGuiScreen.SettingType.NUMBER, () -> cfg.trajectoriesMaxTicks, v -> {
            cfg.trajectoriesMaxTicks = (Integer)v;
            cfg.save();
         }, 30, 300));
         trajectories.addSetting(new ClickGuiScreen.Setting("Show Landing", ClickGuiScreen.SettingType.BOOLEAN, () -> cfg.trajectoriesShowLanding, v -> {
            cfg.trajectoriesShowLanding = (Boolean)v;
            cfg.save();
         }));
         trajectories.addSetting(new ClickGuiScreen.Setting("In-Flight", ClickGuiScreen.SettingType.BOOLEAN, () -> cfg.trajectoriesInFlight, v -> {
            cfg.trajectoriesInFlight = (Boolean)v;
            cfg.save();
         }));
         String[] channelNames = new String[]{"Red", "Green", "Blue", "Opacity"};
         int[] channelShifts = new int[]{16, 8, 0, 24};

         for (int colorIndex = 0; colorIndex < 2; colorIndex++) {
            boolean inFlight = colorIndex == 1;

            for (int channel = 0; channel < 4; channel++) {
               int shift = channelShifts[channel];
               trajectories.addSetting(
                  new ClickGuiScreen.Setting(
                     (inFlight ? "Flight " : "Held ") + channelNames[channel],
                     ClickGuiScreen.SettingType.NUMBER,
                     () -> (inFlight ? cfg.trajectoriesInFlightColor : cfg.trajectoriesColor) >>> shift & 0xFF,
                     v -> {
                        int color = inFlight ? cfg.trajectoriesInFlightColor : cfg.trajectoriesColor;
                        color = color & ~(255 << shift) | ((Integer)v & 0xFF) << shift;
                        if (inFlight) {
                           cfg.trajectoriesInFlightColor = color;
                        } else {
                           cfg.trajectoriesColor = color;
                        }

                        cfg.save();
                     },
                     0,
                     255
                  )
               );
            }
         }

         otherList.add(trajectories);
         ClickGuiScreen.Module updateChecker = new ClickGuiScreen.Module(
            "Update Checker", "Checks for new CPvP Macros updates on startup and notifies on the start screen.", () -> cfg.checkUpdates, v -> {
               cfg.checkUpdates = v;
               cfg.save();
            }
         );
         otherList.add(updateChecker);
         this.modules.put("Other", otherList);
         String[] emptyCats = new String[]{"Configs"};

         for (String cat : emptyCats) {
            this.modules.put(cat, new ArrayList<>());
         }
      }
   }

   @Override
   public void renderContent(GuiGraphicsContext c, int mouseX, int mouseY, float delta) {
      long start = System.nanoTime();
      c.resetMetrics();
      this.lastContext = c;
      long now = System.nanoTime();
      this.dt = this.lastFrameNs == 0L ? 0.016F : Math.min(0.1F, (float)(now - this.lastFrameNs) / 1.0E9F);
      this.lastFrameNs = now;
      if (!this.closing) {
         this.openProgress = Math.min(1.0F, this.openProgress + this.dt / 0.16F);
      } else {
         this.closeProgress = Math.min(1.0F, this.closeProgress + this.dt / 0.1F);
         if (this.closeProgress >= 1.0F) {
            this.closeScreen();
            return;
         }
      }

      float open = GuiLayout.ease(this.openProgress);
      float close = GuiLayout.ease(this.closeProgress);
      this.opacity = this.closing ? 1.0F - close : open;
      this.drawerAnim = this.selectedModule == null ? Math.max(0.0F, this.drawerAnim - this.dt / 0.15F) : Math.min(1.0F, this.drawerAnim + this.dt / 0.15F);
      if (this.drawerAnim == 0.0F) {
         this.lastDrawnModule = null;
      }

      this.scrollOffset = damp((float)this.scrollOffset, (float)this.targetScrollOffset, this.dt);
      this.drawerScrollOffset = damp((float)this.drawerScrollOffset, (float)this.targetDrawerScrollOffset, this.dt);
      this.layout = GuiLayout.fit(this.getScreenWidth(), this.getScreenHeight(), this.drawerAnim, this.closing ? 1.0F - 0.04F * close : 0.96F + 0.04F * open);
      if (this.lastScreenWidth != this.getScreenWidth() || this.lastScreenHeight != this.getScreenHeight()) {
         this.lastScreenWidth = this.getScreenWidth();
         this.lastScreenHeight = this.getScreenHeight();
         this.textDirty = true;
      }

      if (this.textDirty) {
         this.prepareTexts(c);
      }

      this.prepareSettings(c, this.selectedModule != null ? this.selectedModule : this.lastDrawnModule);
      double mx = this.layout.localX(mouseX);
      double my = this.layout.localY(mouseY);
      if (this.draggingSetting != null) {
         this.handleDrag(mx, my);
      }

      c.setOpacity(this.opacity);
      GuiCompat.drawRect(c, 0, 0, this.getScreenWidth(), this.getScreenHeight(), Theme.OVERLAY_BG);
      c.pushMatrix();
      c.translateMatrix(this.layout.x, this.layout.y);
      c.scaleMatrix(this.layout.scale, this.layout.scale);
      GuiCompat.drawRoundedRect(c, 0, 0, 640, 384, 14, Theme.PANEL_BG);
      this.drawSidebar(c, mx, my);
      this.drawHeader(c, mx, my);
      this.drawContent(c, mx, my);
      if (this.selectedModule != null) {
         this.lastDrawnModule = this.selectedModule;
      }

      if (this.drawerAnim > 0.0F && (this.selectedModule != null || this.lastDrawnModule != null)) {
         this.drawSettingsDrawer(c, 0, 0, mx, my);
      }

      this.drawTooltip(c, mx, my, now);
      c.popMatrix();
      c.setOpacity(1.0F);
      this.measuredTimeNs = System.nanoTime() - start;
      this.measuredFills = c.getFillCalls();
      this.measuredTextures = c.getTextureCalls();
      this.measuredTexts = c.getDrawTextCalls();
      this.measuredMatrices = c.getMatrixPushCalls();
   }

   private void drawSidebar(GuiGraphicsContext c, double mx, double my) {
      GuiCompat.drawRoundedRect(c, 12, 12, 26, 26, 8, Theme.ACCENT_MUTED);
      c.drawLogo(17, 17, 16);
      c.drawTextMedium("CPvP Macros", 46, 18, Theme.TEXT_PRIMARY, false);
      GuiCompat.drawRoundedRect(c, 12, 50, 140, 26, 10, Theme.SEARCH_BG);
      if (this.searchActive) {
         c.drawNineSlice(NineSliceSprite.BORDER_SEARCH, 12, 50, 140, 26, Theme.ACCENT);
      }

      c.drawIcon(IconId.SEARCH, 20, 56, 13, Theme.TEXT_MUTED);
      String search = this.searchText.isEmpty() ? "Search" : this.ellipsis(c, this.searchText, 100, true);
      c.drawTextSmall(search, 40, 57, this.searchText.isEmpty() ? Theme.TEXT_MUTED : Theme.TEXT_PRIMARY, false);
      c.drawTextSmall("Modules", 20, 88, Theme.TEXT_SECTION, false);

      for (int i = 0; i < CATEGORIES.length; i++) {
         this.drawSidebarItem(c, CATEGORIES[i], 106 + i * 28, mx, my);
      }

      c.drawTextSmall("Client", 20, 284, Theme.TEXT_SECTION, false);
      this.drawSidebarItem(c, "Configs", 302, mx, my);
      int y = 336;
      GuiCompat.drawRoundedRect(c, 12, y, 140, 36, 10, Theme.CARD_BG);
      String name = c.getSessionPlayerName();
      if (name == null || name.isBlank()) {
         name = "Player";
      }

      if (!c.drawPlayerAvatar(20, y + 10, 16)) {
         GuiCompat.drawRoundedRect(c, 17, y + 5, 26, 26, 8, Theme.ACCENT_MUTED);
         c.drawTextMedium(name.substring(0, name.offsetByCodePoints(0, 1)), 25, y + 11, Theme.ACCENT_HOVER, false);
      }

      c.drawTextSmall(this.ellipsis(c, name, 94, true), 48, y + 12, Theme.TEXT_PRIMARY, false);
   }

   private void drawSidebarItem(GuiGraphicsContext c, String name, int y, double mx, double my) {
      boolean selected = !this.searching() && this.activeCategory.equals(name);
      boolean hover = in(mx, my, 12, y, 140, 24);
      float a = damp(this.sidebarStates.getOrDefault(name, selected ? 1.0F : 0.0F), selected ? 1.0F : 0.0F, this.dt);
      this.sidebarStates.put(name, a);
      if (a > 0.0F) {
         c.drawNineSlice(NineSliceSprite.PILL, 12, y, 140, 24, Theme.state(Theme.SIDEBAR, a));
      } else if (hover) {
         GuiCompat.drawRoundedRect(c, 12, y, 140, 24, 10, Theme.CARD_HOVER_BG);
      }

      int color = Theme.state(Theme.ICONS, a);
      c.drawIcon(IconId.category(name), 22, y + 5, 13, color);
      c.drawTextMedium(name, 42, y + 5, color, false);
      String count = this.counts.get(name);
      if (count != null) {
         c.drawTextSmall(count, 140 - c.getTextWidthSmall(count), y + 6, color, false);
      }
   }

   private void drawHeader(GuiGraphicsContext c, double mx, double my) {
      String title = this.searching() ? "Results" : this.activeCategory;
      c.drawTextHeading(title, 176, 15, Theme.TEXT_PRIMARY);
      if (!this.activeCategory.equals("Configs") || this.searching()) {
         c.drawTextSmall(this.headingCount, 184 + c.getTextWidthHeading(title), 18, Theme.TEXT_SECTION, false);
      }

      if (!this.filterPills.isEmpty()) {
         ClickGuiScreen.Pill first = this.filterPills.get(0);
         ClickGuiScreen.Pill last = this.filterPills.get(this.filterPills.size() - 1);
         c.drawNineSlice(NineSliceSprite.PILL, first.x - 3, 10, last.x + last.width - first.x + 6, 28, Theme.CARD_BG);

         for (ClickGuiScreen.Pill p : this.filterPills) {
            float state = damp(
               this.pillStates.getOrDefault(p.label, p.label.equals(this.activeFilter) ? 1.0F : 0.0F), p.label.equals(this.activeFilter) ? 1.0F : 0.0F, this.dt
            );
            this.pillStates.put(p.label, state);
            if (state > 0.0F) {
               c.drawNineSlice(NineSliceSprite.PILL, p.x, 13, p.width, 22, Theme.state(Theme.PILLS, state));
            } else if (in(mx, my, p.x, 13, p.width, 22)) {
               c.drawNineSlice(NineSliceSprite.PILL, p.x, 13, p.width, 22, Theme.CARD_HOVER_BG);
            }

            c.drawTextSmall(p.caption, p.x + (p.width - c.getTextWidthSmall(p.caption)) / 2, 18, Theme.state(Theme.PILL_TEXT, state), false);
         }
      }
   }

   private void drawContent(GuiGraphicsContext c, double mx, double my) {
      if (this.activeCategory.equals("Configs") && !this.searching()) {
         this.drawConfigs(c, mx, my);
      } else {
         c.enableScissor(176, 48, 452, 304);
         ClickGuiScreen.Module hovered = null;

         for (int i = 0; i < this.visible.size(); i++) {
            int x = 176 + i % 2 * 230;
            int y = 48 + i / 2 * 52 - (int)Math.round(this.scrollOffset);
            if (y + 44 > 48
               && y < 352
               && (this.drawerAnim != 1.0F || this.layout.attached || this.selectedModule == null || x + 222 <= this.layout.drawerX(this.drawerAnim))) {
               ClickGuiScreen.Module m = this.visible.get(i);
               boolean over = in(mx, my, x, y, 222, 44) && in(mx, my, 176, 48, 452, 304) && !this.drawerBlocks(mx, my);
               this.drawModuleCard(c, m, x, y, over);
               if (over) {
                  hovered = m;
               }
            }
         }

         c.disableScissor();
         if (hovered != this.hoverModule) {
            this.hoverModule = hovered;
            this.hoverStartNs = System.nanoTime();
         }

         if (this.visible.isEmpty()) {
            String empty = "No modules found";
            c.drawTextSmall(empty, 176 + (452 - c.getTextWidthSmall(empty)) / 2, 200, Theme.TEXT_MUTED, false);
         }

         c.drawTextSmall("Left click: toggle · Right click: settings", 176, 362, Theme.TEXT_SECTION, false);
         if (this.maxScroll > 0) {
            this.drawScroll(c, 633, 48, 304, this.scrollOffset, this.maxScroll, 304 + this.maxScroll);
         }
      }
   }

   private void drawScroll(GuiGraphicsContext c, int x, int y, int height, double offset, int max, int total) {
      int thumb = Math.max(16, height * height / Math.max(height, total));
      GuiCompat.drawRoundedRect(c, x, y, 2, height, 1, Theme.BUTTON_BG);
      GuiCompat.drawRoundedRect(c, x, y + (int)Math.round(offset / max * (height - thumb)), 2, thumb, 1, Theme.TEXT_MUTED);
   }

   private void drawModuleCard(GuiGraphicsContext c, ClickGuiScreen.Module m, int x, int y, boolean hovered) {
      ClickGuiScreen.Visual v = this.visuals.get(m);
      v.active = damp(v.active, m.isEnabled() ? 1.0F : 0.0F, this.dt);
      v.hover = damp(v.hover, hovered ? 1.0F : 0.0F, this.dt);
      v.press = damp(v.press, System.nanoTime() - v.pressedAt < 100000000L ? 1.0F : 0.0F, this.dt);
      GuiCompat.drawRoundedRect(c, x, y, 222, 44, 9, Theme.card(v.active, v.hover, v.press));
      if (v.active > 0.0F) {
         c.drawNineSlice(NineSliceSprite.BORDER_CARD, x, y, 222, 44, Theme.state(Theme.RINGS, v.active));
      }

      GuiCompat.drawRoundedRect(c, x + 8, y + 9, 26, 26, 8, Theme.state(Theme.ICON_TILES, v.active));
      c.drawIcon(IconId.module(m.name), x + 14, y + 15, 14, Theme.state(Theme.ICONS, v.active));
      ClickGuiScreen.TextCache t = this.texts.get(m);
      c.drawTextMedium(t.title, x + 42, y + 7, Theme.state(Theme.TITLES, v.active), false);
      c.drawTextSmall(t.description, x + 42, y + 25, Theme.TEXT_DESCRIPTION, false);
      if (t.key > 0 || this.bindingModule == m) {
         String key = this.bindingModule == m ? "…" : t.keyText;
         int width = this.bindingModule == m ? 18 : t.keyWidth;
         GuiCompat.drawRoundedRect(c, x + 222 - width - 8, y + 8, width, 16, 6, Theme.BUTTON_BG);
         if (this.bindingModule == m) {
            c.drawNineSlice(NineSliceSprite.BORDER_CHIP, x + 222 - width - 8, y + 8, width, 16, Theme.ACCENT);
         }

         c.drawTextSmall(key, x + 222 - width - 3, y + 10, Theme.TEXT_KEY, false);
      }
   }

   private void drawTooltip(GuiGraphicsContext c, double mx, double my, long now) {
      if (this.hoverModule != null && now - this.hoverStartNs >= 400000000L && !this.drawerBlocks(mx, my)) {
         ClickGuiScreen.TextCache t = this.texts.get(this.hoverModule);
         if (t != null && t.clipped) {
            int w = 248;
            int h = 30 + t.tooltip.size() * 14;
            int x = Math.max(176, Math.min(640 - w - 8, (int)mx + 12));
            int y = Math.max(4, Math.min(384 - h - 4, (int)my + 16));
            c.nextLayer();
            GuiCompat.drawRoundedRectWithBorder(c, x, y, w, h, 9, 1, Theme.SETTINGS_BORDER, Theme.SETTINGS_BG);
            c.drawTextMedium(this.hoverModule.name, x + 8, y + 7, Theme.TEXT_PRIMARY, false);

            for (int i = 0; i < t.tooltip.size(); i++) {
               c.drawTextSmall(t.tooltip.get(i), x + 8, y + 25 + i * 14, Theme.TEXT_DESCRIPTION, false);
            }
         }
      }
   }

   private void drawConfigs(GuiGraphicsContext c, double mx, double my) {
      this.configProfiles.draw(c, mx, my);
      this.syncTextInput();
   }

   private void prepareSettings(GuiGraphicsContext c, ClickGuiScreen.Module m) {
      if (this.rowsModule != m) {
         this.rowsModule = m;
         if (m == null) {
            this.settingRows = List.of();
         } else {
            this.drawerDescription = c.wrapTextSmall(m.description, 200);
            if (this.drawerDescription.size() > 2) {
               String tail = String.join(" ", this.drawerDescription.subList(1, this.drawerDescription.size()));
               this.drawerDescription = List.of(this.drawerDescription.getFirst(), this.ellipsis(c, tail, 200, true));
            }

            List<ClickGuiScreen.SettingRow> rows = new ArrayList<>();
            int y = 76;
            rows.add(new ClickGuiScreen.SettingRow(null, y, 40));
            y += 40;

            for (ClickGuiScreen.Setting s : m.settings) {
               int h = s.type == ClickGuiScreen.SettingType.BOOLEAN ? 32 : 40;
               rows.add(new ClickGuiScreen.SettingRow(s, y, h));
               y += h;
               int labelWidth = 200 - (s.type == ClickGuiScreen.SettingType.BOOLEAN ? 38 : (s.type == ClickGuiScreen.SettingType.NUMBER ? 48 : 0));
               this.settingLabels.put(s, this.ellipsis(c, s.name, labelWidth, true));
            }

            this.settingRows = List.copyOf(rows);
            this.lastDrawerClipTop = 76;
            this.lastDrawerTotalContentH = y - 76;
            this.maxDrawerScroll = Math.max(0, this.lastDrawerTotalContentH - 298);
            this.targetDrawerScrollOffset = Math.max(0.0, Math.min((double)this.maxDrawerScroll, this.targetDrawerScrollOffset));
         }
      }
   }

   private void drawSettingsDrawer(GuiGraphicsContext c, int left, int top, double mx, double my) {
      ClickGuiScreen.Module m = this.selectedModule != null ? this.selectedModule : this.lastDrawnModule;
      if (m != null) {
         int x = this.layout.drawerX(this.drawerAnim);
         int w = 224;
         c.nextLayer();
         c.setOpacity(this.opacity * GuiLayout.ease(this.drawerAnim));
         GuiCompat.drawRoundedRect(c, x, 0, w, 384, 14, Theme.SETTINGS_BG);
         c.drawNineSlice(NineSliceSprite.BORDER_PANEL, x, 0, w, 384, Theme.SETTINGS_BORDER);
         c.drawTextMedium(m.name, x + 12, 13, Theme.TEXT_PRIMARY, false);
         c.drawIcon(IconId.CLOSE, x + w - 25, 13, 13, in(mx, my, x + w - 28, 8, 24, 24) ? Theme.ACCENT_HOVER : Theme.TEXT_MUTED);

         for (int i = 0; i < Math.min(2, this.drawerDescription.size()); i++) {
            c.drawTextSmall(this.drawerDescription.get(i), x + 12, 35 + i * 14, Theme.TEXT_DESCRIPTION, false);
         }

         c.enableScissor(x + 8, 76, w - 16, 298);
         int offset = (int)Math.round(this.drawerScrollOffset);

         for (ClickGuiScreen.SettingRow row : this.settingRows) {
            int y = row.y - offset;
            if (y + row.height > 76 && y < 374) {
               ClickGuiScreen.Setting s = row.setting;
               if (s == null) {
                  c.drawTextSmall("Toggle Key", x + 12, y + 3, Theme.TEXT_MUTED, false);
                  this.drawKeyField(c, x + 12, y + 18, w - 24, m.getKey == null ? -1 : m.getKey.get(), this.bindingModule == m);
               } else {
                  c.drawTextSmall(this.settingLabels.get(s), x + 12, y + 3, Theme.TEXT_MUTED, false);
                  if (s.type == ClickGuiScreen.SettingType.BOOLEAN) {
                     boolean enabled = Boolean.TRUE.equals(s.getVal.get());
                     float value = damp(this.toggleStates.getOrDefault(s, enabled ? 1.0F : 0.0F), enabled ? 1.0F : 0.0F, this.dt);
                     this.toggleStates.put(s, value);
                     GuiCompat.drawSmoothToggle(c, x + w - 38, y + 2, 26, 14, value, Theme.TOGGLE_ON, Theme.TOGGLE_OFF);
                  } else if (s.type == ClickGuiScreen.SettingType.KEYBIND) {
                     this.drawKeyField(c, x + 12, y + 18, w - 24, ((Number)s.getVal.get()).intValue(), this.bindingSetting == s);
                  } else if (s.type == ClickGuiScreen.SettingType.MODE || s.type == ClickGuiScreen.SettingType.NUMBER && s.max - s.min == 1) {
                     int current = ((Number)s.getVal.get()).intValue();
                     boolean binary = s.type == ClickGuiScreen.SettingType.NUMBER || s.modeOptions != null && s.modeOptions.length == 2;
                     if (binary) {
                        for (int i = 0; i < 2; i++) {
                           boolean active = current == (s.type == ClickGuiScreen.SettingType.NUMBER ? s.min + i : i);
                           int sx = x + 12 + i * (w - 24) / 2;
                           int sw = (w - 24) / 2 - 2;
                           String label = s.type == ClickGuiScreen.SettingType.NUMBER ? Integer.toString(s.min + i) : s.modeOptions[i];
                           GuiCompat.drawRoundedRect(c, sx, y + 18, sw, 18, 8, active ? Theme.ACCENT_MUTED : Theme.BUTTON_BG);
                           c.drawTextSmall(label, sx + (sw - c.getTextWidthSmall(label)) / 2, y + 21, active ? Theme.ACCENT_HOVER : Theme.TEXT_MUTED, false);
                        }
                     } else if (s.modeOptions != null && s.modeOptions.length > 0) {
                        int idx = Math.max(0, Math.min(s.modeOptions.length - 1, current));
                        GuiCompat.drawRoundedRect(c, x + 12, y + 18, w - 24, 18, 8, Theme.BUTTON_BG);
                        c.drawTextSmall(s.modeOptions[idx], x + 20, y + 21, Theme.TEXT_PRIMARY, false);
                        c.drawIcon(IconId.CHEVRON_DOWN, x + w - 31, y + 21, 11, Theme.TEXT_MUTED);
                     }
                  } else if (s.type == ClickGuiScreen.SettingType.NUMBER) {
                     int value = ((Number)s.getVal.get()).intValue();
                     ClickGuiScreen.NumberText text = this.numberTexts.get(s);
                     if (text == null || text.value != value) {
                        text = new ClickGuiScreen.NumberText(value, s.displayScale == 1.0 ? Integer.toString(value) : Double.toString(value * s.displayScale));
                        this.numberTexts.put(s, text);
                     }

                     c.drawTextSmall(text.text, x + w - 12 - c.getTextWidthSmall(text.text), y + 3, Theme.TEXT_MUTED, false);
                     int bx = x + 12;
                     int bw = w - 24;
                     int by = y + 24;
                     float ratio = this.draggingSetting == s && this.draggingVisualRatio != null
                        ? this.draggingVisualRatio
                        : (float)(value - s.min) / Math.max(1, s.max - s.min);
                     ratio = Math.max(0.0F, Math.min(1.0F, ratio));
                     int knob = bx + Math.round(ratio * (bw - 10));
                     GuiCompat.drawRoundedRect(c, bx, by, bw, 4, 2, Theme.BUTTON_BG);
                     GuiCompat.drawRoundedRect(c, bx, by, knob - bx + 5, 4, 2, Theme.ACCENT);
                     c.drawNineSlice(NineSliceSprite.PILL, knob, by - 3, 10, 10, Theme.WHITE);
                  }
               }
            }
         }

         c.disableScissor();
         if (this.maxDrawerScroll > 0) {
            this.drawScroll(c, x + w - 5, 76, 298, this.drawerScrollOffset, this.maxDrawerScroll, this.lastDrawerTotalContentH);
         }

         c.setOpacity(this.opacity);
      }
   }

   private void drawKeyField(GuiGraphicsContext c, int x, int y, int width, int key, boolean binding) {
      GuiCompat.drawRoundedRect(c, x, y, width, 18, 6, Theme.BUTTON_BG);
      if (binding) {
         c.drawNineSlice(NineSliceSprite.BORDER_CHIP, x, y, width, 18, Theme.ACCENT);
      }

      String label = binding ? "Press a key…" : this.getKeyName(key);
      c.drawTextSmall(label, x + (width - c.getTextWidthSmall(label)) / 2, y + 3, Theme.TEXT_KEY, false);
   }

   private boolean settingsClick(int mx, int my, int button) {
      if (button == 0 && this.selectedModule != null) {
         int x = this.layout.drawerX(this.drawerAnim);
         int w = 224;
         if (in(mx, my, x + w - 28, 8, 24, 24)) {
            this.selectedModule = null;
            this.clearBinding();
            return true;
         } else if (this.maxDrawerScroll > 0 && in(mx, my, x + w - 10, 76, 10, 298)) {
            this.draggingDrawerScroll = true;
            this.onDrawerScrollDrag(my);
            return true;
         } else if (!in(mx, my, x + 8, 76, w - 16, 298)) {
            return true;
         } else {
            for (ClickGuiScreen.SettingRow row : this.settingRows) {
               int y = row.y - (int)Math.round(this.drawerScrollOffset);
               if (y + row.height > 76 && y < 374) {
                  ClickGuiScreen.Setting s = row.setting;
                  if (s == null) {
                     if (in(mx, my, x + 12, y + 18, w - 24, 18)) {
                        this.bindingModule = this.bindingModule == this.selectedModule ? null : this.selectedModule;
                        this.bindingSetting = null;
                        this.textDirty = true;
                     }
                  } else {
                     if (s.type == ClickGuiScreen.SettingType.BOOLEAN && in(mx, my, x + w - 38, y + 2, 26, 14)) {
                        s.setVal.accept(!Boolean.TRUE.equals(s.getVal.get()));
                        MacroConfig.get().save();
                        return true;
                     }

                     if (s.type == ClickGuiScreen.SettingType.KEYBIND && in(mx, my, x + 12, y + 18, w - 24, 18)) {
                        this.bindingSetting = this.bindingSetting == s ? null : s;
                        this.bindingModule = null;
                        return true;
                     }

                     if ((s.type == ClickGuiScreen.SettingType.MODE || s.type == ClickGuiScreen.SettingType.NUMBER && s.max - s.min == 1)
                        && in(mx, my, x + 12, y + 18, w - 24, 18)) {
                        if (s.type == ClickGuiScreen.SettingType.NUMBER) {
                           s.setVal.accept(s.min + (mx >= x + w / 2 ? 1 : 0));
                        } else if (s.modeOptions != null && s.modeOptions.length == 2) {
                           s.setVal.accept(mx >= x + w / 2 ? 1 : 0);
                        } else if (s.modeOptions != null && s.modeOptions.length > 0) {
                           s.setVal.accept((((Number)s.getVal.get()).intValue() + 1) % s.modeOptions.length);
                        }

                        MacroConfig.get().save();
                        return true;
                     }

                     if (s.type == ClickGuiScreen.SettingType.NUMBER && s.max - s.min != 1 && in(mx, my, x + 8, y + 18, w - 16, 16)) {
                        this.draggingSetting = s;
                        this.handleDrag(mx, my);
                        return true;
                     }
                  }
               }
            }

            return true;
         }
      } else {
         return true;
      }
   }

   private void onDrawerScrollDrag(double my) {
      int thumb = Math.max(16, 88804 / Math.max(298, this.lastDrawerTotalContentH));
      this.targetDrawerScrollOffset = Math.max(
         0.0, Math.min((double)this.maxDrawerScroll, (my - 76.0 - thumb / 2.0) / Math.max(1, 298 - thumb) * this.maxDrawerScroll)
      );
      this.drawerScrollOffset = this.targetDrawerScrollOffset;
   }

   private void clearBinding() {
      this.bindingModule = null;
      this.bindingSetting = null;
      this.draggingSetting = null;
      this.draggingVisualRatio = null;
   }

   private void selectCategory(String category) {
      this.configProfiles.leave();
      this.activeCategory = category;
      this.activeFilter = "All";
      this.setSearchActive(false);
      this.searchText = "";
      if (category.equals("Configs")) {
         this.configProfiles.open();
      }

      this.scrollOffset = this.targetScrollOffset = 0.0;
      this.selectedModule = null;
      this.clearBinding();
      this.refreshView();
   }

   private boolean handleDrag(double mx, double my) {
      if (this.draggingSetting != null && this.selectedModule != null && this.layout != null) {
         int x = this.layout.drawerX(this.drawerAnim) + 12;
         int w = 200;
         double ratio = Math.max(0.0, Math.min(1.0, (mx - x - 5.0) / (w - 10.0)));
         this.draggingVisualRatio = (float)ratio;
         int val = (int)Math.round(this.draggingSetting.min + ratio * (this.draggingSetting.max - this.draggingSetting.min));
         if (!Objects.equals(this.draggingSetting.getVal.get(), val)) {
            this.draggingSetting.setVal.accept(val);
         }

         return true;
      } else {
         return false;
      }
   }

   @Override
   public boolean onMouseClicked(int mouseX, int mouseY, int button) {
      if (!this.closing && this.layout != null) {
         double mx = this.layout.localX(mouseX);
         double my = this.layout.localY(mouseY);
         if (this.configView() && this.configProfiles.hasModal()) {
            boolean handled = this.configProfiles.click(mx, my, button);
            this.syncTextInput();
            return handled;
         } else if (this.drawerBlocks(mx, my)) {
            return this.settingsClick((int)mx, (int)my, button);
         } else if (button == 0 && in(mx, my, 12, 50, 140, 26)) {
            this.configProfiles.blur();
            this.setSearchActive(true);
            this.clearBinding();
            return true;
         } else {
            this.setSearchActive(false);
            if (button == 0) {
               for (int i = 0; i < CATEGORIES.length; i++) {
                  if (in(mx, my, 12, 106 + i * 28, 140, 24)) {
                     this.selectCategory(CATEGORIES[i]);
                     return true;
                  }
               }

               if (in(mx, my, 12, 302, 140, 24)) {
                  this.selectCategory("Configs");
                  return true;
               }

               for (ClickGuiScreen.Pill p : this.filterPills) {
                  if (in(mx, my, p.x, 13, p.width, 22)) {
                     this.activeFilter = p.label;
                     this.scrollOffset = this.targetScrollOffset = 0.0;
                     this.selectedModule = null;
                     this.clearBinding();
                     this.refreshView();
                     return true;
                  }
               }
            }

            if (this.configView()) {
               boolean handled = this.configProfiles.click(mx, my, button);
               this.syncTextInput();
               return handled;
            } else if (!in(mx, my, 176, 48, 452, 304)) {
               return false;
            } else {
               for (int ix = 0; ix < this.visible.size(); ix++) {
                  int x = 176 + ix % 2 * 230;
                  int y = 48 + ix / 2 * 52 - (int)Math.round(this.scrollOffset);
                  if ((this.drawerAnim != 1.0F || this.layout.attached || this.selectedModule == null || x + 222 <= this.layout.drawerX(this.drawerAnim))
                     && in(mx, my, x, y, 222, 44)) {
                     ClickGuiScreen.Module m = this.visible.get(ix);
                     ClickGuiScreen.TextCache t = this.texts.get(m);
                     if (button == 0
                        && t != null
                        && (t.key > 0 || this.bindingModule == m)
                        && in(mx, my, x + 222 - (this.bindingModule == m ? 18 : t.keyWidth) - 8, y + 8, this.bindingModule == m ? 18 : t.keyWidth, 16)) {
                        this.bindingModule = this.bindingModule == m ? null : m;
                        this.bindingSetting = null;
                        return true;
                     }

                     if (button == 0) {
                        m.toggle();
                        this.visuals.get(m).pressedAt = System.nanoTime();
                        MacroConfig.get().save();
                        return true;
                     }

                     if (button == 1) {
                        this.selectedModule = this.selectedModule == m ? null : m;
                        this.clearBinding();
                        if (this.selectedModule != null && this.lastContext != null) {
                           this.prepareSettings(this.lastContext, this.selectedModule);
                        }

                        this.drawerScrollOffset = this.targetDrawerScrollOffset = 0.0;
                        this.hoverModule = null;
                        return true;
                     }
                  }
               }

               return false;
            }
         }
      } else {
         return true;
      }
   }

   @Override
   public boolean onMouseReleased(int x, int y, int button) {
      this.draggingDrawerScroll = false;
      if (this.draggingSetting != null) {
         MacroConfig.get().save();
         this.draggingSetting = null;
         this.draggingVisualRatio = null;
         return true;
      } else {
         return false;
      }
   }

   @Override
   public boolean onMouseDragged(int x, int y, int button, double dx, double dy) {
      if (this.layout != null && !this.closing) {
         double mx = this.layout.localX(x);
         double my = this.layout.localY(y);
         if (this.draggingDrawerScroll && this.selectedModule != null && this.maxDrawerScroll > 0) {
            this.onDrawerScrollDrag(my);
            return true;
         } else {
            return this.handleDrag(mx, my);
         }
      } else {
         return false;
      }
   }

   @Override
   public boolean onMouseScrolled(int x, int y, double amountX, double amountY) {
      if (this.layout != null && !this.closing) {
         double mx = this.layout.localX(x);
         double my = this.layout.localY(y);
         double amount = Math.abs(amountY) > 0.01 ? amountY : amountX;
         if (this.configView()) {
            return this.configProfiles.scroll(mx, my, amount);
         } else if (this.drawerBlocks(mx, my)) {
            this.targetDrawerScrollOffset = Math.max(0.0, Math.min((double)this.maxDrawerScroll, this.targetDrawerScrollOffset - amount * 24.0));
            return true;
         } else if (in(mx, my, 176, 48, 452, 304)) {
            this.targetScrollOffset = Math.max(0.0, Math.min((double)this.maxScroll, this.targetScrollOffset - amount * 24.0));
            return true;
         } else {
            return false;
         }
      } else {
         return false;
      }
   }

   @Override
   public boolean onKeyPressed(int key, int scan, int mods) {
      if (this.closing) {
         return true;
      } else if (this.bindingModule != null) {
         this.bindingModule.setKey.accept(clearKey(key) ? -1 : key);
         this.bindingModule = null;
         this.textDirty = true;
         if (this.lastContext != null) {
            this.prepareTexts(this.lastContext);
         }

         return true;
      } else if (this.bindingSetting != null) {
         this.bindingSetting.setVal.accept(clearKey(key) ? -1 : key);
         this.bindingSetting = null;
         return true;
      } else if (key == 256) {
         if (this.searchActive || !this.searchText.isEmpty()) {
            this.setSearchActive(false);
            this.searchText = "";
            this.scrollOffset = this.targetScrollOffset = 0.0;
            this.refreshView();
            return true;
         } else if (this.configView() && this.configProfiles.key(key, mods, this.lastContext)) {
            this.syncTextInput();
            return true;
         } else if (this.selectedModule != null) {
            this.selectedModule = null;
            this.clearBinding();
            return true;
         } else {
            this.startClose();
            return true;
         }
      } else if (this.configView() && this.configProfiles.hasModal()) {
         boolean handled = this.configProfiles.key(key, mods, this.lastContext);
         this.syncTextInput();
         return handled;
      } else if (key == 70 && (mods & 2) != 0) {
         this.configProfiles.blur();
         this.setSearchActive(true);
         return true;
      } else if (this.configView() && !this.searchActive) {
         boolean handled = this.configProfiles.key(key, mods, this.lastContext);
         this.syncTextInput();
         return handled;
      } else {
         if (this.searchActive) {
            if (key == 259 && !this.searchText.isEmpty()) {
               this.searchText = this.searchText.substring(0, this.searchText.offsetByCodePoints(this.searchText.length(), -1));
               this.scrollOffset = this.targetScrollOffset = 0.0;
               this.refreshView();
               return true;
            }

            if (key == 257 || key == 335) {
               this.setSearchActive(false);
               this.refreshView();
               return true;
            }
         }

         return false;
      }
   }

   private static boolean clearKey(int key) {
      return key == 256 || key == 261 || key == 259;
   }

   @Override
   public boolean onCharTyped(char chr) {
      if (this.configView() && !this.searchActive) {
         return this.configProfiles.character(chr);
      } else if (this.searchActive && chr >= ' ' && chr != 127 && this.searchText.length() < 50) {
         this.searchText = this.searchText + chr;
         this.scrollOffset = this.targetScrollOffset = 0.0;
         this.refreshView();
         return true;
      } else {
         return false;
      }
   }

   private String getKeyName(int key) {
      return this.keyNames.computeIfAbsent(key, this::formatKeyName);
   }

   private String formatKeyName(int key) {
      if (key <= 0 || key == -1) {
         return "NONE";
      } else if (key >= 290 && key <= 314) {
         return "F" + (key - 290 + 1);
      } else {
         String name = this.nativeKeyName(key);
         if (name != null && !name.isEmpty()) {
            return name.toUpperCase();
         } else {
            return switch (key) {
               case 32 -> "SPACE";
               case 256 -> "ESC";
               case 257 -> "ENTER";
               case 258 -> "TAB";
               case 259 -> "BACK";
               case 262 -> "RIGHT";
               case 263 -> "LEFT";
               case 264 -> "DOWN";
               case 265 -> "UP";
               case 340 -> "LSHIFT";
               case 341 -> "LCTRL";
               case 342 -> "LALT";
               case 344 -> "RSHIFT";
               case 345 -> "RCTRL";
               case 346 -> "RALT";
               default -> "KEY_" + key;
            };
         }
      }
   }

   public static class Module {
      public String name;
      public String description;
      public String subcategory = "Other";
      public Supplier<Boolean> getEnabled;
      public Consumer<Boolean> setEnabled;
      public Supplier<Integer> getKey;
      public Consumer<Integer> setKey;
      public boolean dummyEnabled = false;
      public List<ClickGuiScreen.Setting> settings = new ArrayList<>();

      public Module(String name, String description) {
         this.name = name;
         this.description = description;
         this.getEnabled = () -> this.dummyEnabled;
         this.setEnabled = v -> this.dummyEnabled = v;
         MacroConfig cfg = MacroConfig.get();
         this.getKey = () -> cfg != null ? cfg.getModuleKey(name) : -1;
         this.setKey = k -> {
            if (cfg != null) {
               cfg.setModuleKey(name, k);
               cfg.save();
            }
         };
      }

      public Module(String name, String description, Supplier<Boolean> getEnabled, Consumer<Boolean> setEnabled) {
         this.name = name;
         this.description = description;
         this.getEnabled = getEnabled;
         this.setEnabled = setEnabled;
         MacroConfig cfg = MacroConfig.get();
         this.getKey = () -> cfg != null ? cfg.getModuleKey(name) : -1;
         this.setKey = k -> {
            if (cfg != null) {
               cfg.setModuleKey(name, k);
               cfg.save();
            }
         };
      }

      public boolean isEnabled() {
         return this.getEnabled != null ? this.getEnabled.get() : this.dummyEnabled;
      }

      public void toggle() {
         if (this.setEnabled != null) {
            this.setEnabled.accept(!this.isEnabled());
         } else {
            this.dummyEnabled = !this.dummyEnabled;
         }
      }

      public void addSetting(ClickGuiScreen.Setting setting) {
         this.settings.add(setting);
      }
   }

   private record NumberText(int value, String text) {
   }

   private record Pill(String label, String caption, int x, int width) {
   }

   public static class Setting {
      public String name;
      public ClickGuiScreen.SettingType type;
      public Supplier<Object> getVal;
      public Consumer<Object> setVal;
      public int min;
      public int max;
      public double displayScale = 1.0;
      public String[] modeOptions;

      public Setting(String name, ClickGuiScreen.SettingType type, Supplier<Object> getVal, Consumer<Object> setVal) {
         this.name = name;
         this.type = type;
         this.getVal = getVal;
         this.setVal = setVal;
      }

      public Setting(String name, ClickGuiScreen.SettingType type, Supplier<Object> getVal, Consumer<Object> setVal, int min, int max) {
         this(name, type, getVal, setVal);
         this.min = min;
         this.max = max;
      }

      public Setting(String name, ClickGuiScreen.SettingType type, Supplier<Object> getVal, Consumer<Object> setVal, int min, int max, double displayScale) {
         this(name, type, getVal, setVal, min, max);
         this.displayScale = displayScale;
      }

      public Setting(String name, ClickGuiScreen.SettingType type, Supplier<Object> getVal, Consumer<Object> setVal, String[] modeOptions) {
         this(name, type, getVal, setVal);
         this.modeOptions = modeOptions;
         this.min = 0;
         this.max = modeOptions != null ? modeOptions.length - 1 : 0;
      }
   }

   private record SettingRow(ClickGuiScreen.Setting setting, int y, int height) {
   }

   public static enum SettingType {
      BOOLEAN,
      NUMBER,
      KEYBIND,
      MODE;
   }

   private record TextCache(String title, String description, boolean clipped, List<String> tooltip, int key, String keyText, int keyWidth) {
   }

   private static final class Visual {
      float active;
      float hover;
      float press;
      long pressedAt;
   }
}
