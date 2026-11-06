package dev.priyme.legitcrystalmacro.gui;

import dev.priyme.legitcrystalmacro.config.ConfigProfiles;
import dev.priyme.legitcrystalmacro.config.MacroConfig;
import dev.priyme.legitcrystalmacro.util.ConfigFileManager;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;
import java.util.stream.Stream;

final class ConfigProfilesPanel {
   private static final ExecutorService IO = Executors.newSingleThreadExecutor(r -> {
      Thread thread = new Thread(r, "LCM-Profile-IO");
      thread.setDaemon(true);
      return thread;
   });
   private final Runnable changed;
   private final ConfigTextField name = new ConfigTextField(96);
   private final ConfigTextField path = new ConfigTextField(2048);
   private List<String> profiles = List.of();
   private List<String> profileLabels = List.of();
   private List<ConfigProfilesPanel.Entry> entries = List.of();
   private List<String> entryLabels = List.of();
   private String selected = "";
   private String status = "Save your current settings as a named profile.";
   private String shortStatus = this.status;
   private Path folder;
   private boolean busy;
   private boolean opened;
   private boolean browser;
   private boolean exporting;
   private boolean labelsDirty = true;
   private int profileScroll;
   private int fileScroll;
   private Runnable confirmation;
   private String question = "";

   ConfigProfilesPanel(Runnable changed) {
      this.changed = changed;
   }

   boolean hasTextFocus() {
      return this.name.focused || this.path.focused;
   }

   boolean hasModal() {
      return this.browser || this.confirmation != null;
   }

   void blur() {
      this.name.focused = this.path.focused = false;
   }

   void leave() {
      this.blur();
      this.browser = false;
      this.confirmation = null;
   }

   void open() {
      if (!this.opened) {
         this.opened = true;
         this.refresh();
      }
   }

   private static boolean in(double mx, double my, int x, int y, int w, int h) {
      return mx >= x && mx < x + w && my >= y && my < y + h;
   }

   private void message(String text) {
      this.status = text;
      this.labelsDirty = true;
   }

   private void failed(Exception error) {
      String detail = error.getMessage();
      this.message(detail == null ? error.getClass().getSimpleName() : detail);
   }

   private <T> void work(ConfigProfilesPanel.Work<T> action, Consumer<T> done) {
      if (!this.busy) {
         this.busy = true;
         IO.execute(() -> {
            try {
               T result = action.run();
               ConfigFileManager.executeOnClient(() -> {
                  this.busy = false;

                  try {
                     done.accept(result);
                  } catch (Exception var4x) {
                     this.failed(var4x);
                  }
               });
            } catch (Exception var4) {
               ConfigFileManager.executeOnClient(() -> {
                  this.busy = false;
                  this.failed(var4);
               });
            }
         });
      }
   }

   private void refresh() {
      this.work(ConfigProfiles::list, items -> {
         this.profiles = items;
         this.labelsDirty = true;
         if (!this.profiles.contains(this.selected)) {
            this.selected = "";
         }

         this.profileScroll = Math.max(0, Math.min(this.profileScroll, this.profiles.size() - 5));
      });
   }

   private void apply(ConfigProfilesPanel.Work<Void> action, String success) {
      if (!this.busy) {
         try {
            action.run();
            this.changed.run();
            this.message(success);
         } catch (Exception var4) {
            this.failed(var4);
         }
      }
   }

   private void save(boolean update) {
      if (!this.busy) {
         try {
            String target = update ? this.selected : ConfigProfiles.validateName(this.name.value);
            if (target.isEmpty()) {
               throw new IllegalArgumentException("Select a profile first.");
            }

            String json = ConfigProfiles.snapshot(MacroConfig.get());
            this.work(() -> {
               if (update) {
                  ConfigProfiles.update(target, json);
               } else {
                  ConfigProfiles.create(target, json);
               }

               return ConfigProfiles.list();
            }, items -> {
               this.profiles = items;
               this.selected = target;
               this.name.set(target);
               this.message("Saved profile: " + target);
            });
         } catch (Exception var4) {
            this.failed(var4);
         }
      }
   }

   private void load() {
      if (!this.selected.isEmpty()) {
         String target = this.selected;
         this.work(() -> ConfigProfiles.load(target), config -> this.apply(() -> {
            MacroConfig.get().replaceAndSave(config);
            return null;
         }, "Loaded profile: " + target));
      }
   }

   private void rename() {
      if (!this.selected.isEmpty()) {
         try {
            String old = this.selected;
            String next = ConfigProfiles.validateName(this.name.value);
            this.work(() -> {
               ConfigProfiles.rename(old, next);
               return ConfigProfiles.list();
            }, items -> {
               this.profiles = items;
               this.selected = next;
               this.message("Renamed profile to: " + next);
            });
         } catch (Exception var3) {
            this.failed(var3);
         }
      }
   }

   private void confirm(String text, Runnable action) {
      this.blur();
      this.question = text;
      this.confirmation = action;
   }

   private void remove() {
      String target = this.selected;
      this.confirm("Delete profile \"" + target + "\"?", () -> this.work(() -> {
         ConfigProfiles.delete(target);
         return ConfigProfiles.list();
      }, items -> {
         this.profiles = items;
         this.selected = "";
         this.name.set("");
         this.message("Deleted profile; a .json.bak backup remains.");
      }));
   }

   private void startBrowser(boolean export) {
      if (!this.busy) {
         this.blur();
         this.exporting = export;
         this.browser = true;
         this.fileScroll = 0;
         Path downloads = Path.of(System.getProperty("user.home"), "Downloads");
         this.navigate(export ? ConfigProfiles.exportDirectory() : (Files.isDirectory(downloads) ? downloads : downloads.getParent()));
      }
   }

   private void navigate(Path next) {
      Path directory = next.toAbsolutePath().normalize();
      this.work(
         () -> {
            if (this.exporting) {
               Files.createDirectories(directory);
            }

            List var3;
            try (Stream<Path> files = Files.list(directory)) {
               var3 = files.filter(p -> Files.isDirectory(p) || p.getFileName().toString().toLowerCase(Locale.ROOT).endsWith(".json"))
                  .sorted(
                     Comparator.<Path, Boolean>comparing(p -> !Files.isDirectory(p))
                        .thenComparing(p -> p.getFileName().toString(), String.CASE_INSENSITIVE_ORDER)
                  )
                  .map(p -> new ConfigProfilesPanel.Entry(p, Files.isDirectory(p), (Files.isDirectory(p) ? "[Folder] " : "") + p.getFileName()))
                  .toList();
            }

            return var3;
         },
         items -> {
            this.entries = items;
            this.folder = directory;
            this.fileScroll = 0;
            this.labelsDirty = true;
            this.path.set(this.exporting ? directory.resolve("cpvp-config.json").toString() : directory.toString());
            this.path.focused = true;
         }
      );
   }

   private Path inputPath() {
      String value = this.path.value.strip();
      if (value.startsWith("\"") && value.endsWith("\"") && value.length() > 1) {
         value = value.substring(1, value.length() - 1);
      }

      if (value.startsWith("~")) {
         value = System.getProperty("user.home") + value.substring(1);
      }

      if (value.isBlank()) {
         throw new IllegalArgumentException("Enter a file or folder path.");
      } else {
         return Path.of(value).toAbsolutePath().normalize();
      }
   }

   private void chooseFile(boolean overwrite) {
      if (!this.busy) {
         try {
            Path file = this.inputPath();
            if (Files.isDirectory(file)) {
               this.navigate(file);
               return;
            }

            if (this.exporting) {
               if (!file.getFileName().toString().toLowerCase(Locale.ROOT).endsWith(".json")) {
                  file = file.resolveSibling(file.getFileName() + ".json");
               }

               Path target = file;
               if (!overwrite && Files.exists(target)) {
                  this.confirm("Replace this JSON file? A backup will be saved.", () -> this.export(target, true));
               } else {
                  this.export(target, overwrite);
               }
            } else {
               final Path importFile = file;
               this.work(() -> ConfigProfiles.read(importFile), config -> {
                  try {
                     MacroConfig.get().replaceAndSave(config);
                     this.changed.run();
                     this.browser = false;
                     this.blur();
                     this.message("Imported: " + importFile.getFileName());
                  } catch (Exception var4x) {
                     this.failed(var4x);
                  }
               });
            }
         } catch (Exception var4) {
            this.failed(var4);
         }
      }
   }

   private void export(Path target, boolean overwrite) {
      try {
         String json = ConfigProfiles.snapshot(MacroConfig.get());
         this.work(() -> ConfigProfiles.export(target, json, overwrite), file -> {
            this.browser = false;
            this.blur();
            this.message("Exported: " + file);
         });
      } catch (Exception var4) {
         this.failed(var4);
      }
   }

   private static String clipped(GuiGraphicsContext c, String value, int width) {
      if (c.getTextWidthSmall(value) <= width) {
         return value;
      } else {
         int end = value.length();

         while (end > 0 && c.getTextWidthSmall(value.substring(0, end) + "…") > width) {
            end = value.offsetByCodePoints(end, -1);
         }

         return value.substring(0, end) + "…";
      }
   }

   void invalidateText() {
      this.labelsDirty = true;
   }

   private void labels(GuiGraphicsContext c) {
      if (this.labelsDirty) {
         this.profileLabels = this.profiles.stream().map(n -> clipped(c, n, 212)).toList();
         this.entryLabels = this.entries.stream().map(e -> clipped(c, e.label, 384)).toList();
         this.shortStatus = clipped(c, this.status.replace('\n', ' '), 448);
         this.labelsDirty = false;
      }
   }

   private void button(GuiGraphicsContext c, String label, int x, int y, int w, double mx, double my, boolean enabled) {
      c.drawNineSlice(NineSliceSprite.BUTTON, x, y, w, 26, enabled && in(mx, my, x, y, w, 26) ? Theme.ACCENT : Theme.BUTTON_BG);
      c.drawTextSmall(label, x + (w - c.getTextWidthSmall(label)) / 2, y + 8, enabled ? Theme.TEXT_PRIMARY : Theme.TEXT_SECTION, false);
   }

   void draw(GuiGraphicsContext c, double mx, double my) {
      this.open();
      this.labels(c);
      this.button(c, "Sword Config", 176, 48, 145, mx, my, !this.busy);
      this.button(c, "Spear Config", 329, 48, 145, mx, my, !this.busy);
      this.button(c, "Reset defaults", 482, 48, 146, mx, my, !this.busy);
      c.drawTextMedium("Saved profiles", 176, 94, Theme.TEXT_PRIMARY, false);
      this.name.draw(c, 176, 116, 284, "Profile name, e.g. CPvP");
      this.button(c, "Save new", 468, 116, 160, mx, my, !this.busy);
      c.drawNineSlice(NineSliceSprite.CARD, 176, 150, 238, 142, Theme.CARD_BG);
      c.enableScissor(182, 156, 226, 130);

      for (int i = this.profileScroll; i < Math.min(this.profiles.size(), this.profileScroll + 5); i++) {
         int y = 156 + (i - this.profileScroll) * 25;
         if (this.profiles.get(i).equals(this.selected) || in(mx, my, 182, y, 226, 24)) {
            c.drawNineSlice(NineSliceSprite.BUTTON, 182, y, 226, 24, this.profiles.get(i).equals(this.selected) ? Theme.ACCENT_MUTED : Theme.BUTTON_BG);
         }

         c.drawTextSmall(this.profileLabels.get(i), 189, y + 7, Theme.TEXT_PRIMARY, false);
      }

      if (this.profiles.isEmpty()) {
         c.drawTextSmall("No saved profiles yet.", 188, 168, Theme.TEXT_MUTED, false);
      }

      c.disableScissor();
      boolean chosen = !this.busy && !this.selected.isEmpty();
      this.button(c, "Load", 422, 150, 99, mx, my, chosen);
      this.button(c, "Update", 529, 150, 99, mx, my, chosen);
      this.button(c, "Rename", 422, 184, 99, mx, my, chosen);
      this.button(c, "Delete", 529, 184, 99, mx, my, chosen);
      c.drawTextSmall("Load, edit module settings,", 422, 236, Theme.TEXT_MUTED, false);
      c.drawTextSmall("then Update to save changes.", 422, 252, Theme.TEXT_MUTED, false);
      c.drawTextSmall("Scroll for more profiles.", 422, 276, Theme.TEXT_SECTION, false);
      this.button(c, "Import JSON", 176, 304, 222, mx, my, !this.busy);
      this.button(c, "Export JSON", 406, 304, 222, mx, my, !this.busy);
      c.drawTextSmall(this.busy ? "Working…" : this.shortStatus, 176, 344, Theme.TEXT_MUTED, false);
      if (this.browser) {
         this.drawBrowser(c, mx, my);
      }

      if (this.confirmation != null) {
         c.nextLayer();
         c.drawNineSlice(NineSliceSprite.PANEL, 188, 130, 428, 112, Theme.SETTINGS_BG);
         c.drawTextSmall(clipped(c, this.question, 400), 202, 150, Theme.TEXT_PRIMARY, false);
         this.button(c, "Cancel", 386, 196, 99, mx, my, !this.busy);
         this.button(c, "Confirm", 493, 196, 99, mx, my, !this.busy);
      }
   }

   private void drawBrowser(GuiGraphicsContext c, double mx, double my) {
      c.nextLayer();
      c.drawNineSlice(NineSliceSprite.PANEL, 188, 68, 428, 266, Theme.SETTINGS_BG);
      c.drawTextMedium(this.exporting ? "Export configuration" : "Import configuration", 200, 80, Theme.TEXT_PRIMARY, false);
      this.button(c, "Up", 556, 76, 48, mx, my, !this.busy);
      this.path.draw(c, 200, 108, 344, "Paste a file or folder path");
      this.button(c, "Go", 552, 108, 52, mx, my, !this.busy);
      c.enableScissor(200, 144, 404, 130);

      for (int i = this.fileScroll; i < Math.min(this.entries.size(), this.fileScroll + 5); i++) {
         int y = 144 + (i - this.fileScroll) * 25;
         if (in(mx, my, 200, y, 404, 24)) {
            c.drawNineSlice(NineSliceSprite.BUTTON, 200, y, 404, 24, Theme.BUTTON_BG);
         }

         c.drawTextSmall(this.entryLabels.get(i), 208, y + 7, this.entries.get(i).directory ? Theme.ACCENT_HOVER : Theme.TEXT_PRIMARY, false);
      }

      if (this.entries.isEmpty()) {
         c.drawTextSmall("No JSON files in this folder.", 208, 152, Theme.TEXT_MUTED, false);
      }

      c.disableScissor();
      c.drawTextSmall("Ctrl+V: paste path · Enter: open / confirm", 200, 282, Theme.TEXT_MUTED, false);
      this.button(c, "Cancel", 390, 300, 99, mx, my, !this.busy);
      this.button(c, this.exporting ? "Save" : "Import", 497, 300, 107, mx, my, !this.busy);
   }

   boolean click(double mx, double my, int button) {
      if (button != 0 || this.busy) {
         return true;
      } else if (this.confirmation != null) {
         if (in(mx, my, 386, 196, 99, 26)) {
            this.confirmation = null;
         } else if (in(mx, my, 493, 196, 99, 26)) {
            Runnable action = this.confirmation;
            this.confirmation = null;
            action.run();
         }

         return true;
      } else if (this.browser) {
         this.path.focused = in(mx, my, 200, 108, 344, 26);
         if (in(mx, my, 390, 300, 99, 26)) {
            this.browser = false;
            this.blur();
         } else if (in(mx, my, 497, 300, 107, 26) || in(mx, my, 552, 108, 52, 26)) {
            this.chooseFile(false);
         } else if (in(mx, my, 556, 76, 48, 26) && this.folder != null && this.folder.getParent() != null) {
            this.navigate(this.folder.getParent());
         } else if (in(mx, my, 200, 144, 404, 125)) {
            int index = this.fileScroll + (int)(my - 144.0) / 25;
            if (index < this.entries.size()) {
               ConfigProfilesPanel.Entry entry = this.entries.get(index);
               if (entry.directory) {
                  this.navigate(entry.file);
               } else {
                  this.path.set(entry.file.toString());
                  this.path.focused = true;
               }
            }
         }

         return true;
      } else {
         this.name.focused = in(mx, my, 176, 116, 284, 26);
         if (in(mx, my, 176, 48, 145, 26)) {
            this.apply(() -> {
               ConfigFileManager.applyPresetChecked("Sword Config");
               return null;
            }, "Applied Sword Config; your personal keybinds are preserved.");
         } else if (in(mx, my, 329, 48, 145, 26)) {
            this.apply(() -> {
               ConfigFileManager.applyPresetChecked("Spear Config");
               return null;
            }, "Applied Spear Config; your personal keybinds are preserved.");
         } else if (in(mx, my, 482, 48, 146, 26)) {
            this.confirm("Reset all active settings? A backup will be saved.", () -> this.apply(() -> {
               MacroConfig.get().resetToDefaults();
               return null;
            }, "Default settings restored."));
         } else if (in(mx, my, 468, 116, 160, 26)) {
            this.save(false);
         } else if (in(mx, my, 182, 156, 226, 125)) {
            int index = this.profileScroll + (int)(my - 156.0) / 25;
            if (index < this.profiles.size()) {
               this.selected = this.profiles.get(index);
               this.name.set(this.selected);
            }
         } else if (!this.selected.isEmpty() && in(mx, my, 422, 150, 99, 26)) {
            this.load();
         } else if (!this.selected.isEmpty() && in(mx, my, 529, 150, 99, 26)) {
            this.save(true);
         } else if (!this.selected.isEmpty() && in(mx, my, 422, 184, 99, 26)) {
            this.rename();
         } else if (!this.selected.isEmpty() && in(mx, my, 529, 184, 99, 26)) {
            this.remove();
         } else if (in(mx, my, 176, 304, 222, 26)) {
            this.startBrowser(false);
         } else if (in(mx, my, 406, 304, 222, 26)) {
            this.startBrowser(true);
         }

         return true;
      }
   }

   boolean scroll(double mx, double my, double amount) {
      if (this.confirmation == null && !this.busy) {
         if (this.browser && in(mx, my, 200, 144, 404, 130)) {
            this.fileScroll = Math.max(0, Math.min(Math.max(0, this.entries.size() - 5), this.fileScroll - (int)Math.signum(amount) * 3));
         } else if (!this.browser && in(mx, my, 176, 150, 238, 142)) {
            this.profileScroll = Math.max(0, Math.min(Math.max(0, this.profiles.size() - 5), this.profileScroll - (int)Math.signum(amount) * 3));
         }

         return true;
      } else {
         return true;
      }
   }

   boolean key(int key, int mods, GuiGraphicsContext context) {
      if (key == 256) {
         if (this.confirmation != null) {
            this.confirmation = null;
            return true;
         } else if (this.browser) {
            this.browser = false;
            this.blur();
            return true;
         } else if (this.hasTextFocus()) {
            this.blur();
            return true;
         } else {
            return false;
         }
      } else if (this.busy) {
         return true;
      } else if (this.confirmation != null) {
         return true;
      } else if (key != 257 && key != 335) {
         if (key == 258) {
            if (this.browser) {
               this.path.focused = !this.path.focused;
            } else {
               this.name.focused = !this.name.focused;
            }

            return true;
         } else {
            return this.browser ? this.path.key(key, mods, context) : this.name.key(key, mods, context);
         }
      } else {
         if (this.browser) {
            this.chooseFile(false);
         } else if (this.name.focused) {
            this.save(false);
         }

         return true;
      }
   }

   boolean character(char chr) {
      return !this.busy && this.confirmation == null && (this.browser ? this.path.character(chr) : this.name.character(chr));
   }

   private record Entry(Path file, boolean directory, String label) {
   }

   @FunctionalInterface
   private interface Work<T> {
      T run() throws Exception;
   }
}
