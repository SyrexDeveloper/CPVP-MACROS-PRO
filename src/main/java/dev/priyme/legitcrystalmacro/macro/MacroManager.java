package dev.priyme.legitcrystalmacro.macro;

import dev.priyme.legitcrystalmacro.config.MacroConfig;
import dev.priyme.legitcrystalmacro.macro.pro.AnchorAuraPro;
import dev.priyme.legitcrystalmacro.macro.pro.AutoTotemPro;
import dev.priyme.legitcrystalmacro.macro.pro.ShieldSwapPro;
import dev.priyme.legitcrystalmacro.macro.sattela.BreachSwapMacro;
import dev.priyme.legitcrystalmacro.macro.sattela.PearlCatchMacro;
import dev.priyme.legitcrystalmacro.macro.sattela.StunSlamMacro;
import dev.priyme.legitcrystalmacro.util.FallDistanceTracker;
import dev.priyme.legitcrystalmacro.util.KeyInput;
import java.util.HashSet;
import java.util.Set;
import java.util.Map.Entry;
import net.minecraft.client.MinecraftClient;

public final class MacroManager {
   private final Set<Integer> pressedKeys = new HashSet<>();

   public void tick(MinecraftClient client, MacroConfig config) {
      if (client.player != null && client.world != null && client.interactionManager != null) {
         if (KeyInput.isScreenNull(client) && config != null && config.moduleKeybinds != null) {
            this.checkKeybinds(client, config);
         }

         FallDistanceTracker.update(
            client.player,
            client.player.getY(),
            client.player.getVelocity().y,
            client.player.isOnGround()
               || client.player.getAbilities().flying
               || client.player.isTouchingWater()
               || client.player.isClimbing()
               || client.player.hasVehicle()
         );
         CrystalMacro.tick(client, config);
         AnchorMacro.tick(client, config);
         DoubleAnchorMacro.tick(client, config);
         SafeAnchorMacro.tick(client, config);
         HoverTotem.tick(client, config);
         HoverPotion.tick(client, config);
         CobwebPlace.tick(client, config);
         PlateWeb.tick(client, config);
         CrystalTweaks.tick(client, config);
         AimAssist.tick(client, config);
         TriggerBot.tick(client, config);
         TapReset.tick(client, config);
         KeyPearl.tick(client, config);
         PearlCatchMacro.tick(client, config);
         StunSlamMacro.tick(client, config);
         BreachSwapMacro.tick(client, config);
         MaceSwapMacro.tick(client, config);
         AutoMaceMacro.tick(client, config);
         SpearReachMacro.tick(client, config);
         WindChargeMacro.tick(client, config);
         SpeedBridge.tick(client, config);
         PanicDisconnectMacro.tick(client, config);
         FastXP.tick(client, config);
         ShieldBreaker.tick(client, config);
         LungeMacro.tick(client, config);
         SpearLunge.tick(client, config);
         // PRO 3.6.2 modulleri
         AutoTotemPro.tick(client, config);
         ShieldSwapPro.tick(client, config);
         AnchorAuraPro.tick(client, config);
         if (config.movementAutoSprintEnabled && !TapReset.isResetting() && !client.player.isSprinting() && client.player.input.hasForwardMovement()) {
            client.player.setSprinting(true);
         }
      } else {
         SpeedBridge.reset(client);
         SafeAnchorMacro.reset();
         JumpReset.reset();
         TapReset.reset();
         FallDistanceTracker.reset();
         this.pressedKeys.clear();
         HoverPotion.reset();
         PlateWeb.reset();
         SpearLunge.reset();
         NoSlow.reset();
         AutoTotemPro.reset();
         ShieldSwapPro.reset();
         AnchorAuraPro.reset();
      }
   }

   private void checkKeybinds(MinecraftClient client, MacroConfig config) {
      Set<Integer> down = new HashSet<>();

      for (int key : config.moduleKeybinds.values()) {
         if (key > 0 && KeyInput.isPressed(client, key)) {
            down.add(key);
         }
      }

      boolean changed = false;

      for (Entry<String, Integer> entry : config.moduleKeybinds.entrySet()) {
         if (down.contains(entry.getValue()) && !this.pressedKeys.contains(entry.getValue())) {
            toggleModule(config, entry.getKey());
            changed = true;
         }
      }

      this.pressedKeys.clear();
      this.pressedKeys.addAll(down);
      if (changed) {
         config.save();
      }
   }

   public static void toggleModule(MacroConfig config, String name) {
      if (name != null && config != null) {
         switch (name) {
            case "Trajectories":
               config.trajectoriesEnabled = !config.trajectoriesEnabled;
               break;
            case "NoInteract":
               config.noInteractEnabled = !config.noInteractEnabled;
               break;
            case "Jump Reset":
               config.jumpResetEnabled = !config.jumpResetEnabled;
               break;
            case "Crystal Macro":
               config.crystalEnabled = !config.crystalEnabled;
               break;
            case "Auto Totem Pro":
               config.proAutoTotemEnabled = !config.proAutoTotemEnabled;
               break;
            case "Shield Swap Pro":
               config.proShieldSwapEnabled = !config.proShieldSwapEnabled;
               break;
            case "Anchor Aura Pro":
               config.proAnchorAuraEnabled = !config.proAnchorAuraEnabled;
               break;
            case "Anchor Macro":
               config.anchorEnabled = !config.anchorEnabled;
               break;
            case "Double Anchor":
               config.doubleAnchorEnabled = !config.doubleAnchorEnabled;
               break;
            case "Safe Anchor":
               config.safeAnchorEnabled = !config.safeAnchorEnabled;
               break;
            case "Hover Totem":
               config.hoverTotemEnabled = !config.hoverTotemEnabled;
               break;
            case "Cobweb Placer":
               config.cobwebPlaceEnabled = !config.cobwebPlaceEnabled;
               break;
            case "Crystal Tweaks":
               config.crystalTweaksEnabled = !config.crystalTweaksEnabled;
               break;
            case "STab Sprint Reset":
               config.sTabSprintResetEnabled = !config.sTabSprintResetEnabled;
               break;
            case "Tap Reset":
               config.tapResetEnabled = !config.tapResetEnabled;
               break;
            case "Aim Assist":
               config.aimAssistEnabled = !config.aimAssistEnabled;
               break;
            case "TriggerBot":
               config.triggerBotEnabled = !config.triggerBotEnabled;
               break;
            case "Key Pearl":
               config.keyPearlEnabled = !config.keyPearlEnabled;
               break;
            case "Pearl Catch":
               config.sattelaPearlCatchEnabled = !config.sattelaPearlCatchEnabled;
               break;
            case "Stun Slam":
               config.sattelaStunSlamEnabled = !config.sattelaStunSlamEnabled;
               break;
            case "Breach Swap":
               config.sattelaBreachSwapEnabled = !config.sattelaBreachSwapEnabled;
               break;
            case "Mace Swap":
               config.maceSwapEnabled = !config.maceSwapEnabled;
               break;
            case "Auto Mace":
               config.autoMaceEnabled = !config.autoMaceEnabled;
               break;
            case "Spear Reach":
               config.spearReachEnabled = !config.spearReachEnabled;
               break;
            case "Key WindCharge":
               config.keyWindChargeEnabled = !config.keyWindChargeEnabled;
               break;
            case "Perfect Windcharge":
               config.perfectWindChargeEnabled = !config.perfectWindChargeEnabled;
               break;
            case "Lunge Macro":
               config.lungeMacroEnabled = !config.lungeMacroEnabled;
               break;
            case "Auto Sprint":
               config.movementAutoSprintEnabled = !config.movementAutoSprintEnabled;
               break;
            case "Speed Bridge":
               config.movementSpeedBridgeEnabled = !config.movementSpeedBridgeEnabled;
               break;
            case "Panic Disconnect":
               config.playerPanicEnabled = !config.playerPanicEnabled;
               break;
            case "Name Protect":
               config.playerNameProtectEnabled = !config.playerNameProtectEnabled;
               break;
            case "Fast XP":
               config.fastXpEnabled = !config.fastXpEnabled;
               break;
            case "Shield Breaker":
               config.shieldBreakerEnabled = !config.shieldBreakerEnabled;
               break;
            case "Delay Randomizer":
               config.randomizeDelays = !config.randomizeDelays;
               break;
            case "Hover Potion":
               config.hoverPotionEnabled = !config.hoverPotionEnabled;
               break;
            case "NoSlow":
               config.noSlowEnabled = !config.noSlowEnabled;
               break;
            case "Pressure Web":
            case "Plate Web":
               config.plateWebEnabled = !config.plateWebEnabled;
               break;
            case "Spear Lunge":
               config.spearLungeEnabled = !config.spearLungeEnabled;
         }
      }
   }
}
