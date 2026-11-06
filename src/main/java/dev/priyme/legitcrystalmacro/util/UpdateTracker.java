package dev.priyme.legitcrystalmacro.util;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import dev.priyme.legitcrystalmacro.LegitCrystalMacroClient;
import dev.priyme.legitcrystalmacro.config.MacroConfig;
import dev.priyme.legitcrystalmacro.gui.UpdateNotificationScreen;
import java.awt.Desktop;
import java.awt.Desktop.Action;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.CompletableFuture;

public final class UpdateTracker {
   public static final String UPDATE_API_URL = "https://api.modrinth.com/v2/project/cpvp-macros/version";
   public static final String MODRINTH_URL = "https://modrinth.com/mod/cpvp-macros";
   private static volatile String latestVersion = null;
   private static volatile boolean checked = false;
   private static volatile boolean updateAvailable = false;
   private static volatile boolean checkFailed = false;
   private static volatile boolean startupNotificationShown = false;

   private UpdateTracker() {
   }

   public static void checkForUpdates() {
      CompletableFuture.runAsync(() -> {
         try {
            URL url = new URI("https://api.modrinth.com/v2/project/cpvp-macros/version").toURL();
            HttpURLConnection conn = (HttpURLConnection)url.openConnection();
            conn.setRequestMethod("GET");
            conn.setConnectTimeout(4000);
            conn.setReadTimeout(4000);
            conn.setRequestProperty("User-Agent", "CPvP-Macros/3.6");
            if (conn.getResponseCode() == 200) {
               try (BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8))) {
                  JsonArray array = JsonParser.parseReader(reader).getAsJsonArray();
                  if (array != null && array.size() > 0) {
                     String highest = null;

                     for (int i = 0; i < array.size(); i++) {
                        JsonObject obj = array.get(i).getAsJsonObject();
                        if (obj.has("version_number")) {
                           String vNum = obj.get("version_number").getAsString();
                           if (highest == null || isNewerVersion(highest, vNum)) {
                              highest = vNum;
                           }
                        }
                     }

                     if (highest != null) {
                        latestVersion = highest;
                        updateAvailable = isNewerVersion("3.6", highest);
                     }
                  }
               }
            } else {
               checkFailed = true;
            }
         } catch (Throwable var15) {
            checkFailed = true;
         } finally {
            if (Boolean.getBoolean("cpvp.testUpdate")) {
               updateAvailable = true;
               if (latestVersion == null) {
                  latestVersion = "3.7";
               }
            }

            checked = true;
         }
      });
   }

   public static boolean isChecked() {
      return checked;
   }

   public static boolean isCheckFailed() {
      return checkFailed;
   }

   public static boolean isUpdateAvailable() {
      return checked && updateAvailable || Boolean.getBoolean("cpvp.testUpdate");
   }

   public static String getLatestVersion() {
      return latestVersion != null ? latestVersion : "3.6";
   }

   public static boolean hasShownStartup() {
      return startupNotificationShown;
   }

   public static void markStartupShown() {
      startupNotificationShown = true;
   }

   public static void handleTitleScreenTick(Object clientObj, Object currentScreen) {
      if (!startupNotificationShown) {
         MacroConfig cfg = LegitCrystalMacroClient.config;
         if (cfg == null || cfg.checkUpdates) {
            if (checked) {
               if (isUpdateAvailable()) {
                  startupNotificationShown = true;
                  KeyInput.setScreen(clientObj, new UpdateNotificationScreen(currentScreen));
               }
            }
         }
      }
   }

   public static void openModrinth() {
      openUrl("https://modrinth.com/mod/cpvp-macros");
   }

   public static void openUrl(String url) {
      try {
         if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Action.BROWSE)) {
            Desktop.getDesktop().browse(new URI(url));
            return;
         }
      } catch (Throwable var3) {
      }

      try {
         String os = System.getProperty("os.name", "").toLowerCase();
         if (os.contains("win")) {
            new ProcessBuilder("rundll32", "url.dll,FileProtocolHandler", url).start();
         } else if (os.contains("mac")) {
            new ProcessBuilder("open", url).start();
         } else {
            new ProcessBuilder("xdg-open", url).start();
         }
      } catch (Throwable var2) {
      }
   }

   public static boolean isNewerVersion(String current, String latest) {
      if (current != null && latest != null) {
         String curBase = current.split("-")[0].trim();
         String latBase = latest.split("-")[0].trim();
         String[] currParts = curBase.split("\\.");
         String[] lateParts = latBase.split("\\.");
         int len = Math.max(currParts.length, lateParts.length);

         for (int i = 0; i < len; i++) {
            int c = i < currParts.length ? parseOrZero(currParts[i]) : 0;
            int l = i < lateParts.length ? parseOrZero(lateParts[i]) : 0;
            if (l > c) {
               return true;
            }

            if (l < c) {
               return false;
            }
         }

         return false;
      } else {
         return false;
      }
   }

   private static int parseOrZero(String s) {
      try {
         return Integer.parseInt(s.replaceAll("[^\\d]", ""));
      } catch (NumberFormatException var2) {
         return 0;
      }
   }
}
