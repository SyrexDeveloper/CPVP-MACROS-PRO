package dev.priyme.legitcrystalmacro.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.BufferedReader;
import java.io.IOException;
import java.lang.reflect.Modifier;
import java.nio.charset.StandardCharsets;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.stream.Stream;

public final class ConfigProfiles {
   private static final Gson JSON = new GsonBuilder().setPrettyPrinting().create();
   private static final long MAX_BYTES = 1048576L;

   private ConfigProfiles() {
   }

   public static Path directory() {
      return MacroConfig.configPath().toAbsolutePath().getParent().resolve("legitcrystalmacro/profiles");
   }

   public static Path exportDirectory() {
      return directory().getParent().resolve("exports");
   }

   public static List<String> list() throws IOException {
      Path dir = directory();
      Files.createDirectories(dir);

      List var2;
      try (Stream<Path> files = Files.list(dir)) {
         var2 = files.filter(p -> Files.isRegularFile(p, LinkOption.NOFOLLOW_LINKS))
            .map(p -> p.getFileName().toString())
            .filter(n -> n.endsWith(".json"))
            .map(n -> n.substring(0, n.length() - 5))
            .sorted(String.CASE_INSENSITIVE_ORDER)
            .toList();
      }

      return var2;
   }

   public static String validateName(String value) {
      String name = value == null ? "" : value.strip();
      if (name.isEmpty()
         || name.codePointCount(0, name.length()) > 48
         || !name.codePoints().allMatch(c -> Character.isLetterOrDigit(c) || c == 32 || c == 95 || c == 45)) {
         throw new IllegalArgumentException("Use 1-48 letters, numbers, spaces, _ or -.");
      } else if (name.matches("(?i)CON|PRN|AUX|NUL|COM[1-9]|LPT[1-9]")) {
         throw new IllegalArgumentException("This name is reserved by Windows.");
      } else {
         return name;
      }
   }

   private static Path profile(String name) {
      return directory().resolve(validateName(name) + ".json");
   }

   private static void plainFile(Path file) throws IOException {
      if (Files.isSymbolicLink(file) || Files.exists(file) && !Files.isRegularFile(file)) {
         throw new IOException("Expected a regular file: " + file.getFileName());
      }
   }

   private static Path existing(String name) throws IOException {
      String normalized = validateName(name);

      for (String entry : list()) {
         if (entry.equalsIgnoreCase(normalized)) {
            return profile(entry);
         }
      }

      throw new NoSuchFileException("Profile not found: " + normalized);
   }

   public static String snapshot(MacroConfig config) {
      MacroConfig copy = (MacroConfig)JSON.fromJson(JSON.toJson(config), MacroConfig.class);
      copy.sanitize();
      return JSON.toJson(copy);
   }

   public static MacroConfig read(Path file) throws IOException {
      plainFile(file);
      if (Files.size(file) > 1048576L) {
         throw new IOException("Config exceeds 1 MB.");
      } else {
         try {
            MacroConfig var6;
            try (BufferedReader reader = Files.newBufferedReader(file, StandardCharsets.UTF_8)) {
               JsonElement root = JsonParser.parseReader(reader);
               if (!root.isJsonObject()) {
                  throw new IllegalArgumentException("Expected a JSON config object.");
               }

               JsonObject object = root.getAsJsonObject();
               boolean known = Arrays.stream(MacroConfig.class.getFields())
                  .filter(f -> !Modifier.isStatic(f.getModifiers()))
                  .anyMatch(f -> object.has(f.getName()));
               if (!known) {
                  throw new IllegalArgumentException("No CPvP Macros settings found in this file.");
               }

               MacroConfig config = (MacroConfig)JSON.fromJson(object, MacroConfig.class);
               config.sanitize();
               JSON.toJson(config);
               var6 = config;
            }

            return var6;
         } catch (RuntimeException var9) {
            throw new IOException("Invalid config: " + var9.getMessage(), var9);
         }
      }
   }

   public static MacroConfig load(String name) throws IOException {
      return read(existing(name));
   }

   public static void create(String name, String json) throws IOException {
      String normalized = validateName(name);
      if (list().stream().anyMatch(n -> n.equalsIgnoreCase(normalized))) {
         throw new FileAlreadyExistsException("A profile with this name already exists.");
      } else {
         write(profile(normalized), json, false);
      }
   }

   public static void update(String name, String json) throws IOException {
      write(existing(name), json, true);
   }

   public static void rename(String name, String replacement) throws IOException {
      Path source = existing(name);
      String next = validateName(replacement);
      if (name.equalsIgnoreCase(next)) {
         if (!source.getFileName().toString().equals(next + ".json")) {
            throw new IOException("Use a different name; letter case alone does not rename a profile.");
         }
      } else if (list().stream().anyMatch(n -> n.equalsIgnoreCase(next))) {
         throw new FileAlreadyExistsException("A profile with this name already exists.");
      } else {
         Files.move(source, profile(next));
      }
   }

   public static void delete(String name) throws IOException {
      Path file = existing(name);
      backup(file);
      Files.delete(file);
   }

   public static Path export(Path destination, String json, boolean overwrite) throws IOException {
      Path file = destination.toAbsolutePath().normalize();
      if (!file.getFileName().toString().toLowerCase(Locale.ROOT).endsWith(".json")) {
         file = file.resolveSibling(file.getFileName() + ".json");
      }

      if (file.equals(MacroConfig.configPath().toAbsolutePath().normalize())) {
         throw new IOException("Choose a separate export file, not the active config.");
      } else {
         write(file, json, overwrite);
         return file;
      }
   }

   private static void backup(Path file) throws IOException {
      Path backup = file.resolveSibling(file.getFileName() + ".bak");
      plainFile(file);
      plainFile(backup);
      atomicWrite(backup, Files.readString(file, StandardCharsets.UTF_8), true);
   }

   private static void write(Path file, String json, boolean overwrite) throws IOException {
      plainFile(file);
      Files.createDirectories(file.toAbsolutePath().getParent());
      if (Files.exists(file)) {
         if (!overwrite) {
            throw new FileAlreadyExistsException(file.toString());
         }

         backup(file);
      }

      atomicWrite(file, json, overwrite);
   }

   private static void atomicWrite(Path file, String json, boolean replace) throws IOException {
      Path temporary = Files.createTempFile(file.toAbsolutePath().getParent(), "cpvp-config-", ".tmp");

      try {
         Files.writeString(temporary, json, StandardCharsets.UTF_8);
         if (!replace) {
            Files.move(temporary, file);
         } else {
            try {
               Files.move(temporary, file, StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING);
            } catch (AtomicMoveNotSupportedException var8) {
               Files.move(temporary, file, StandardCopyOption.REPLACE_EXISTING);
            }
         }
      } finally {
         Files.deleteIfExists(temporary);
      }
   }
}
