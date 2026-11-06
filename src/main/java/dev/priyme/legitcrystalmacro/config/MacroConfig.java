package dev.priyme.legitcrystalmacro.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import dev.priyme.legitcrystalmacro.LegitCrystalMacroClient;
import java.io.IOException;
import java.io.Reader;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import net.fabricmc.loader.api.FabricLoader;
import org.slf4j.LoggerFactory;

public final class MacroConfig {
   private static final int MAX_DELAY_TICKS = 20;
   private static final int MIN_HOTBAR_SLOT = 1;
   private static final int MAX_HOTBAR_SLOT = 9;
   private static final int MIN_RANDOMIZATION_RANGE = 1;
   private static final int MAX_RANDOMIZATION_RANGE = 5;
   private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
   private static final ExecutorService CONFIG_IO = Executors.newSingleThreadExecutor(task -> {
      Thread thread = new Thread(task, "CPvP-Config-IO");
      thread.setDaemon(true);
      return thread;
   });
   public boolean crystalEnabled = false;
   public boolean crystalMacroEnabled = false;
   public int crystalActivateKey = 1;
   public int crystalKeyMode = 0;
   public int crystalPlaceDelay = 0;
   public int crystalBreakDelay = 0;
   public boolean crystalZeroDelay = true;
   public int crystalMinCps = 15;
   public int crystalMaxCps = 25;
   public boolean crystalStopOnKill = true;
   public int crystalStopTime = 3;
   public int crystalStopRange = 18;
   public boolean crystalPlaceObsidian = false;
   public int crystalObsidianSwitchDelay = 0;
   public double crystalObsidianCooldown = 1.0;
   public boolean crystalPlaceObsidianIfMissing = true;
   public boolean crystalTweaksEnabled = false;
   public boolean crystalTweaksZeroDelay = true;

   // ================= PRO 3.6.2 =================
   public boolean proAutoTotemEnabled = false;
   public int proAutoTotemHealth = 12;
   public int proAutoTotemDelay = 1;
   public boolean proShieldSwapEnabled = false;
   public int proShieldHealth = 14;
   public boolean proAnchorAuraEnabled = false;
   public int proAnchorAuraRange = 4;
   public int proAnchorAuraDelay = 2;
   public int proAnchorAuraSafeDistance = 3;
   public boolean proCrystalSelfSafe = true;
   public double proCrystalMinSelfDistance = 2.0;
   // =============== PRO 3.6.2 SON ===============
   public boolean anchorEnabled = false;
   public boolean anchorMacroEnabled = false;
   public int anchorActivateKey = 1;
   public int anchorKeyMode = 0;
   public int anchorCharges = 1;
   public int anchorExplodeWith = 0;
   public int anchorSwapDelay = 0;
   public int anchorChargeDelay = 0;
   public int anchorExplodeDelay = 0;
   public int anchorExplodeSlot = 0;
   public boolean anchorOnlyCharge = false;
   public boolean anchorOwnAnchors = true;
   public boolean anchorSwitchBack = true;
   public boolean anchorLootProtect = false;
   public boolean anchorWhileUse = false;
   public int anchorTotemSlot = 1;
   public boolean safeAnchorEnabled = false;
   public boolean safeAnchorAutoTarget = false;
   public int safeAnchorTargetMode = 0;
   public double safeAnchorTargetRange = 8.0;
   public double safeAnchorFov = 180.0;
   public double safeAnchorChance = 100.0;
   public int safeAnchorRotationMode = 0;
   public double safeAnchorStrength = 72.0;
   public boolean safeAnchorRaycast = true;
   public boolean safeAnchorChargeGlowstone = true;
   public int safeAnchorCharges = 1;
   public boolean safeAnchorAutoExplode = true;
   public boolean safeAnchorExplodeUnsafe = true;
   public boolean safeAnchorEnforceMinDamage = false;
   public double safeAnchorMinDamage = 6.0;
   public int safeAnchorSafeBlock = 0;
   public int safeAnchorSafetyPlacement = 0;
   public int safeAnchorSwapDelayMin = 0;
   public int safeAnchorSwapDelayMax = 0;
   public int safeAnchorPlaceDelayMin = 0;
   public int safeAnchorPlaceDelayMax = 0;
   public boolean safeAnchorSwitchBack = true;
   public boolean doubleAnchorEnabled = false;
   public int doubleAnchorActivateKey = -2;
   public int doubleAnchorSwitchDelay = 0;
   public int doubleAnchorTotemSlot = 1;
   public boolean hoverTotemEnabled = false;
   public int hoverTotemTickDelay = 0;
   public int hoverTotemDelayMs = 0;
   public boolean hoverTotemHotbar = true;
   public int hoverTotemHotbarSlot = 1;
   public boolean hoverTotemAutoSwitch = false;
   public boolean hoverTotemAutoInvOpen = false;
   public boolean hoverTotemPopRefill = true;
   public int hoverTotemHealth = 8;
   public boolean shieldBreakerEnabled = false;
   public boolean shieldBreakerAutoActivate = true;
   public int shieldBreakerAxeSlot = 1;
   public int shieldBreakerReturnSlot = 1;
   public boolean shieldBreakerAutoSwapBack = true;
   public int shieldBreakerAxeSwitchDelay = 0;
   public int shieldBreakerAttackDelay = 0;
   public int shieldBreakerWeaponSwitchDelay = 0;
   public int shieldBreakerKillDelay = 1;
   public int shieldBreakerCycleCooldown = 4;
   public boolean shieldBreakerOnlyPlayers = true;
   public boolean shieldBreakerKillSwitch = true;
   public boolean lungeMacroEnabled = false;
   public int lungeMacroKey = -1;
   public boolean lungeContinuous = false;
   public int lungeContinuousDelay = 450;
   public boolean lungeSwapBack = true;
   public int lungeSwapBackDelay = 1;
   public boolean lungeRandomize = false;
   public boolean keyPearlEnabled = false;
   public int keyPearlKey = 82;
   public int keyPearlDelay = 400;
   public boolean keyPearlSwitchBack = true;
   public boolean fastXpEnabled = false;
   public int fastXpDelay = 1;
   public int fastXpSwitchDelay = 55;
   public boolean swordPlaceObsidianEnabled = false;
   public boolean sTabSprintResetEnabled = false;
   public boolean crystalSprintReset = true;
   public boolean autoObsidianEnabled = false;
   public boolean autoObsidianFeetTrap = true;
   public boolean autoObsidianBaseRepair = true;
   public boolean aimAssistEnabled = false;
   public int aimAssistTargetMode = 0;
   public double aimAssistRange = 6.0;
   public double aimAssistFov = 15.0;
   public double aimAssistSmoothingSpeed = 0.5;
   public double aimAssistTrackingScale = 1.0;
   public double aimAssistPitchWeight = 1.0;
   public double aimAssistDeadzone = 0.1;
   public double aimAssistMicroAdjustment = 0.1;
   public boolean aimAssistOnlyWhileMoving = false;
   public boolean antiTrapEnabled = false;
   public boolean antiTrapHead = true;
   public boolean antiTrapFeet = true;
   public int antiTrapBreakDelay = 50;
   public boolean elytraSwapEnabled = false;
   public int elytraSwapMode = 0;
   public int elytraSwapKey = 73;
   public int elytraSwapEquipDelay = 1;
   public boolean elytraSwapRestoreSlot = true;
   public int elytraSwapFallDistance = 3;
   public boolean elytraSwapAutoSwapBack = true;
   public boolean triggerBotEnabled = false;
   public int triggerBotDelay = 0;
   public int triggerBotRandomDelay = 50;
   public boolean triggerBotCritOnly = false;
   public boolean triggerBotSwords = true;
   public boolean triggerBotAxes = true;
   public int triggerBotMinCps = 8;
   public int triggerBotMaxCps = 12;
   public float triggerBotCooldownThreshold = 1.0F;
   public boolean triggerBotWeaponsOnly = true;
   public int triggerBotHitChance = 100;
   public double triggerBotReach = 3.5;
   public boolean triggerBotPlayersOnly = true;
   public boolean cobwebPlaceEnabled = false;
   public int cobwebPlaceKey = -1;
   public int cobwebPlaceDelay = 200;
   public int cobwebReturnSlot = 1;
   public boolean tapResetEnabled = false;
   public boolean tapResetSTap = true;
   public boolean tapResetWTap = false;
   public int tapResetDuration = 100;
   public boolean capesOrbitEnabled = false;
   public int orbitSpeed = 5;
   public int orbitRadius = 2;
   public int orbitCount = 4;
   public boolean customCrosshairEnabled = false;
   public int crosshairStyle = 0;
   public int crosshairSize = 6;
   public int crosshairGap = 3;
   public int crosshairColor = -16711936;
   public boolean randomizeDelays = false;
   public int randomizationRange = 1;
   public boolean humanizerGaussian = true;
   public boolean sattelaPearlCatchEnabled = false;
   public boolean sattelaPearlCatchUseKeybind = false;
   public int sattelaPearlCatchKey = -1;
   public int sattelaPearlCatchDelay = 50;
   public double pearlCatchSearchRange = 32.0;
   public boolean pearlCatchHealthCheck = false;
   public boolean pearlCatchRestoreRotation = true;
   public boolean sattelaStunSlamEnabled = false;
   public boolean sattelaStunSlamAuto = true;
   public boolean sattelaStunSlamUseKeybind = false;
   public int sattelaStunSlamKey = -1;
   public int sattelaStunSlamDelay = 55;
   public double sattelaStunSlamReach = 4.2;
   public boolean sattelaStunSlamShieldOnly = false;
   public boolean sattelaStunSlamAutoReturn = true;
   public boolean sattelaBreachSwapEnabled = false;
   public boolean sattelaBreachSwapUseKeybind = false;
   public int sattelaBreachSwapKey = -1;
   public int sattelaBreachSwapDelay = 50;
   public boolean maceSwapEnabled = false;
   public int maceSwapKey = -1;
   public boolean maceSwapAuto = true;
   public double maceSwapMinFallDistance = 1.0;
   public double maceSwapReach = 4.2;
   public int maceSwapSwitchDelay = 50;
   public boolean maceSwapSwitchBack = true;
   public boolean maceSwapBreachPriority = true;
   public boolean maceSwapStunSlam = true;
   public boolean autoMaceEnabled = false;
   public double autoMaceMinFallDistance = 1.5;
   public double autoMaceReach = 4.2;
   public double autoMaceFov = 360.0;
   public int autoMaceCooldown = 200;
   public float autoMaceCooldownThreshold = 0.9F;
   public int autoMaceSwitchDelay = 50;
   public boolean autoMaceSwitchBack = true;
   public boolean spearReachEnabled = false;
   public int spearReachKey = -1;
   public boolean spearReachAuto = true;
   public double spearReachDistance = 4.2;
   public int spearReachDistanceMode = 0;
   public int spearReachDelay = 50;
   public boolean spearReachSwitchBack = true;
   public boolean keyWindChargeEnabled = false;
   public int keyWindChargeKey = -1;
   public int keyWindChargeCooldown = 500;
   public int keyWindChargeJumpDelay = 50;
   public boolean keyWindChargeRestorePitch = true;
   public boolean keyWindChargeSwitchBack = true;
   public boolean perfectWindChargeEnabled = false;
   public double perfectWindChargeTriggerRange = 2.0;
   public boolean trajectoriesEnabled = false;
   public int trajectoriesMaxTicks = 120;
   public boolean trajectoriesShowLanding = true;
   public boolean trajectoriesInFlight = true;
   public int trajectoriesColor = -932919041;
   public int trajectoriesInFlightColor = -1258315696;
   public boolean noInteractEnabled = false;
   public boolean jumpResetEnabled = false;
   public int jumpResetMode = 0;
   public boolean jumpResetPlayersOnly = true;
   public boolean jumpResetFacingOnly = true;
   public int jumpResetChance = 100;
   public int jumpResetCooldown = 10;
   public boolean hoverPotionEnabled = false;
   public int hoverPotionDelay = 75;
   public int hoverPotionTargetSlot = 2;
   public boolean noSlowEnabled = false;
   public int noSlowMode = 0;
   public boolean plateWebEnabled = false;
   public int plateWebDelay = 50;
   public int plateWebReturnSlot = 0;
   public boolean spearLungeEnabled = false;
   public int spearLungeKey = -1;
   public double spearLungeMinHunger = 6.0;
   public int spearLungeCooldown = 500;
   public double spearLungePower = 1.45;
   public double spearLungeUpwardPower = 0.25;
   public int aimAssistBoneMode = 0;
   public boolean aimAssistGcdSmoothing = true;
   public Map<String, Integer> moduleKeybinds = new HashMap<>();
   public boolean movementWindJumpEnabled = false;
   public int movementWindJumpReturnSlot = 1;
   public int movementWindJumpBoost = 5;
   public boolean movementAutoSprintEnabled = false;
   public boolean movementSpeedBridgeEnabled = false;
   public int movementSpeedBridgeDelay = 50;
   public boolean playerPanicEnabled = false;
   public int playerPanicKey = -1;
   public int playerPanicHealth = 6;
   public boolean playerNameProtectEnabled = false;
   public String playerNameProtectFakeName = "⚡「 幽霊 」⚡";
   public String playerNameProtectOriginalName = "";
   public boolean playerPingSpoofEnabled = false;
   public boolean checkUpdates = true;
   public boolean visualsStorageEspEnabled = false;
   public boolean visualsLightEspEnabled = false;
   public boolean visualsEntityEspEnabled = false;
   public boolean autoMineEnabled = false;
   public boolean shopBuyerEnabled = false;
   public boolean treeFarmerEnabled = false;

   public static MacroConfig get() {
      return LegitCrystalMacroClient.config;
   }

   public static void setInstance(MacroConfig cfg) {
      LegitCrystalMacroClient.config = cfg;
   }

   public static Path configPath() {
      try {
         if (FabricLoader.getInstance() != null && FabricLoader.getInstance().getConfigDir() != null) {
            return FabricLoader.getInstance().getConfigDir().resolve("legitcrystalmacro.json");
         }
      } catch (Throwable var1) {
      }

      return Path.of("config", "legitcrystalmacro.json");
   }

   public int getModuleKey(String moduleName) {
      if (this.moduleKeybinds == null) {
         this.moduleKeybinds = new HashMap<>();
      }

      if ("Pressure Web".equals(moduleName) && !this.moduleKeybinds.containsKey("Pressure Web") && this.moduleKeybinds.containsKey("Plate Web")) {
         return this.moduleKeybinds.get("Plate Web");
      } else {
         return "Plate Web".equals(moduleName) && !this.moduleKeybinds.containsKey("Plate Web")
            ? this.moduleKeybinds.getOrDefault("Pressure Web", -1)
            : this.moduleKeybinds.getOrDefault(moduleName, -1);
      }
   }

   public void setModuleKey(String moduleName, int key) {
      if (this.moduleKeybinds == null) {
         this.moduleKeybinds = new HashMap<>();
      }

      if ("Plate Web".equals(moduleName)) {
         moduleName = "Pressure Web";
      }

      if ("Pressure Web".equals(moduleName)) {
         this.moduleKeybinds.remove("Plate Web");
      }

      if (key > 0 && key != -1) {
         this.moduleKeybinds.put(moduleName, key);
      } else {
         this.moduleKeybinds.remove(moduleName);
      }
   }

   public static MacroConfig load() {
      Path path = configPath();
      if (!Files.exists(path)) {
         MacroConfig config = new MacroConfig();
         config.save();
         return config;
      } else {
         try {
            MacroConfig var3;
            try (Reader reader = Files.newBufferedReader(path)) {
               MacroConfig config = (MacroConfig)GSON.fromJson(reader, MacroConfig.class);
               if (config == null) {
                  config = new MacroConfig();
               }

               config.sanitize();
               var3 = config;
            }

            return var3;
         } catch (Exception var6) {
            MacroConfig config = new MacroConfig();
            config.save();
            return config;
         }
      }
   }

   public void save() {
      String json;
      try {
         json = GSON.toJson(this);
      } catch (RuntimeException var3) {
         LoggerFactory.getLogger(MacroConfig.class).error("Could not serialize configuration", var3);
         return;
      }

      Path path = configPath();
      CompletableFuture.runAsync(() -> {
         try {
            writeAtomically(path, json);
         } catch (IOException var3x) {
            LoggerFactory.getLogger(MacroConfig.class).error("Could not save configuration", var3x);
         }
      }, CONFIG_IO);
   }

   private static void writeAtomically(Path path, String json) throws IOException {
      Path absolute = path.toAbsolutePath();
      Files.createDirectories(absolute.getParent());
      Path temporary = Files.createTempFile(absolute.getParent(), absolute.getFileName().toString(), ".tmp");

      try {
         Files.writeString(temporary, json);
         Files.move(temporary, absolute, StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING);
      } finally {
         Files.deleteIfExists(temporary);
      }
   }

   public static void flushPendingSaves() throws IOException {
      awaitWrite(CONFIG_IO.submit(() -> {}));
   }

   private static void awaitWrite(Future<?> write) throws IOException {
      boolean interrupted = false;

      try {
         while (true) {
            try {
               write.get();
               return;
            } catch (InterruptedException var9) {
               interrupted = true;
            }
         }
      } catch (ExecutionException var10) {
         if (var10.getCause() instanceof IOException io) {
            throw io;
         } else {
            throw new IOException("Could not save configuration", var10.getCause());
         }
      } finally {
         if (interrupted) {
            Thread.currentThread().interrupt();
         }
      }
   }

   public void replaceAndSave(MacroConfig other) throws IOException {
      MacroConfig next = (MacroConfig)GSON.fromJson(GSON.toJson(other), MacroConfig.class);
      if (next == null) {
         throw new IOException("Invalid configuration");
      } else {
         next.sanitize();
         String previousJson = GSON.toJson(this);
         String nextJson = GSON.toJson(next);
         Path path = configPath();
         awaitWrite(CONFIG_IO.submit(() -> {
            writeAtomically(path.resolveSibling(path.getFileName() + ".bak"), previousJson);
            writeAtomically(path, nextJson);
            return null;
         }));
         this.applyFrom(next);
      }
   }

   public void copyFrom(MacroConfig other) {
      if (other != null) {
         String json = GSON.toJson(other);
         MacroConfig parsed = (MacroConfig)GSON.fromJson(json, MacroConfig.class);
         if (parsed != null) {
            parsed.sanitize();
            this.applyFrom(parsed);
         }
      }
   }

   private void applyFrom(MacroConfig o) {
      this.trajectoriesEnabled = o.trajectoriesEnabled;
      this.trajectoriesMaxTicks = o.trajectoriesMaxTicks;
      this.trajectoriesShowLanding = o.trajectoriesShowLanding;
      this.trajectoriesInFlight = o.trajectoriesInFlight;
      this.trajectoriesColor = o.trajectoriesColor;
      this.trajectoriesInFlightColor = o.trajectoriesInFlightColor;
      this.noInteractEnabled = o.noInteractEnabled;
      this.jumpResetEnabled = o.jumpResetEnabled;
      this.jumpResetMode = o.jumpResetMode;
      this.jumpResetPlayersOnly = o.jumpResetPlayersOnly;
      this.jumpResetFacingOnly = o.jumpResetFacingOnly;
      this.jumpResetChance = o.jumpResetChance;
      this.jumpResetCooldown = o.jumpResetCooldown;
      this.crystalEnabled = o.crystalEnabled;
      this.crystalMacroEnabled = o.crystalMacroEnabled;
      this.crystalActivateKey = o.crystalActivateKey;
      this.crystalKeyMode = o.crystalKeyMode;
      this.crystalPlaceDelay = o.crystalPlaceDelay;
      this.crystalBreakDelay = o.crystalBreakDelay;
      this.crystalZeroDelay = o.crystalZeroDelay;
      this.crystalMinCps = o.crystalMinCps;
      this.crystalMaxCps = o.crystalMaxCps;
      this.crystalStopOnKill = o.crystalStopOnKill;
      this.crystalStopTime = o.crystalStopTime;
      this.crystalStopRange = o.crystalStopRange;
      this.crystalPlaceObsidian = o.crystalPlaceObsidian;
      this.crystalObsidianSwitchDelay = o.crystalObsidianSwitchDelay;
      this.crystalObsidianCooldown = o.crystalObsidianCooldown;
      this.crystalPlaceObsidianIfMissing = o.crystalPlaceObsidianIfMissing;
      this.crystalTweaksEnabled = o.crystalTweaksEnabled;
      this.crystalTweaksZeroDelay = o.crystalTweaksZeroDelay;
      this.proAutoTotemEnabled = o.proAutoTotemEnabled;
      this.proAutoTotemHealth = o.proAutoTotemHealth;
      this.proAutoTotemDelay = o.proAutoTotemDelay;
      this.proShieldSwapEnabled = o.proShieldSwapEnabled;
      this.proShieldHealth = o.proShieldHealth;
      this.proAnchorAuraEnabled = o.proAnchorAuraEnabled;
      this.proAnchorAuraRange = o.proAnchorAuraRange;
      this.proAnchorAuraDelay = o.proAnchorAuraDelay;
      this.proAnchorAuraSafeDistance = o.proAnchorAuraSafeDistance;
      this.proCrystalSelfSafe = o.proCrystalSelfSafe;
      this.proCrystalMinSelfDistance = o.proCrystalMinSelfDistance;
      this.anchorEnabled = o.anchorEnabled;
      this.anchorMacroEnabled = o.anchorMacroEnabled;
      this.anchorActivateKey = o.anchorActivateKey;
      this.anchorKeyMode = o.anchorKeyMode;
      this.anchorCharges = o.anchorCharges;
      this.anchorExplodeWith = o.anchorExplodeWith;
      this.anchorSwapDelay = o.anchorSwapDelay;
      this.anchorChargeDelay = o.anchorChargeDelay;
      this.anchorExplodeDelay = o.anchorExplodeDelay;
      this.anchorExplodeSlot = o.anchorExplodeSlot;
      this.anchorOnlyCharge = o.anchorOnlyCharge;
      this.anchorOwnAnchors = o.anchorOwnAnchors;
      this.anchorSwitchBack = o.anchorSwitchBack;
      this.anchorLootProtect = o.anchorLootProtect;
      this.anchorWhileUse = o.anchorWhileUse;
      this.anchorTotemSlot = o.anchorTotemSlot;
      this.safeAnchorEnabled = o.safeAnchorEnabled;
      this.safeAnchorAutoTarget = o.safeAnchorAutoTarget;
      this.safeAnchorTargetMode = o.safeAnchorTargetMode;
      this.safeAnchorTargetRange = o.safeAnchorTargetRange;
      this.safeAnchorFov = o.safeAnchorFov;
      this.safeAnchorChance = o.safeAnchorChance;
      this.safeAnchorRotationMode = o.safeAnchorRotationMode;
      this.safeAnchorStrength = o.safeAnchorStrength;
      this.safeAnchorRaycast = o.safeAnchorRaycast;
      this.safeAnchorChargeGlowstone = o.safeAnchorChargeGlowstone;
      this.safeAnchorCharges = o.safeAnchorCharges;
      this.safeAnchorAutoExplode = o.safeAnchorAutoExplode;
      this.safeAnchorExplodeUnsafe = o.safeAnchorExplodeUnsafe;
      this.safeAnchorEnforceMinDamage = o.safeAnchorEnforceMinDamage;
      this.safeAnchorMinDamage = o.safeAnchorMinDamage;
      this.safeAnchorSafeBlock = o.safeAnchorSafeBlock;
      this.safeAnchorSafetyPlacement = o.safeAnchorSafetyPlacement;
      this.safeAnchorSwapDelayMin = o.safeAnchorSwapDelayMin;
      this.safeAnchorSwapDelayMax = o.safeAnchorSwapDelayMax;
      this.safeAnchorPlaceDelayMin = o.safeAnchorPlaceDelayMin;
      this.safeAnchorPlaceDelayMax = o.safeAnchorPlaceDelayMax;
      this.safeAnchorSwitchBack = o.safeAnchorSwitchBack;
      this.doubleAnchorEnabled = o.doubleAnchorEnabled;
      this.doubleAnchorActivateKey = o.doubleAnchorActivateKey;
      this.doubleAnchorSwitchDelay = o.doubleAnchorSwitchDelay;
      this.doubleAnchorTotemSlot = o.doubleAnchorTotemSlot;
      this.hoverTotemEnabled = o.hoverTotemEnabled;
      this.hoverTotemTickDelay = o.hoverTotemTickDelay;
      this.hoverTotemDelayMs = o.hoverTotemDelayMs;
      this.hoverTotemHotbar = o.hoverTotemHotbar;
      this.hoverTotemHotbarSlot = o.hoverTotemHotbarSlot;
      this.hoverTotemAutoSwitch = o.hoverTotemAutoSwitch;
      this.hoverTotemAutoInvOpen = o.hoverTotemAutoInvOpen;
      this.hoverTotemPopRefill = o.hoverTotemPopRefill;
      this.hoverTotemHealth = o.hoverTotemHealth;
      this.shieldBreakerEnabled = o.shieldBreakerEnabled;
      this.shieldBreakerAutoActivate = o.shieldBreakerAutoActivate;
      this.shieldBreakerAxeSlot = o.shieldBreakerAxeSlot;
      this.shieldBreakerReturnSlot = o.shieldBreakerReturnSlot;
      this.shieldBreakerAutoSwapBack = o.shieldBreakerAutoSwapBack;
      this.shieldBreakerAxeSwitchDelay = o.shieldBreakerAxeSwitchDelay;
      this.shieldBreakerAttackDelay = o.shieldBreakerAttackDelay;
      this.shieldBreakerWeaponSwitchDelay = o.shieldBreakerWeaponSwitchDelay;
      this.shieldBreakerKillDelay = o.shieldBreakerKillDelay;
      this.shieldBreakerCycleCooldown = o.shieldBreakerCycleCooldown;
      this.shieldBreakerOnlyPlayers = o.shieldBreakerOnlyPlayers;
      this.shieldBreakerKillSwitch = o.shieldBreakerKillSwitch;
      this.lungeMacroEnabled = o.lungeMacroEnabled;
      this.lungeMacroKey = o.lungeMacroKey;
      this.lungeContinuous = o.lungeContinuous;
      this.lungeContinuousDelay = o.lungeContinuousDelay;
      this.lungeSwapBack = o.lungeSwapBack;
      this.lungeSwapBackDelay = o.lungeSwapBackDelay;
      this.lungeRandomize = o.lungeRandomize;
      this.keyPearlEnabled = o.keyPearlEnabled;
      this.keyPearlKey = o.keyPearlKey;
      this.keyPearlDelay = o.keyPearlDelay;
      this.keyPearlSwitchBack = o.keyPearlSwitchBack;
      this.fastXpEnabled = o.fastXpEnabled;
      this.fastXpDelay = o.fastXpDelay;
      this.fastXpSwitchDelay = o.fastXpSwitchDelay;
      this.swordPlaceObsidianEnabled = o.swordPlaceObsidianEnabled;
      this.sTabSprintResetEnabled = o.sTabSprintResetEnabled;
      this.crystalSprintReset = o.crystalSprintReset;
      this.autoObsidianEnabled = o.autoObsidianEnabled;
      this.autoObsidianFeetTrap = o.autoObsidianFeetTrap;
      this.autoObsidianBaseRepair = o.autoObsidianBaseRepair;
      this.aimAssistEnabled = o.aimAssistEnabled;
      this.aimAssistTargetMode = o.aimAssistTargetMode;
      this.aimAssistRange = o.aimAssistRange;
      this.aimAssistFov = o.aimAssistFov;
      this.aimAssistSmoothingSpeed = o.aimAssistSmoothingSpeed;
      this.aimAssistTrackingScale = o.aimAssistTrackingScale;
      this.aimAssistPitchWeight = o.aimAssistPitchWeight;
      this.aimAssistDeadzone = o.aimAssistDeadzone;
      this.aimAssistMicroAdjustment = o.aimAssistMicroAdjustment;
      this.aimAssistOnlyWhileMoving = o.aimAssistOnlyWhileMoving;
      this.antiTrapEnabled = o.antiTrapEnabled;
      this.antiTrapHead = o.antiTrapHead;
      this.antiTrapFeet = o.antiTrapFeet;
      this.antiTrapBreakDelay = o.antiTrapBreakDelay;
      this.elytraSwapEnabled = o.elytraSwapEnabled;
      this.elytraSwapMode = o.elytraSwapMode;
      this.elytraSwapKey = o.elytraSwapKey;
      this.elytraSwapEquipDelay = o.elytraSwapEquipDelay;
      this.elytraSwapRestoreSlot = o.elytraSwapRestoreSlot;
      this.elytraSwapFallDistance = o.elytraSwapFallDistance;
      this.elytraSwapAutoSwapBack = o.elytraSwapAutoSwapBack;
      this.triggerBotEnabled = o.triggerBotEnabled;
      this.triggerBotDelay = o.triggerBotDelay;
      this.triggerBotRandomDelay = o.triggerBotRandomDelay;
      this.triggerBotCritOnly = o.triggerBotCritOnly;
      this.triggerBotSwords = o.triggerBotSwords;
      this.triggerBotAxes = o.triggerBotAxes;
      this.triggerBotMinCps = o.triggerBotMinCps;
      this.triggerBotMaxCps = o.triggerBotMaxCps;
      this.triggerBotCooldownThreshold = o.triggerBotCooldownThreshold;
      this.triggerBotWeaponsOnly = o.triggerBotWeaponsOnly;
      this.triggerBotHitChance = o.triggerBotHitChance;
      this.triggerBotReach = o.triggerBotReach;
      this.triggerBotPlayersOnly = o.triggerBotPlayersOnly;
      this.cobwebPlaceEnabled = o.cobwebPlaceEnabled;
      this.cobwebPlaceKey = o.cobwebPlaceKey;
      this.cobwebPlaceDelay = o.cobwebPlaceDelay;
      this.cobwebReturnSlot = o.cobwebReturnSlot;
      this.tapResetEnabled = o.tapResetEnabled;
      this.tapResetSTap = o.tapResetSTap;
      this.tapResetWTap = o.tapResetWTap;
      this.tapResetDuration = o.tapResetDuration;
      this.capesOrbitEnabled = o.capesOrbitEnabled;
      this.orbitSpeed = o.orbitSpeed;
      this.orbitRadius = o.orbitRadius;
      this.orbitCount = o.orbitCount;
      this.customCrosshairEnabled = o.customCrosshairEnabled;
      this.crosshairStyle = o.crosshairStyle;
      this.crosshairSize = o.crosshairSize;
      this.crosshairGap = o.crosshairGap;
      this.crosshairColor = o.crosshairColor;
      this.randomizeDelays = o.randomizeDelays;
      this.randomizationRange = o.randomizationRange;
      this.humanizerGaussian = o.humanizerGaussian;
      this.sattelaPearlCatchEnabled = o.sattelaPearlCatchEnabled;
      this.sattelaPearlCatchUseKeybind = o.sattelaPearlCatchUseKeybind;
      this.sattelaPearlCatchKey = o.sattelaPearlCatchKey;
      this.sattelaPearlCatchDelay = o.sattelaPearlCatchDelay;
      this.pearlCatchSearchRange = o.pearlCatchSearchRange;
      this.pearlCatchHealthCheck = o.pearlCatchHealthCheck;
      this.pearlCatchRestoreRotation = o.pearlCatchRestoreRotation;
      this.sattelaStunSlamEnabled = o.sattelaStunSlamEnabled;
      this.sattelaStunSlamAuto = o.sattelaStunSlamAuto;
      this.sattelaStunSlamUseKeybind = o.sattelaStunSlamUseKeybind;
      this.sattelaStunSlamKey = o.sattelaStunSlamKey;
      this.sattelaStunSlamDelay = o.sattelaStunSlamDelay;
      this.sattelaStunSlamReach = o.sattelaStunSlamReach;
      this.sattelaStunSlamShieldOnly = o.sattelaStunSlamShieldOnly;
      this.sattelaStunSlamAutoReturn = o.sattelaStunSlamAutoReturn;
      this.sattelaBreachSwapEnabled = o.sattelaBreachSwapEnabled;
      this.sattelaBreachSwapUseKeybind = o.sattelaBreachSwapUseKeybind;
      this.sattelaBreachSwapKey = o.sattelaBreachSwapKey;
      this.sattelaBreachSwapDelay = o.sattelaBreachSwapDelay;
      this.maceSwapEnabled = o.maceSwapEnabled;
      this.maceSwapKey = o.maceSwapKey;
      this.maceSwapAuto = o.maceSwapAuto;
      this.maceSwapMinFallDistance = o.maceSwapMinFallDistance;
      this.maceSwapReach = o.maceSwapReach;
      this.maceSwapSwitchDelay = o.maceSwapSwitchDelay;
      this.maceSwapSwitchBack = o.maceSwapSwitchBack;
      this.maceSwapBreachPriority = o.maceSwapBreachPriority;
      this.maceSwapStunSlam = o.maceSwapStunSlam;
      this.autoMaceEnabled = o.autoMaceEnabled;
      this.autoMaceMinFallDistance = o.autoMaceMinFallDistance;
      this.autoMaceReach = o.autoMaceReach;
      this.autoMaceFov = o.autoMaceFov;
      this.autoMaceCooldown = o.autoMaceCooldown;
      this.autoMaceCooldownThreshold = o.autoMaceCooldownThreshold;
      this.autoMaceSwitchDelay = o.autoMaceSwitchDelay;
      this.autoMaceSwitchBack = o.autoMaceSwitchBack;
      this.spearReachEnabled = o.spearReachEnabled;
      this.spearReachKey = o.spearReachKey;
      this.spearReachAuto = o.spearReachAuto;
      this.spearReachDistance = o.spearReachDistance;
      this.spearReachDistanceMode = o.spearReachDistanceMode;
      this.spearReachDelay = o.spearReachDelay;
      this.spearReachSwitchBack = o.spearReachSwitchBack;
      this.keyWindChargeEnabled = o.keyWindChargeEnabled;
      this.keyWindChargeKey = o.keyWindChargeKey;
      this.keyWindChargeCooldown = o.keyWindChargeCooldown;
      this.keyWindChargeJumpDelay = o.keyWindChargeJumpDelay;
      this.keyWindChargeRestorePitch = o.keyWindChargeRestorePitch;
      this.keyWindChargeSwitchBack = o.keyWindChargeSwitchBack;
      this.perfectWindChargeEnabled = o.perfectWindChargeEnabled;
      this.perfectWindChargeTriggerRange = o.perfectWindChargeTriggerRange;
      if (o.moduleKeybinds != null) {
         this.moduleKeybinds = new HashMap<>(o.moduleKeybinds);
      }

      this.movementWindJumpEnabled = o.movementWindJumpEnabled;
      this.movementWindJumpReturnSlot = o.movementWindJumpReturnSlot;
      this.movementWindJumpBoost = o.movementWindJumpBoost;
      this.movementAutoSprintEnabled = o.movementAutoSprintEnabled;
      this.movementSpeedBridgeEnabled = o.movementSpeedBridgeEnabled;
      this.movementSpeedBridgeDelay = o.movementSpeedBridgeDelay;
      this.playerPanicEnabled = o.playerPanicEnabled;
      this.playerPanicKey = o.playerPanicKey;
      this.playerPanicHealth = o.playerPanicHealth;
      this.playerNameProtectEnabled = o.playerNameProtectEnabled;
      this.playerNameProtectFakeName = o.playerNameProtectFakeName;
      this.playerNameProtectOriginalName = o.playerNameProtectOriginalName;
      this.playerPingSpoofEnabled = o.playerPingSpoofEnabled;
      this.checkUpdates = o.checkUpdates;
      this.visualsStorageEspEnabled = o.visualsStorageEspEnabled;
      this.visualsLightEspEnabled = o.visualsLightEspEnabled;
      this.visualsEntityEspEnabled = o.visualsEntityEspEnabled;
      this.autoMineEnabled = o.autoMineEnabled;
      this.shopBuyerEnabled = o.shopBuyerEnabled;
      this.treeFarmerEnabled = o.treeFarmerEnabled;
      this.hoverPotionEnabled = o.hoverPotionEnabled;
      this.hoverPotionDelay = o.hoverPotionDelay;
      this.hoverPotionTargetSlot = o.hoverPotionTargetSlot;
      this.noSlowEnabled = o.noSlowEnabled;
      this.noSlowMode = o.noSlowMode;
      this.plateWebEnabled = o.plateWebEnabled;
      this.plateWebDelay = o.plateWebDelay;
      this.plateWebReturnSlot = o.plateWebReturnSlot;
      this.spearLungeEnabled = o.spearLungeEnabled;
      this.spearLungeKey = o.spearLungeKey;
      this.spearLungeMinHunger = o.spearLungeMinHunger;
      this.spearLungeCooldown = o.spearLungeCooldown;
      this.spearLungePower = o.spearLungePower;
      this.spearLungeUpwardPower = o.spearLungeUpwardPower;
      this.aimAssistBoneMode = o.aimAssistBoneMode;
      this.aimAssistGcdSmoothing = o.aimAssistGcdSmoothing;
   }

   public void resetToDefaults() {
      try {
         this.replaceAndSave(new MacroConfig());
      } catch (IOException var2) {
         throw new UncheckedIOException(var2);
      }
   }

   public void sanitize() {
      if (this.moduleKeybinds != null && this.moduleKeybinds.containsKey("Plate Web")) {
         Integer legacyKey = this.moduleKeybinds.remove("Plate Web");
         if (legacyKey != null && !this.moduleKeybinds.containsKey("Pressure Web")) {
            this.moduleKeybinds.put("Pressure Web", legacyKey);
         }
      }

      this.trajectoriesMaxTicks = clamp(this.trajectoriesMaxTicks, 30, 300);
      this.jumpResetMode = clamp(this.jumpResetMode, 0, 1);
      this.jumpResetChance = clamp(this.jumpResetChance, 0, 100);
      this.jumpResetCooldown = clamp(this.jumpResetCooldown, 0, 20);
      this.crystalPlaceDelay = clamp(this.crystalPlaceDelay, 0, 1000);
      this.crystalBreakDelay = clamp(this.crystalBreakDelay, 0, 1000);
      this.crystalMinCps = clamp(this.crystalMinCps, 1, 50);
      this.crystalMaxCps = clamp(this.crystalMaxCps, 1, 50);
      this.crystalStopTime = clamp(this.crystalStopTime, 0, 15);
      this.crystalStopRange = clamp(this.crystalStopRange, 4, 48);
      this.crystalObsidianSwitchDelay = clamp(this.crystalObsidianSwitchDelay, 0, 20);
      this.proAutoTotemHealth = clamp(this.proAutoTotemHealth, 1, 20);
      this.proAutoTotemDelay = clamp(this.proAutoTotemDelay, 0, 20);
      this.proShieldHealth = clamp(this.proShieldHealth, 1, 20);
      this.proAnchorAuraRange = clamp(this.proAnchorAuraRange, 1, 6);
      this.proAnchorAuraDelay = clamp(this.proAnchorAuraDelay, 0, 20);
      this.proAnchorAuraSafeDistance = clamp(this.proAnchorAuraSafeDistance, 0, 8);
      if (this.proCrystalMinSelfDistance < 0.0) {
         this.proCrystalMinSelfDistance = 0.0;
      }
      if (this.proCrystalMinSelfDistance > 8.0) {
         this.proCrystalMinSelfDistance = 8.0;
      }
      if (this.crystalObsidianCooldown < 0.0) {
         this.crystalObsidianCooldown = 0.0;
      }

      if (this.crystalObsidianCooldown > 10.0) {
         this.crystalObsidianCooldown = 10.0;
      }

      this.anchorExplodeDelay = clamp(this.anchorExplodeDelay, 0, 1000);
      this.anchorTotemSlot = clamp(this.anchorTotemSlot, 1, 9);
      this.doubleAnchorSwitchDelay = clamp(this.doubleAnchorSwitchDelay, 0, 20);
      this.doubleAnchorTotemSlot = clamp(this.doubleAnchorTotemSlot, 1, 9);
      this.hoverTotemTickDelay = clamp(this.hoverTotemTickDelay, 0, 20);
      this.hoverTotemDelayMs = clamp(this.hoverTotemDelayMs, 0, 500);
      this.hoverTotemHotbarSlot = clamp(this.hoverTotemHotbarSlot, 1, 9);
      this.randomizationRange = clamp(this.randomizationRange, 1, 5);
      this.sattelaStunSlamDelay = clamp(this.sattelaStunSlamDelay, 0, 500);
      if (this.sattelaStunSlamReach < 2.0) {
         this.sattelaStunSlamReach = 2.0;
      }

      if (this.sattelaStunSlamReach > 6.0) {
         this.sattelaStunSlamReach = 6.0;
      }

      this.sattelaBreachSwapDelay = clamp(this.sattelaBreachSwapDelay, 0, 500);
      this.shieldBreakerAxeSlot = clamp(this.shieldBreakerAxeSlot, 1, 9);
      this.shieldBreakerReturnSlot = clamp(this.shieldBreakerReturnSlot, 1, 9);
      this.cobwebReturnSlot = clamp(this.cobwebReturnSlot, 1, 9);
      this.fastXpDelay = clamp(this.fastXpDelay, 1, 4);
      this.fastXpSwitchDelay = clamp(this.fastXpSwitchDelay, 0, 300);
      this.triggerBotDelay = clamp(this.triggerBotDelay, 0, 1000);
      this.movementSpeedBridgeDelay = clamp(this.movementSpeedBridgeDelay, 10, 200);
      this.tapResetSTap = !this.tapResetWTap;
      this.tapResetDuration = clamp(this.tapResetDuration, 20, 500);
      this.aimAssistTargetMode = clamp(this.aimAssistTargetMode, 0, 2);
      if (this.aimAssistRange < 2.0) {
         this.aimAssistRange = 2.0;
      }

      if (this.aimAssistRange > 12.0) {
         this.aimAssistRange = 12.0;
      }

      if (this.aimAssistFov < 1.0) {
         this.aimAssistFov = 1.0;
      }

      if (this.aimAssistFov > 35.0) {
         this.aimAssistFov = 35.0;
      }

      if (this.aimAssistSmoothingSpeed < 0.1) {
         this.aimAssistSmoothingSpeed = 0.1;
      }

      if (this.aimAssistSmoothingSpeed > 1.0) {
         this.aimAssistSmoothingSpeed = 1.0;
      }

      if (this.aimAssistTrackingScale < 0.5) {
         this.aimAssistTrackingScale = 0.5;
      }

      if (this.aimAssistTrackingScale > 1.5) {
         this.aimAssistTrackingScale = 1.5;
      }

      if (this.aimAssistPitchWeight < 0.5) {
         this.aimAssistPitchWeight = 0.5;
      }

      if (this.aimAssistPitchWeight > 1.5) {
         this.aimAssistPitchWeight = 1.5;
      }

      if (this.aimAssistDeadzone < 0.0) {
         this.aimAssistDeadzone = 0.0;
      }

      if (this.aimAssistDeadzone > 0.5) {
         this.aimAssistDeadzone = 0.5;
      }

      if (this.aimAssistMicroAdjustment < 0.05) {
         this.aimAssistMicroAdjustment = 0.05;
      }

      if (this.aimAssistMicroAdjustment > 0.6) {
         this.aimAssistMicroAdjustment = 0.6;
      }

      this.movementWindJumpReturnSlot = clamp(this.movementWindJumpReturnSlot, 1, 9);
      this.elytraSwapEquipDelay = clamp(this.elytraSwapEquipDelay, 0, 20);
      this.elytraSwapMode = clamp(this.elytraSwapMode, 0, 2);
      this.crystalKeyMode = clamp(this.crystalKeyMode, 0, 2);
      this.anchorKeyMode = clamp(this.anchorKeyMode, 0, 2);
      this.anchorCharges = clamp(this.anchorCharges, 1, 4);
      this.anchorExplodeWith = clamp(this.anchorExplodeWith, 0, 2);
      this.anchorSwapDelay = clamp(this.anchorSwapDelay, 0, 300);
      this.anchorChargeDelay = clamp(this.anchorChargeDelay, 0, 300);
      this.anchorExplodeDelay = clamp(this.anchorExplodeDelay, 0, 300);
      this.anchorExplodeSlot = clamp(this.anchorExplodeSlot, 0, 9);
      this.safeAnchorTargetMode = clamp(this.safeAnchorTargetMode, 0, 2);
      this.safeAnchorRotationMode = clamp(this.safeAnchorRotationMode, 0, 1);
      this.safeAnchorSafeBlock = clamp(this.safeAnchorSafeBlock, 0, 2);
      this.safeAnchorSafetyPlacement = clamp(this.safeAnchorSafetyPlacement, 0, 1);
      this.safeAnchorCharges = clamp(this.safeAnchorCharges, 1, 4);
      this.safeAnchorTargetRange = Math.max(2.0, Math.min(15.0, this.safeAnchorTargetRange));
      this.safeAnchorFov = Math.max(10.0, Math.min(180.0, this.safeAnchorFov));
      this.safeAnchorChance = Math.max(0.0, Math.min(100.0, this.safeAnchorChance));
      this.safeAnchorStrength = Math.max(1.0, Math.min(100.0, this.safeAnchorStrength));
      this.safeAnchorMinDamage = Math.max(0.5, Math.min(20.0, this.safeAnchorMinDamage));
      this.safeAnchorSwapDelayMin = clamp(this.safeAnchorSwapDelayMin, 0, 300);
      this.safeAnchorSwapDelayMax = clamp(this.safeAnchorSwapDelayMax, this.safeAnchorSwapDelayMin, 300);
      this.safeAnchorPlaceDelayMin = clamp(this.safeAnchorPlaceDelayMin, 0, 500);
      this.safeAnchorPlaceDelayMax = clamp(this.safeAnchorPlaceDelayMax, this.safeAnchorPlaceDelayMin, 500);
      this.triggerBotMinCps = clamp(this.triggerBotMinCps, 1, 20);
      this.triggerBotMaxCps = clamp(this.triggerBotMaxCps, 1, 20);
      this.triggerBotHitChance = clamp(this.triggerBotHitChance, 1, 100);
      if (this.triggerBotReach < 1.0) {
         this.triggerBotReach = 1.0;
      }

      if (this.triggerBotReach > 6.0) {
         this.triggerBotReach = 6.0;
      }

      this.maceSwapSwitchDelay = clamp(this.maceSwapSwitchDelay, 0, 1000);
      if (this.maceSwapReach < 1.0) {
         this.maceSwapReach = 1.0;
      }

      if (this.maceSwapReach > 6.0) {
         this.maceSwapReach = 6.0;
      }

      this.autoMaceSwitchDelay = clamp(this.autoMaceSwitchDelay, 0, 1000);
      this.autoMaceCooldown = clamp(this.autoMaceCooldown, 0, 2000);
      if (this.autoMaceReach < 1.0) {
         this.autoMaceReach = 1.0;
      }

      if (this.autoMaceReach > 6.0) {
         this.autoMaceReach = 6.0;
      }

      if (this.autoMaceFov < 10.0) {
         this.autoMaceFov = 10.0;
      }

      if (this.autoMaceFov > 360.0) {
         this.autoMaceFov = 360.0;
      }

      this.spearReachDelay = clamp(this.spearReachDelay, 0, 1000);
      this.spearReachDistanceMode = clamp(this.spearReachDistanceMode, 0, 2);
      if (this.spearReachDistance < 1.0) {
         this.spearReachDistance = 1.0;
      }

      if (this.spearReachDistance > 7.0) {
         this.spearReachDistance = 7.0;
      }

      this.keyWindChargeCooldown = clamp(this.keyWindChargeCooldown, 0, 2000);
      this.keyWindChargeJumpDelay = clamp(this.keyWindChargeJumpDelay, 0, 500);
      if (this.perfectWindChargeTriggerRange < 0.5) {
         this.perfectWindChargeTriggerRange = 0.5;
      }

      if (this.perfectWindChargeTriggerRange > 5.0) {
         this.perfectWindChargeTriggerRange = 5.0;
      }

      this.hoverPotionDelay = clamp(this.hoverPotionDelay, 0, 500);
      this.hoverPotionTargetSlot = clamp(this.hoverPotionTargetSlot, 1, 9);
      this.noSlowMode = clamp(this.noSlowMode, 0, 1);
      this.plateWebDelay = clamp(this.plateWebDelay, 0, 500);
      this.plateWebReturnSlot = clamp(this.plateWebReturnSlot, 0, 9);
      this.spearLungeMinHunger = Math.max(0.0, Math.min(20.0, this.spearLungeMinHunger));
      this.spearLungeCooldown = clamp(this.spearLungeCooldown, 0, 5000);
      this.spearLungePower = Math.max(0.1, Math.min(5.0, this.spearLungePower));
      this.spearLungeUpwardPower = Math.max(0.0, Math.min(2.0, this.spearLungeUpwardPower));
      this.aimAssistBoneMode = clamp(this.aimAssistBoneMode, 0, 2);
   }

   static int clamp(int value, int min, int max) {
      return Math.max(min, Math.min(max, value));
   }

   static {
      Runtime.getRuntime().addShutdownHook(new Thread(() -> {
         try {
            flushPendingSaves();
         } catch (IOException var1) {
            LoggerFactory.getLogger(MacroConfig.class).error("Could not flush configuration", var1);
         }
      }, "CPvP-Config-Shutdown"));
   }
}
